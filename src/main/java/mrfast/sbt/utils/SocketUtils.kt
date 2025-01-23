package mrfast.sbt.utils

import io.socket.engineio.client.Socket
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.customevents.SocketMessageEvent
import net.minecraftforge.common.MinecraftForge
import java.net.URISyntaxException

@SkyblockTweaks.EventComponent
object SocketUtils {
    private var socket: Socket? = null
    var socketConnected = true

    init {
        setupSocket()
    }

    private var lostConnection = false
    fun setupSocket() {
        socketConnected = false
        if(DeveloperConfig.showServerErrors) println("Attempting connection to SBT websocket! ${DeveloperConfig.modSocketURL}")
        try {
            // Connect to the Socket.IO server
            socket = Socket(DeveloperConfig.modSocketURL)
            val socket = socket ?: return

            socket.on(Socket.EVENT_OPEN) {
                println("Opened connection to SBT websocket! ${DeveloperConfig.modSocketURL}")
                socket.send("ClientInfo|${SkyblockTweaks.MOD_VERSION}|${Utils.mc.session.profile.name}")

                if(lostConnection) {
                    ItemApi.updateSkyblockItemData(false)
                }

                lostConnection = false
                socketConnected = true
            }

            socket.on(Socket.EVENT_MESSAGE) { args: Array<Any> ->
                if (args.isNotEmpty()) {
                    val eventType = args[0].toString().split("~".toRegex())[0]
                    val data = args[0].toString().split("~".toRegex())[1]
                    MinecraftForge.EVENT_BUS.post(SocketMessageEvent(socket, data, eventType))
                }
            }

            socket.on(Socket.EVENT_CLOSE) { args: Array<Any> ->
                val reason = if (args.isNotEmpty()) args[0].toString() else "Unknown reason"
                println("Connection closed: $reason")
                retryConnection()
            }

            socket.on(Socket.EVENT_ERROR) {

            }

            socket.open()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun retryConnection() {
        if(DeveloperConfig.showServerErrors) println("Lost connection to SBT websocket! Retrying in 5 seconds..")
        lostConnection = true
        Utils.setTimeout({
            setupSocket()
        }, 5000)
        socketConnected = false
    }
}