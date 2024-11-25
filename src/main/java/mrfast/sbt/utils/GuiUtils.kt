package mrfast.sbt.utils

import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.state.BasicState
import gg.essential.universal.UMatrixStack
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.guis.components.ColorPickerComponent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.guis.components.shader.GaussianBlur
import mrfast.sbt.guis.components.shader.ShaderManager
import mrfast.sbt.utils.Utils.cleanColor
import mrfast.sbt.utils.Utils.getStringWidth
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL14
import java.awt.Color
import java.util.*
import kotlin.math.sin


object GuiUtils {
    var rainbowColor = BasicState(Color.CYAN)

    init {
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val time = System.currentTimeMillis() / 4  // Slower rate
                val red = (sin(time / 200.0) * 127 + 128).toInt()
                val green = (sin(time / 200.0 + 2) * 127 + 128).toInt()
                val blue = (sin(time / 200.0 + 4) * 127 + 128).toInt()
                rainbowColor.set(Color(red, green, blue))
            }
        }, 0, 100)
    }

    enum class TextStyle {
        DROP_SHADOW,
        BLACK_OUTLINE,
        DEFAULT
    }

    fun drawText(text: String, x: Float, y: Float, style: TextStyle) {
        drawText(text, x, y, style, Color(0xFFFFFF))
    }

    fun drawText(
        text: String,
        x: Float,
        y: Float,
        style: TextStyle,
        coreColor: Color,
        centered: Boolean = false,
        scale: Float = 1.0f
    ) {
        val shadowText: String = text.cleanColor()
        val fontRenderer = Minecraft.getMinecraft().fontRendererObj

        // Calculate the centered x position if needed
        val startX = if (centered) {
            val textWidth = fontRenderer.getStringWidth(shadowText) * scale // Scale the width for centering
            x - textWidth / 2 // Adjust the x position based on the scaled width
        } else {
            x
        }

        GlStateManager.pushMatrix() // Push matrix for transformations
        GlStateManager.translate(0f, 0f, 200f)

        // Apply scaling transformation
        GlStateManager.scale(scale, scale, 1f)

        if (style == TextStyle.BLACK_OUTLINE) {
            fontRenderer.drawString(shadowText, startX / scale + 2, y / scale + 1, 0x000000, false)
            fontRenderer.drawString(shadowText, startX / scale, y / scale + 1, 0x000000, false)
            fontRenderer.drawString(shadowText, startX / scale + 1, y / scale + 2, 0x000000, false)
            fontRenderer.drawString(shadowText, startX / scale + 1, y / scale, 0x000000, false)
        }

        // Main Text
        fontRenderer.drawString(
            text,
            startX / scale + 1,
            y / scale + 1,
            coreColor.rgb,
            style == TextStyle.DROP_SHADOW
        )

        GlStateManager.popMatrix() // Restore the previous matrix state
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

    fun highlightSlot(slot: Slot, backgroundColor: Color) {
        Gui.drawRect(
            slot.xDisplayPosition,
            slot.yDisplayPosition,
            slot.xDisplayPosition + 16,
            slot.yDisplayPosition + 16,
            backgroundColor.rgb
        )
    }

    class Button(var width: Float, var height: Float, var x: Int, var y: Int) {
        var onClicked: Runnable? = null

        fun isClicked(mouseX: Double, mouseY: Double, guiLeft: Float, guiTop: Float): Boolean {
            val clicked =
                (mouseX >= (x + guiLeft) && mouseX <= (x + guiLeft) + width && mouseY >= (y + guiTop) && mouseY <= (y + guiTop) + height)
            if (clicked) {
                this.onClicked?.run()
            }
            return clicked
        }

        fun isHovered(mouseX: Double, mouseY: Double, guiLeft: Float, guiTop: Float): Boolean {
            return (mouseX >= (x + guiLeft) && mouseX <= (x + guiLeft) + width && mouseY >= (y + guiTop) && mouseY <= (y + guiTop) + height)
        }
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

    class Element(
        var x: Float,
        var y: Float,
        var text: String,
        var hoverText: List<String>?,
        var onClick: Runnable? = null,
        var drawBackground: Boolean = false
    ) {
        var width = 0
        var height = Utils.mc.fontRendererObj.FONT_HEIGHT

        init {
            width = text.getStringWidth()
        }

        fun draw(mouseX: Int, mouseY: Int, originX: Int = 0, originY: Int = 0) {
            val actualX = x + originX
            val actualY = y + originY

            if (drawBackground) {
                OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                    UMatrixStack(),
                    x - 1,
                    y - 1,
                    width + 3f,
                    height + 3f,
                    3f,
                    Color(40, 40, 40),
                    Color(85, 255, 85).constraint,
                    1f
                )
            }

            drawText(text, x, y, TextStyle.DROP_SHADOW)

            if (mouseX > actualX && mouseY > actualY && mouseX < actualX + width && mouseY < actualY + height) {
                if (hoverText != null) {
                    GlStateManager.pushMatrix()
                    GlStateManager.pushAttrib()
                    net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                        hoverText,
                        mouseX,
                        mouseY,
                        Utils.mc.displayWidth,
                        Utils.mc.displayHeight,
                        -1,
                        Utils.mc.fontRendererObj
                    )
                    GlStateManager.popMatrix()
                    GlStateManager.popAttrib()
                }
                if (onClick != null && Mouse.isButtonDown(0)) {
                    onClick!!.run()
                }
            }
        }
    }

    private val blurShader = GaussianBlur(radius = DeveloperConfig.valueTest.toFloat())
    private var blurScale = 0.5f
    private var time = 0f // Used to track the interpolation time
    private val duration = 150f // Total duration for the easing effect

    fun resetBlurAnimation() {
        blurScale = 0.5f
        time = 0f
    }

    private fun easeInOutCubic(t: Float): Float {
        return if (t < 0.5f) {
            4f * t * t * t
        } else {
            (1f - (Math.pow(-2.0 * t + 2.0, 3.0) / 2f)).toFloat()
        }
    }

    fun drawBackgroundBlur() {
        if (time < duration) {
            time += 1f // Increment time (can adjust speed)
            val t = time / duration // Normalize time to a range of 0 to 1
            val easedValue = easeInOutCubic(t)
            blurScale = 1f + easedValue * (10f - 0.5f) // Ease between 0.5f and 10f
            blurShader.radius = blurScale
        }

        blurShader.drawShader {
            GL11.glPushMatrix()
            GlStateManager.disableTexture2D()
            GlStateManager.color(0f, 0f, 0f, 1f)
            ShaderManager.drawQuads(0f, 0f, Utils.mc.displayWidth / 2f, Utils.mc.displayHeight / 2f)
            GlStateManager.enableTexture2D()
            GL11.glPopMatrix()
        }
    }
}