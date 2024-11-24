package mrfast.sbt.guis.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.RelativeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import gg.essential.universal.USound
import gg.essential.vigilance.gui.settings.ColorPicker
import gg.essential.vigilance.utils.onLeftClick
import java.awt.Color
import kotlin.math.roundToInt

/**
 * Modified from Vigliance's ColorPicker.kt
 * Added better alpha picker
 */
class ColorPicker(initial: Color) : UIContainer() {
    private var currentHue: Float
    private var currentSaturation: Float
    private var currentBrightness: Float
    private var currentAlpha = initial.alpha / 255f

    init {
        val hsb = Color.RGBtoHSB(initial.red, initial.green, initial.blue, null)
        currentHue = hsb[0]
        currentSaturation = hsb[1]
        currentBrightness = hsb[2]
        ColorPicker
    }

    private var onValueChange: (Color) -> Unit = { }
    private var draggingHue = false
    private var draggingPicker = false
    private var draggingAlpha = false

    private val bigPickerBox = UIBlock().constrain {
        width = 70.pixels
        height = 100.percent
        color = Color.GRAY.toConstraint()
    } childOf this

    private val pickerIndicator = UIContainer().constrain {
        x = (RelativeConstraint(currentSaturation) - 3.5f.pixels).coerceIn(2.pixels, 2.pixels(alignOpposite = true))
        y = (RelativeConstraint(1f - currentBrightness) - 3.5f.pixels).coerceIn(
            2.pixels,
            2.pixels(alignOpposite = true)
        )
        width = 3.pixels
        height = 3.pixels
    } effect OutlineEffect(Color.WHITE, 1f)

    private val huePickerLine = UIBlock().constrain {
        x = SiblingConstraint(5f)
        width = 14.pixels
        height = 100.percent
        color = Color.GRAY.toConstraint()
    } childOf this

    private val hueIndicator = UIText("◄").constrain {
        x = (-4).pixels(alignOpposite = true)
        y = RelativeConstraint(currentHue) - 5.pixels
        color = Color.WHITE.toConstraint()
    }

    private val alphaPickerLine = UIBlock().constrain {
        x = SiblingConstraint(5f)
        width = 14.pixels
        height = 100.percent
        color = Color.GRAY.toConstraint()
    } childOf this

    private val alphaIndicator = UIText("◄").constrain {
        x = (-4).pixels(alignOpposite = true)
        y = RelativeConstraint(1f - currentAlpha) - 5.pixels
        color = Color.WHITE.toConstraint()
    }

    init {

        huePickerLine.addChild(object : UIComponent() {
            override fun draw(matrixStack: UMatrixStack) {
                super.beforeDraw(matrixStack)
                drawHueLine(matrixStack, this)
                super.draw(matrixStack)
            }
        }.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - 2.pixels
            height = 100.percent - 2.pixels
        }).addChild(hueIndicator)

        huePickerLine.onLeftClick { event ->
            USound.playButtonPress()
            draggingHue = true
            currentHue = (event.relativeY - 1f) / huePickerLine.getHeight()
            updateHueIndicator()
        }.onMouseDrag { _, mouseY, _ ->
            if (!draggingHue) return@onMouseDrag
            currentHue = ((mouseY - 1f) / huePickerLine.getHeight()).coerceIn(0f..1f)
            updateHueIndicator()
        }.onMouseRelease {
            draggingHue = false
        }

        alphaPickerLine.addChild(object : UIComponent() {
            override fun draw(matrixStack: UMatrixStack) {
                super.beforeDraw(matrixStack)
                drawAlphaLine(matrixStack, this)
                super.draw(matrixStack)
            }
        }.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - 2.pixels
            height = 100.percent - 2.pixels
        }).addChild(alphaIndicator)

        alphaPickerLine.onLeftClick { event ->
            USound.playButtonPress()
            draggingAlpha = true
            currentAlpha = 1f - ((event.relativeY - 1f) / alphaPickerLine.getHeight())
            updateAlphaIndicator()
        }.onMouseDrag { _, mouseY, _ ->
            if (!draggingAlpha) return@onMouseDrag
            currentAlpha = 1f - ((mouseY - 1f) / alphaPickerLine.getHeight()).coerceIn(0f..1f)
            updateAlphaIndicator()
        }.onMouseRelease {
            draggingAlpha = false
        }

        bigPickerBox.addChild(object : UIComponent() {
            override fun draw(matrixStack: UMatrixStack) {
                super.beforeDraw(matrixStack)
                drawColorPicker(matrixStack, this)
                super.draw(matrixStack)
            }
        }.constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 100.percent - 2.pixels
            height = 100.percent - 2.pixels
        }).addChild(pickerIndicator)

        bigPickerBox.onLeftClick { event ->
            USound.playButtonPress()
            draggingPicker = true
            currentSaturation = event.relativeX / bigPickerBox.getWidth()
            currentBrightness = 1f - (event.relativeY / bigPickerBox.getHeight())
            updatePickerIndicator()
        }.onMouseDrag { mouseX, mouseY, _ ->
            if (!draggingPicker) return@onMouseDrag
            currentSaturation = (mouseX / bigPickerBox.getWidth()).coerceIn(0f..1f)
            currentBrightness = 1f - ((mouseY / bigPickerBox.getHeight()).coerceIn(0f..1f))
            updatePickerIndicator()
        }.onMouseRelease {
            draggingPicker = false
        }
    }

    private fun updateHueIndicator() {
        hueIndicator.setY(RelativeConstraint(currentHue.coerceAtMost(0.98f)) - 3.pixels)
        recalculateColor()
    }

    private fun updateAlphaIndicator() {
        alphaIndicator.setY(RelativeConstraint(1f - currentAlpha.coerceAtMost(0.98f)) - 3.pixels)
        recalculateColor()
    }

    private fun updatePickerIndicator() {
        pickerIndicator.setX(
            (RelativeConstraint(currentSaturation) - 2.5f.pixels).coerceIn(2.pixels, 2.pixels(alignOpposite = true))
        )
        pickerIndicator.setY(
            (RelativeConstraint(1f - currentBrightness) - 2.5f.pixels).coerceIn(
                2.pixels,
                2.pixels(alignOpposite = true)
            )
        )
        recalculateColor()
    }

    private fun recalculateColor() {
        onValueChange(getCurrentColor())
    }

    fun getCurrentColor(): Color {
        return Color(
            (Color.HSBtoRGB(
                currentHue,
                currentSaturation,
                currentBrightness
            ) and 0xffffff) or ((currentAlpha * 255f).roundToInt() shl 24),
            true
        )
    }

    fun setHSB(hue: Float, sat: Float, bright: Float) {
        currentHue = hue
        currentSaturation = sat
        currentBrightness = bright
        updateHueIndicator()
        updatePickerIndicator()
        recalculateColor()
    }

    fun setAlpha(alpha: Float) {
        currentAlpha = alpha
        updateAlphaIndicator()
        recalculateColor()
    }

    fun onValueChange(listener: (Color) -> Unit) {
        onValueChange = listener
    }

    private fun drawColorPicker(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val right = component.getRight().toDouble()
        val bottom = component.getBottom().toDouble()

        setupDraw()
        val graphics = UGraphics.getFromTessellator()
        graphics.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)

        val height = bottom - top

        for (x in 0..49) {
            val curLeft = left + (right - left).toFloat() * x.toFloat() / 50f
            val curRight = left + (right - left).toFloat() * (x.toFloat() + 1) / 50f

            var first = true
            for (y in 0..50) {
                val yPos = top + (y.toFloat() * height / 50.0)
                val color = getColor(x.toFloat() / 50f, 1 - y.toFloat() / 50f, currentHue)

                if (!first) {
                    drawVertex(graphics, matrixStack, curLeft, yPos, color)
                    drawVertex(graphics, matrixStack, curRight, yPos, color)
                }

                if (y < 50) {
                    drawVertex(graphics, matrixStack, curRight, yPos, color)
                    drawVertex(graphics, matrixStack, curLeft, yPos, color)
                }
                first = false
            }
        }

        graphics.drawDirect()
        cleanupDraw()
    }

    private fun drawHueLine(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val right = component.getRight().toDouble()
        val height = component.getHeight().toDouble()

        setupDraw()
        val graphics = UGraphics.getFromTessellator()
        graphics.beginWithDefaultShader(UGraphics.DrawMode.QUADS, UGraphics.CommonVertexFormats.POSITION_COLOR)

        var first = true
        for ((i, color) in hueColorList.withIndex()) {
            val yPos = top + (i.toFloat() * height / 50.0)
            if (!first) {
                drawVertex(graphics, matrixStack, left, yPos, color)
                drawVertex(graphics, matrixStack, right, yPos, color)
            }

            drawVertex(graphics, matrixStack, right, yPos, color)
            drawVertex(graphics, matrixStack, left, yPos, color)

            first = false
        }

        graphics.drawDirect()
        cleanupDraw()
    }

    private fun drawAlphaLine(matrixStack: UMatrixStack, component: UIComponent) {
        val left = component.getLeft().toDouble()
        val top = component.getTop().toDouble()
        val width = component.getWidth().toDouble()
        val height = component.getHeight().toDouble()

        val rectSize = 2.0 // Size of each square in the checkered pattern
        val baseColor = getCurrentColor()

        // Step 1: Draw the static checkered pattern
        for (y in 0 until (height / rectSize).toInt()) {
            for (x in 0 until (width / rectSize).toInt()) {
                val isDarker = (x + y) % 2 == 0
                val baseGray = if (isDarker) 140 else 205
                val checkerColor = Color(baseGray, baseGray, baseGray)

                // Draw the checkered square
                UIBlock.drawBlock(
                    matrixStack,
                    checkerColor,
                    left + x * rectSize,
                    top + y * rectSize,
                    left + (x + 1) * rectSize,
                    top + (y + 1) * rectSize
                )
            }
        }

        // Step 2: Overlay the gradient
        for (y in 0 until height.toInt()) {
            // Calculate the fade factor (alpha) for the gradient at this y-position
            val fadeFactor = 1f - (y / height).toFloat() // Range: 1.0 (top) to 0.0 (bottom)

            val overlayColor = Color(
                baseColor.red,
                baseColor.green,
                baseColor.blue,
                (255 * fadeFactor).toInt().coerceIn(0, 255) // Gradient alpha
            )

            // Draw a horizontal strip for the gradient
            UIBlock.drawBlock(
                matrixStack,
                overlayColor,
                left,
                top + y,
                left + width,
                top + y + 1 // Single pixel height
            )
        }
    }

    private fun setupDraw() {
        UGraphics.enableBlend()
        UGraphics.disableAlpha()
        UGraphics.tryBlendFuncSeparate(770, 771, 1, 0)
        UGraphics.shadeModel(7425)
    }

    private fun cleanupDraw() {
        UGraphics.shadeModel(7424)
        UGraphics.disableBlend()
        UGraphics.enableAlpha()
    }

    private fun getColor(x: Float, y: Float, hue: Float): Color {
        return Color(Color.HSBtoRGB(hue, x, y))
    }

    private fun drawVertex(graphics: UGraphics, matrixStack: UMatrixStack, x: Double, y: Double, color: Color) {
        graphics
            .pos(matrixStack, x, y, 0.0)
            .color(
                color.red.toFloat() / 255f,
                color.green.toFloat() / 255f,
                color.blue.toFloat() / 255f,
                color.alpha.toFloat() / 255f
            )
            .endVertex()
    }

    companion object {
        private val hueColorList: List<Color> = (0..50).map { i -> Color(Color.HSBtoRGB(i / 50f, 1f, 0.7f)) }
    }
}