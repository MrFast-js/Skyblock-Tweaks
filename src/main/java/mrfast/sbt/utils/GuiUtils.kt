package mrfast.sbt.utils

import mrfast.sbt.utils.Utils.cleanColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.ContainerChest
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.sqrt

object GuiUtils {

    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE
    }

    fun drawText(text: String, x: Float, y: Float, style: TextStyle) {
        val shadowText: String = text.cleanColor()

        GlStateManager.translate(0f,0f,200f)
        if (style == TextStyle.BLACK_OUTLINE) {
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 2, y + 1, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x, y + 1, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y + 2, 0x000000, false)
            Minecraft.getMinecraft().fontRendererObj.drawString(shadowText, x + 1, y, 0x000000, false)
        }
        // Main Text
        Minecraft.getMinecraft().fontRendererObj.drawString(
            text,
            x + 1,
            y + 1,
            0xFFFFFF,
            style == TextStyle.DROP_SHADOW
        )
        GlStateManager.translate(0f,0f,-200f)
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

    fun drawGuiLine(x1: Int, y1: Int, x2: Int, y2: Int, color: Color) {
        GlStateManager.disableLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)

        val dx = (x2 - x1).toFloat()
        val dy = (y2 - y1).toFloat()
        val length = sqrt(dx * dx + dy * dy)

        val nx = dx / length
        val ny = -dy / length

        GL11.glLineWidth(2f)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 0.3f)

        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(x1 - nx + nx.toDouble(), y1 - ny + ny.toDouble(), 0.0).endVertex()
        worldRenderer.pos(x2 - nx + nx.toDouble(), y2 - ny + ny.toDouble(), 0.0).endVertex()
        tessellator.draw()

        GlStateManager.enableTexture2D()
    }

    fun GuiContainer.chestName(): String {
        if(this.inventorySlots !is ContainerChest) return ""
        val chest = this.inventorySlots as ContainerChest
        val inv = chest.lowerChestInventory
        return inv.displayName.unformattedText.trim()
    }
}