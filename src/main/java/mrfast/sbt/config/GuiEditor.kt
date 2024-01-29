package mrfast.sbt.config

import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.realms.RealmsMth.clamp
import org.lwjgl.input.Mouse
import java.awt.Color

class GuiEditor : GuiScreen() {
    private val guiManager = GuiManager // Your GuiManager object

    private var hoveredElement: GuiManager.Element? = null
    private var selectedElement: GuiManager.Element? = null
    private var offsetX = 0.0
    private var offsetY = 0.0
    private var res = ScaledResolution(Utils.mc)

    override fun onGuiClosed() {
        super.onGuiClosed()
        GuiManager.saveGuiElements()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        updateMousePos(mouseX, mouseY)
        for (element in guiManager.guiElements) {
            val x = (element.relativeX * res.scaledWidth)
            val y = (element.relativeY * res.scaledHeight)


            GlStateManager.translate(x, y, 0.0)
            GlStateManager.scale(element.scale, element.scale, 1.0)

            drawRect(0, 0, element.width, element.height, Color(255, 255, 255, 100).rgb) // Draw a background rectangle
            element.draw()

            GlStateManager.scale(1 / element.scale, 1 / element.scale, 1.0)
            GlStateManager.translate(-x, -y, 0.0)

            // Draw additional content for each element if needed
            fontRendererObj.drawString(element.elementName, x.toInt(), (y - 10).toInt(), 0xFFFFFF)
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        for (element in guiManager.guiElements) {
            val x = (element.relativeX * res.scaledWidth).toInt()
            val y = (element.relativeY * res.scaledHeight).toInt()
            val elementWidth = (element.width * element.scale).toInt()
            val elementHeight = (element.height * element.scale).toInt()

            // Element hovered
            if (mouseX >= x && mouseX < x + elementWidth && mouseY >= y && mouseY < y + elementHeight) {
                selectedElement = element
                offsetX = (mouseX - x).toDouble()
                offsetY = (mouseY - y).toDouble()
                break
            }
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()

        val wheel = Mouse.getEventDWheel()

        if (wheel != 0) {
            println("WHEEL MOVED: ${wheel}")
            hoveredElement?.let {
                it.scale += 0.5 * if (wheel > 0) 1 else -1
                it.scale = clamp(1.0, it.scale, 4.0)
            }
        }
    }

    fun updateMousePos(mouseX: Int, mouseY: Int) {
        hoveredElement = null
        for (element in guiManager.guiElements) {
            val x = (element.relativeX * res.scaledWidth).toInt()
            val y = (element.relativeY * res.scaledHeight).toInt()
            val elementWidth = (element.width * element.scale).toInt()
            val elementHeight = (element.height * element.scale).toInt()

            // Element hovered
            if (mouseX >= x && mouseX < x + elementWidth && mouseY >= y && mouseY < y + elementHeight) {
                hoveredElement = element
                break
            }
        }
        if (Mouse.isButtonDown(0)) {
            // Left mouse button pressed, handle dragging
            selectedElement?.let {
                it.relativeX = (mouseX - offsetX) / res.scaledWidth.toDouble()
                it.relativeY = (mouseY - offsetY) / res.scaledHeight.toDouble()
            }
        } else {
            selectedElement = null
        }
    }
}
