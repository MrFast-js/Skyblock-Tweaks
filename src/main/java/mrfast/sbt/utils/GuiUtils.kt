package mrfast.sbt.utils

import mrfast.sbt.utils.Utils.cleanColor
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import kotlin.math.sqrt


object GuiUtils {

    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE
    }

    fun drawText(text: String, x: Float, y: Float, style: TextStyle) {
        val shadowText: String = text.cleanColor()

        GlStateManager.translate(0f, 0f, 200f)
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
        GlStateManager.translate(0f, 0f, -200f)
    }

    fun renderItemStackOnScreen(stack: ItemStack?, x: Float, y: Float, width: Float, height: Float) {
        if (stack == null || stack.item == null) {
            return
        }
        GlStateManager.pushMatrix()
        RenderHelper.enableGUIStandardItemLighting()
        GlStateManager.enableDepth()
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.translate(x, y, 0f)
        GlStateManager.scale(width / 16f, height / 16f, 1.0f)
        Minecraft.getMinecraft().renderItem.renderItemIntoGUI(stack, 0, 0)
        Minecraft.getMinecraft().renderItem.renderItemOverlays(Utils.mc.fontRendererObj, stack, 0, 0)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.popMatrix()
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
        if (this.inventorySlots !is ContainerChest) return ""
        val chest = this.inventorySlots as ContainerChest
        val inv = chest.lowerChestInventory
        return inv.displayName.unformattedText.trim()
    }

    /**
     * Taken from NotEnoughUpdates under GNU LGPL v3.0 license
     * @link https://github.com/NotEnoughUpdates/NotEnoughUpdates/blob/master/COPYING
     * @author Linnea Gräf
     */
    fun drawTexture(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        uMin: Float,
        uMax: Float,
        vMin: Float,
        vMax: Float,
        filter: Int
    ) {
        GlStateManager.enableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        GL14.glBlendFuncSeparate(
            GL11.GL_SRC_ALPHA,
            GL11.GL_ONE_MINUS_SRC_ALPHA,
            GL11.GL_ONE,
            GL11.GL_ONE_MINUS_SRC_ALPHA
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        worldrenderer
            .pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex(uMin.toDouble(), vMax.toDouble()).endVertex()
        worldrenderer
            .pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(uMax.toDouble(), vMax.toDouble()).endVertex()
        worldrenderer
            .pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(uMax.toDouble(), vMin.toDouble()).endVertex()
        worldrenderer
            .pos(x.toDouble(), y.toDouble(), 0.0)
            .tex(uMin.toDouble(), vMin.toDouble()).endVertex()
        tessellator.draw()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
        GlStateManager.disableBlend()
    }


}