package mrfast.sbt.utils

import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.sendToServer
import net.hypixel.modapi.HypixelModAPI
import net.hypixel.modapi.handler.ClientboundPacketHandler
import net.hypixel.modapi.packet.impl.clientbound.ClientboundLocationPacket
import net.hypixel.modapi.packet.impl.serverbound.ServerboundLocationPacket
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object LocationUtils {
    var inSkyblock = false
    var inDungeons = false
    var currentIsland = ""
    var currentArea = ""
    var dungeonFloor = 0

    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        inSkyblock = false
        currentArea = ""
        currentIsland = ""

        Utils.setTimeout({
            updatePlayerLocation()
        }, 1000)
    }

    init {
        HypixelModAPI.getInstance().registerHandler(object : ClientboundPacketHandler {
            override fun onLocationPacket(packet: ClientboundLocationPacket?) {
                if (packet != null) {
                    if (packet.map.isPresent) {
                        currentIsland = packet.map.get()
                    }
                    if (packet.serverType.isPresent) {
                        inSkyblock = packet.serverType.get().name == "SkyBlock"
                    }
                    if (DeveloperConfig.showLocationUpdates) {
                        sendDeveloperInfo(packet)
                    }
                }
            }
        })
    }

    private fun sendDeveloperInfo(packet:ClientboundLocationPacket) {
        val chatMessage = ChatComponentText("")

        fun addComponentWithTitle(title: String, content: String, hoverText: String? = null) {
            val comp = ChatComponentText(title)
            comp.chatStyle.chatHoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(hoverText ?: content))
            chatMessage.appendSibling(comp)
        }

        packet.map.ifPresent { addComponentWithTitle("§a[Map] ", it) }
        packet.mode.ifPresent { addComponentWithTitle("§2[Mode] ", it) }
        addComponentWithTitle("§b[Environment] ",
            "Name: ${packet.environment.name}\n" +
                    "Id: ${packet.environment.id}\n" +
                    "Ordinal: ${packet.environment.ordinal}"
        )
        addComponentWithTitle("§3[Proxy] ", packet.proxyName)
        addComponentWithTitle("§e[Serv. Name] ", packet.serverName)
        packet.serverType.ifPresent { addComponentWithTitle("§6[Serv. Type] ", it.name) }

        ChatUtils.sendClientMessage(chatMessage,false)
    }


    private fun updatePlayerLocation() {
        ServerboundLocationPacket().sendToServer()
        for (line in ScoreboardUtils.getSidebarLines(true)) {
            val clean = line.clean()
            if (clean.contains("⏣")) {
                currentArea = clean.split("⏣ ")[1]
                if (currentArea.contains("The Catacombs (")) {
                    dungeonFloor = currentArea.replace("[^0-9]".toRegex(), "").toInt()
                    inDungeons = true
                }
            }
        }
    }
}