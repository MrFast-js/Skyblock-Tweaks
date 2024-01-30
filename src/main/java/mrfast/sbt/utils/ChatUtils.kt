package mrfast.sbt.utils

import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft
import net.minecraft.util.ChatComponentText

object ChatUtils {
    fun sendPlayerMessage(message: String) {
        Minecraft.getMinecraft().thePlayer?.sendChatMessage(message)
    }
    fun logMessage(message: String) {
        val prefix = "§eSkyblock§9Tweaks§6 >>§r "
        Minecraft.getMinecraft().ingameGUI.chatGUI.printChatMessage(ChatComponentText(prefix+message))
    }
}