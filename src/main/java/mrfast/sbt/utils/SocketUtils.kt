package mrfast.sbt.utils

import io.socket.engineio.client.Socket
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.customevents.SocketMessageEvent
import net.minecraftforge.common.MinecraftForge
import java.net.URISyntaxException

@SkyblockTweaks.EventComponent
object SocketUtils {
    private var socket: Socket? = null
    private var internalClose = false
    var socketConnected = true
    init {
        setupSocket()
    }
    fun setupSocket() {
        println("Attempting connection to SBT websocket! ${DeveloperConfig.modSocketURL}")
        try {
            if (socket != null) {
                internalClose = true
                socket!!.close()
            }
            // Connect to the Socket.IO server
            socket = Socket(DeveloperConfig.modSocketURL)
            val socket = socket?:return

            socket.on(Socket.EVENT_OPEN) {
                println("Opened connection to SBT websocket! ${DeveloperConfig.modSocketURL}")
                socket.send("ClientInfo|${SkyblockTweaks.MOD_VERSION}|${Utils.mc.session.profile.name}")

                socketConnected = true
                internalClose = false
            }

            socket.on(Socket.EVENT_MESSAGE) { args: Array<Any> ->
                if (args.isNotEmpty()) {
                    val eventType = args[0].toString().split("~".toRegex())[0]
                    val data = args[0].toString().split("~".toRegex())[1]
                    MinecraftForge.EVENT_BUS.post(SocketMessageEvent(socket, data, eventType))
                }
            }

            socket.on(Socket.EVENT_CLOSE) {
                if (internalClose) return@on
                println("Lost connection to SBT websocket! Retrying in 5 seconds..")
                Utils.setTimeout({
                    setupSocket()
                }, 5000)
                socketConnected = false
            }

            socket.on(Socket.EVENT_ERROR) {
                if (internalClose) return@on
                socketConnected = false
            }

            socket.open()
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }
    }
}