package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent

object ChatUtils {
    private const val modChatPrefix = "§eSkyblock§9Tweaks§6 >>§r "

    fun sendPlayerMessage(message: String) {
        Minecraft.getMinecraft().thePlayer?.sendChatMessage(message)
    }
    fun logMessage(message: String) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText(modChatPrefix+message))
    }
    fun logMessage(message: IChatComponent) {
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText(modChatPrefix).appendSibling(message))
    }
}