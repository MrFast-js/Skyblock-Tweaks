package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

object ChatUtils {
    private const val modChatPrefix = "§eSkyblock§9Tweaks§6 >>§r "

    fun sendPlayerMessage(message: String) {
        Minecraft.getMinecraft().thePlayer?.sendChatMessage(message)
    }

    fun sendClientMessage(message: String, prefix: Boolean? = false) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText((if (prefix == true) (modChatPrefix) else "") + message))
    }

    fun sendClientMessage(message: IChatComponent, prefix: Boolean? = false) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(
            ChatComponentText(if (prefix == true) modChatPrefix else "").appendSibling(
                message
            )
        )
    }
}