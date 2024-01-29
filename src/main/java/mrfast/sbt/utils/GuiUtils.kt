package mrfast.sbt.utils

import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.Minecraft

object GuiUtils {

    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE
    }

    fun drawText(text: String, x: Float, y: Float, style: TextStyle) {
        val bold = text.contains(ChatFormatting.BOLD.toString())
        var shadowText: String = Utils.cleanColor(text)
        if (bold) shadowText = ChatFormatting.BOLD.toString() + shadowText

        if (style == TextStyle.BLACK_OUTLINE) {
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x - 1, y, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y + 1, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y - 1, 0x000000, false)
        }
        // Main Text
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x, y, 0xFFFFFF, style == TextStyle.DROP_SHADOW)
    }
}