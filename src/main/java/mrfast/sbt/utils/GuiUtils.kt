package mrfast.sbt.utils

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbt.utils.Utils.clean
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.ContainerChest
import java.awt.Color

object GuiUtils {

    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE
    }

    fun drawText(text: String, x: Float, y: Float, style: TextStyle) {
        val bold = text.contains(ChatFormatting.BOLD.toString())
        var shadowText: String = text.clean()
        if (bold) shadowText = ChatFormatting.BOLD.toString() + shadowText

        if (style == TextStyle.BLACK_OUTLINE) {
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 2, y + 1, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y + 1, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y + 2, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y, 0x000000, false)
        }
        // Main Text
        Minecraft.getMinecraft().fontRendererObj.drawString(text, x + 1, y + 1, 0xFFFFFF, style == TextStyle.DROP_SHADOW)
    }

    fun drawOutlinedSquare(x: Int, y: Int, width: Int, height: Int, backgroundColor: Color, borderColor: Color) {
        // Draw the filled square
        Gui.drawRect(x, y, x + width, y + height, backgroundColor.rgb)

        // Draw the border without overlapping on the corners
        Gui.drawRect(x, y, x + width, y + 1, borderColor.rgb) // Top
        Gui.drawRect(x, y + 1, x + 1, y + height - 1, borderColor.rgb) // Left
        Gui.drawRect(x + width - 1, y + 1, x + width, y + height - 1, borderColor.rgb) // Right
        Gui.drawRect(x, y + height - 1, x + width, y + height, borderColor.rgb) // Bottom
    }

    fun GuiContainer.chestName(): String {
        val chest = this.inventorySlots as ContainerChest
        val inv = chest.lowerChestInventory
        return inv.displayName.unformattedText.trim()
    }
}