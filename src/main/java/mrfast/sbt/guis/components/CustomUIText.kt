package mrfast.sbt.guis.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.width
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State
import gg.essential.elementa.state.pixels
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import mrfast.sbt.utils.GuiUtils

/**
 * Simple text component that draws its given `text` at the scale determined by
 * this component's width & height constraints.
 */
open class CustomUIText(
    text: State<String>,
    shadow: State<Boolean> = BasicState(true)
) : UIComponent() {
    @JvmOverloads constructor(
        text: String = "",
        shadow: Boolean = true
    ) : this(BasicState(text), BasicState(shadow))

    private val textState: MappedState<String, String> = text.map { it } // extra map so we can easily rebind it
    private val shadowState: MappedState<Boolean, Boolean> = shadow.map { it }
    private val textScaleState = asState { getTextScale() }
    /** Guess on whether we should be trying to center or top-align this component. See [BELOW_LINE_HEIGHT]. */
    private val verticallyCenteredState = asState { y is CenterConstraint }
    private val fontProviderState = asState { fontProvider }
    private var textWidthState = textState.zip(textScaleState.zip(fontProviderState)).map { (text, opts) ->
        val (textScale, fontProvider) = opts
        text.width(textScale, fontProvider) / textScale
    }

    private fun <T> asState(selector: UIConstraints.() -> T) = BasicState(selector(constraints)).also {
        constraints.addObserver { _, _ -> it.set(selector(constraints)) }
    }

    init {
        setWidth(textWidthState.pixels())
        setHeight(shadowState.zip(verticallyCenteredState.zip(fontProviderState)).map { (shadow, opts) ->
            val (verticallyCentered, fontProvider) = opts
            val above = (if (verticallyCentered) fontProvider.getBelowLineHeight() else 0f)
            val center = fontProvider.getBaseLineHeight()
            val below = fontProvider.getBelowLineHeight() + (if (shadow) fontProvider.getShadowHeight() else 0f)
            above + center + below
        }.pixels())
    }

    fun getText() = textState.get()
    fun setText(text: String) = apply { textState.set(text) }

    override fun getWidth(): Float {
        return super.getWidth() * getTextScale()
    }

    override fun getHeight(): Float {
        return super.getHeight() * getTextScale()
    }

    override fun draw(matrixStack: UMatrixStack) {
        val text = textState.get()
        if (text.isEmpty())
            return

        beforeDrawCompat(matrixStack)

        val scale = getWidth() / textWidthState.get()
        val x = getLeft()
        val y = getTop() + (if (verticallyCenteredState.get()) fontProviderState.get().getBelowLineHeight() * scale else 0f)
        val color = getColor()

        // We aren't visible, don't draw
        if (color.alpha <= 10) {
            return super.draw(matrixStack)
        }

        UGraphics.enableBlend()

        GuiUtils.drawText(
            textState.get(),
            x, y,
            GuiUtils.TextStyle.DROP_SHADOW,
            color,
            centered = false,
            scale = scale,
        )

        super.draw(matrixStack)
    }
}
