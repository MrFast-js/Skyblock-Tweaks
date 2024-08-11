package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

object ChatUtils {
    private const val modPrefix = "§eSkyblock§9Tweaks§6 >>§r "
    private const val shortModPrefix = "§eSB§9T§6 >>§r "

    fun sendPlayerMessage(message: String) {
        Minecraft.getMinecraft().thePlayer?.sendChatMessage(message)
    }

    fun sendClientMessage(message: String, prefix: Boolean? = false, shortPrefix: Boolean? = false) {
        val selectedPrefix = if (prefix == true) modPrefix else if (shortPrefix == true) shortModPrefix else ""
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText(selectedPrefix + message))
    }

    fun sendClientMessage(message: IChatComponent, prefix: Boolean? = false, shortPrefix: Boolean? = false) {
        val selectedPrefix = if (prefix == true) modPrefix else if (shortPrefix == true) shortModPrefix else ""

        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(
            ChatComponentText(selectedPrefix).appendSibling(
                message
            )
        )
    }
}