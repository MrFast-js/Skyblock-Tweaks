package mrfast.sbt.config.components

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.universal.UMatrixStack
import java.awt.Color

class OutlinedRoundedRectangle(
    val borderColor: ConstantColorConstraint,
    val borderWidth: Float,
    val borderRadius: Float
) : UIRoundedRectangle(borderRadius) {
    override fun beforeDraw() {
        super.beforeDraw()

        val left = getLeft() - borderWidth
        val top = getTop() - borderWidth
        val right = getRight() + borderWidth
        val bottom = getBottom() + borderWidth
        val adjustedRadius = borderRadius + 1

        drawRoundedRectangle(UMatrixStack(), left, top, right, bottom, adjustedRadius, borderColor.color)
    }

    companion object {
        fun drawOutlinedRoundedRectangle(
            matrixStack: UMatrixStack,
            left: Float,
            top: Float,
            width: Float,
            height: Float,
            radius: Float,
            color: Color,
            borderColor: ConstantColorConstraint,
            borderWidth: Float
        ) {
            val borderLeft = left - borderWidth
            val borderTop = top - borderWidth
            val borderRight = left + width + borderWidth
            val borderBottom = top + height + borderWidth
            val adjustedRadius = radius + 1
            // Draw outline
            drawRoundedRectangle(
                matrixStack,
                borderLeft,
                borderTop,
                borderRight,
                borderBottom,
                adjustedRadius,
                borderColor.color
            )

            // Draw main thing
            drawRoundedRectangle(matrixStack, left, top, left + width, top + height, radius, color)
        }
    }
}
