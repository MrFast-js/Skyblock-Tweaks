package mrfast.sbt.guis.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.UIConstraints
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.basicHeightConstraint
import gg.essential.elementa.dsl.width
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.MappedState
import gg.essential.elementa.state.State
import gg.essential.elementa.state.pixels
import gg.essential.elementa.utils.getStringSplitToWidth
import gg.essential.elementa.utils.getStringSplitToWidthTruncated
import gg.essential.universal.UGraphics
import gg.essential.universal.UMatrixStack
import mrfast.sbt.utils.GuiUtils

/**
 * Taken from Elementa's UIText component, but with some changes to custom font rendering
 * https://github.com/EssentialGG/Elementa/blob/master/src/main/kotlin/gg/essential/elementa/components/UIText.kt
 */
open class CustomUIWrappedText @JvmOverloads constructor(
    text: State<String>,
    shadow: State<Boolean> = BasicState(true),
    private var centered: Boolean = false,
    /**
     * Keeps the rendered text within the bounds of the component,
     * inserting an ellipsis ("...") if text is trimmed
     */
    private val trimText: Boolean = false,
    private val lineSpacing: Float = 9f,
    private val trimmedTextSuffix: String = "..."
) : UIComponent() {
    @JvmOverloads constructor(
        text: String = "",
        shadow: Boolean = true,
        centered: Boolean = false,
        /**
         * Keeps the rendered text within the bounds of the component,
         * inserting an ellipsis ("...") if text is trimmed
         */
        trimText: Boolean = false,
        lineSpacing: Float = 9f,
        trimmedTextSuffix: String = "..."
    ) : this(BasicState(text), BasicState(shadow), centered, trimText, lineSpacing, trimmedTextSuffix)


    private val textState: MappedState<String, String> = text.map { it } // extra map so we can easily rebind it
    private val shadowState: MappedState<Boolean, Boolean> = shadow.map { it }
    private val textScaleState = asState { getTextScale() }
    private val fontProviderState = asState { fontProvider }
    private var textWidthState = textState.zip(textScaleState.zip(fontProviderState)).map { (text, opts) ->
        val (textScale, fontProvider) = opts
        text.width(textScale, fontProvider) / textScale
    }

    private val charWidth = UGraphics.getCharWidth('x')

    /** Guess on whether we should be trying to center or top-align this component. See [BELOW_LINE_HEIGHT]. */
    private val verticallyCenteredState = asState { y is CenterConstraint }

    private fun <T> asState(selector: UIConstraints.() -> T) = BasicState(selector(constraints)).also {
        constraints.addObserver { _, _ -> it.set(selector(constraints)) }
    }

    /**
     * Balances out space required below the line by adding empty space above the first one.
     * Also, if there are no shadows, the last line can be shorter so it looks more centered overall.
     */
    private val extraHeightState = fontProviderState.zip(verticallyCenteredState).zip(shadowState).map { (opts, shadow) ->
        val (fontProvider, verticallyCentered) = opts
        (if (verticallyCentered) fontProvider.getBelowLineHeight() else 0f) + (if (shadow) 0f else - fontProvider.getShadowHeight())
    }

    init {
        setWidth(textWidthState.pixels())
        setHeight(basicHeightConstraint {
            val fontProvider = super.getFontProvider()

            val lines = getStringSplitToWidth(
                getText(),
                getWidth(),
                getTextScale(),
                ensureSpaceAtEndOfLines = false,
                fontProvider = fontProvider,
            )
            if (lines.isEmpty()) {
                return@basicHeightConstraint 0f
            }

            // The height of the last line of text should be equal the size of that text
            // independent of the lineSpacing property. Otherwise, when lineSpacing is greater
            // than the text height the component's size will be larger than the area the text
            // is rendered
            ((lines.size - 1) * lineSpacing + extraHeightState.get() // All lines but last
                    + (fontProvider.getBaseLineHeight() + fontProvider.getBelowLineHeight() + fontProvider.getShadowHeight()) // Last line
                    ) * getTextScale()
        })
    }

    fun getText() = textState.get()

    override fun draw(matrixStack: UMatrixStack) {
        beforeDrawCompat(matrixStack)

        val textScale = getTextScale()
        val x = getLeft()
        val y = getTop() + (if (verticallyCenteredState.get()) fontProviderState.get().getBelowLineHeight() * textScale else 0f)
        val width = getWidth()
        val color = getColor()

        // We aren't visible, don't draw
        if (color.alpha <= 10) {
            return super.draw(matrixStack)
        }

        if (width / textScale <= charWidth) {
            // If we are smaller than a char, we can't physically split this string into
            // "width" strings, so we'll prefer a no-op to an error.
            return super.draw(matrixStack)
        }

        UGraphics.enableBlend()

        val lines = if (trimText) {
            getStringSplitToWidthTruncated(
                textState.get(),
                width,
                textScale,
                getMaxLines(),
                ensureSpaceAtEndOfLines = false,
                fontProvider = getFontProvider(),
                trimmedTextSuffix = trimmedTextSuffix
            )
        } else {
            getStringSplitToWidth(
                textState.get(),
                width,
                textScale,
                ensureSpaceAtEndOfLines = false,
                fontProvider = getFontProvider()
            )
        }.map { it.trimEnd() }

        lines.forEachIndexed { i, line ->
            val xOffset = if (centered) {
                (width - line.width(textScale)) / 2f
            } else 0f

            GuiUtils.drawText(
                line,
                x + xOffset,
                y + i * lineSpacing * textScale,
                GuiUtils.TextStyle.DROP_SHADOW,
                color,
                centered = false,
                scale = textScale,
            )
        }

        super.draw(matrixStack)
    }

    private fun getMaxLines(): Int {
        val fontProvider = getFontProvider()
        val height = getHeight() / getTextScale() - extraHeightState.get()
        val baseLineHeight = fontProvider.getBaseLineHeight() + fontProvider.getBelowLineHeight() + fontProvider.getShadowHeight()

        if (height < baseLineHeight) {
            return 0
        }

        return 1 + ((height - baseLineHeight) / lineSpacing).toInt()
    }
}
