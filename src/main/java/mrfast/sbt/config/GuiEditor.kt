package mrfast.sbt.config

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.client.config.GuiUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.max

class GuiEditor : GuiScreen() {
    private val guiManager = GuiManager // Your GuiManager object
    private var hoveredElement: GuiManager.Element? = null
    private var selectedElement: GuiManager.Element? = null
    private var showAllEnabledElements = false
    private var offsetX = 0.0
    private var offsetY = 0.0
    private var screenWidth = Utils.mc.displayWidth / 2
    private var screenHeight = Utils.mc.displayHeight / 2

    override fun initGui() {
        super.initGui()
        buttonList.add(GuiButton(6969, width / 2 - 60, 0, 120, 20, getButtonLabel()))
    }

    private fun getButtonLabel() = "§e§lShow ${if (showAllEnabledElements) "Active Only" else "All Enabled"}"

    override fun onGuiClosed() {
        super.onGuiClosed()
        GuiManager.saveGuiElements()
    }

    override fun actionPerformed(button: GuiButton?) {
        super.actionPerformed(button)
        // Toggle view all visible elements
        if (button!!.id == 6969) {
            showAllEnabledElements = !showAllEnabledElements
            button.displayString = getButtonLabel()
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.drawScreen(mouseX, mouseY, partialTicks)

        val scaledResolution = ScaledResolution(Utils.mc)
        screenWidth = scaledResolution.scaledWidth
        screenHeight = scaledResolution.scaledHeight

        drawDefaultBackground()
        updateMousePos(mouseX, mouseY)
        for (element in guiManager.guiElements) {
            if (!element.isActive()) continue
            if (!showAllEnabledElements && !element.isVisible()) continue

            val x = (element.relativeX * screenWidth)
            val y = (element.relativeY * screenHeight)
            val backgroundColor = Color(25, 25, 25, if (element == hoveredElement) 200 else 159)
            var borderColor = Color(100, 100, 100, 220)
            if (element == hoveredElement) borderColor = Color(150, 150, 150, 220)

            val actualWidth = (element.width * element.scale).toInt()
            val actualHeight = (element.height * element.scale).toInt()

            // Offset for outlined box
            mrfast.sbt.utils.GuiUtils.drawOutlinedSquare(
                (x - 2).toInt(),
                (y - 2).toInt(),
                actualWidth + 4,
                actualHeight + 4,
                backgroundColor,
                borderColor
            )

            GlStateManager.translate(x, y, 0.0)
            GlStateManager.scale(element.scale, element.scale, 1.0)

            GlStateManager.pushMatrix()
            if (element.needsExample) {
                element.drawExample()
            } else {
                element.draw()
            }
            GlStateManager.popMatrix()

            GlStateManager.scale(1 / element.scale, 1 / element.scale, 1.0)
            GlStateManager.translate(-x, -y, 0.0)

            // Draw name for elements for debugging
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && CustomizationConfig.developerMode) {
                fontRendererObj.drawString(element.elementName, x.toInt(), (y - 10).toInt(), 0xFFFFFF)
            }
        }

        buttonList.find {
            it.id == 6969
        }?.drawButton(Utils.mc, mouseX, mouseY)

        if (hoveredElement != null && !isMouseMoving) {
            val renderTooltip = mutableListOf(
                "§a§l${hoveredElement!!.elementName}",
                "§7X: §e${Math.round(hoveredElement!!.relativeX * screenWidth)} §7Y: §e${Math.round(hoveredElement!!.relativeY * screenHeight)} §7Scale: §e${hoveredElement!!.scale}",
                "§3R-CLICK to open config"
            )
            val fontObj = Utils.mc.fontRendererObj
            val tooltipWidth = fontObj.getStringWidth(renderTooltip[0])
            val tooltipHeight = renderTooltip.size * fontObj.FONT_HEIGHT

            var adjustedX = max(0, mouseX - 3)
            var adjustedY = mouseY

            if (adjustedX + tooltipWidth > screenWidth) {
                adjustedX = screenWidth - tooltipWidth
            }

            if (mouseY + tooltipHeight > screenHeight) {
                adjustedY = max(screenHeight - tooltipHeight, mouseY - tooltipHeight - 12)
            }

            if (Mouse.isButtonDown(1)) {
                ConfigGui.openConfigSearch(hoveredElement!!.elementName)
            }

            GuiUtils.drawHoveringText(renderTooltip, adjustedX, adjustedY, screenWidth, screenHeight, -1, fontObj)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        super.mouseClicked(mouseX, mouseY, mouseButton)
        for (element in guiManager.guiElements) {
            if (!element.isActive()) continue
            if (!showAllEnabledElements && !element.isVisible()) continue

            val x = (element.relativeX * screenWidth).toInt()
            val y = (element.relativeY * screenHeight).toInt()
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
            selectedElement?.let {
                it.scale += 0.5 * if (wheel > 0) 1 else -1
                it.scale = it.scale.coerceIn(0.5, 4.0)
            }
        }
    }

    private var lastMoveTime = System.currentTimeMillis()
    private var isMouseMoving = false
    private var copyingPos = false
    private var lastMouseX = 0
    private var lastMouseY = 0

    private fun updateMousePos(mouseX: Int, mouseY: Int) {
        hoveredElement = null
        for (element in guiManager.guiElements) {
            if (!element.isActive()) continue
            if (!showAllEnabledElements && !element.isVisible()) continue

            val x = (element.relativeX * screenWidth).toInt()
            val y = (element.relativeY * screenHeight).toInt()
            val elementWidth = (element.width * element.scale).toInt()
            val elementHeight = (element.height * element.scale).toInt()

            // Element hovered
            if (mouseX >= x && mouseX < x + elementWidth && mouseY >= y && mouseY < y + elementHeight) {
                hoveredElement = element
                break
            }
        }
        if (Mouse.isButtonDown(0)) {
            val selectedElement = selectedElement ?: return

            // Left mouse button pressed, handle dragging
            selectedElement.let {
                // Calculate new positions
                var newX = (mouseX - offsetX) / screenWidth.toDouble()
                var newY = (mouseY - offsetY) / screenHeight.toDouble()

                // Calculate the scaled width and height
                val scaledWidth = it.width * it.scale
                val scaledHeight = it.height * it.scale

                // Calculate maximum valid X and Y values, ensuring they are not negative
                val maxX = maxOf(0.0, 1.0 - scaledWidth / screenWidth.toDouble())
                val maxY = maxOf(0.0, 1.0 - scaledHeight / screenHeight.toDouble())

                // Ensure the element stays within the screen's horizontal bounds
                newX = newX.coerceIn(0.0, maxX)

                // Ensure the element stays within the screen's vertical bounds
                newY = newY.coerceIn(0.0, maxY)

                // Apply the adjusted values
                it.relativeX = newX
                it.relativeY = newY
            }
            // Potentially add edge snapping against other elements here
        } else {
            selectedElement = null
        }

        // Reset position if the element is completely off the screen
        for (element in guiManager.guiElements) {
            val elementRight = element.relativeX + (element.width * element.scale) / screenWidth.toDouble()
            val elementBottom = element.relativeY + (element.height * element.scale) / screenHeight.toDouble()
            if (element.relativeX >= 1.0 || element.relativeY >= 1.0 || elementRight <= 0.0 || elementBottom <= 0.0) {
                element.relativeX = 0.0
                element.relativeY = 0.0
            }
        }

        // Debug tool to copy elements position to clipboard
        if (CustomizationConfig.developerMode && hoveredElement != null) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && Keyboard.isKeyDown(Keyboard.KEY_C)) {
                if (copyingPos) return
                copyingPos = true

                val point =
                    "this.relativeX = ${hoveredElement!!.relativeX}\nthis.relativeY = ${hoveredElement!!.relativeY}"
                Utils.copyToClipboard(point)
                ChatUtils.sendClientMessage(ChatFormatting.GREEN.toString() + "Copied hovered element position: " + ChatFormatting.YELLOW + point)
            } else {
                copyingPos = false
            }
        }

        val currentTime = System.currentTimeMillis()
        if (lastMouseX != mouseX || lastMouseY != mouseY) {
            lastMoveTime = currentTime
        }
        isMouseMoving = currentTime - lastMoveTime < 500

        lastMouseX = mouseX
        lastMouseY = mouseY
    }
}
