package mrfast.sbt.guis.components

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.Window
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.universal.UMatrixStack
import java.awt.Color

class OutlinedRoundedRectangle(
    val borderColor: ConstantColorConstraint,
    val borderWidth: Float,
    val borderRadius: Float
) : UIRoundedRectangle(borderRadius) {

    override fun draw(matrixStack: UMatrixStack) {
        val radius = getRadius()
        val adjustedRadius = borderRadius + 1

        drawRoundedRectangle(
            matrixStack,
            getLeft(),
            getTop(),
            getRight(),
            getBottom(),
            adjustedRadius,
            borderColor.color
        )

        drawRoundedRectangle(
            matrixStack,
            getLeft() + borderWidth,
            getTop() + borderWidth,
            getRight() - borderWidth,
            getBottom() - borderWidth,
            radius,
            getColor()
        )

        if (!isInitialized) {
            isInitialized = true
            afterInitialization()
        }

        beforeChildrenDrawCompat(matrixStack)

        val parentWindow = Window.of(this)
        this.children.forEach { child ->
            // If the child is outside the current viewport, don't waste time drawing
            if (!this.alwaysDrawChildren() && !parentWindow.isAreaVisible(
                    child.getLeft().toDouble(),
                    child.getTop().toDouble(),
                    child.getRight().toDouble(),
                    child.getBottom().toDouble()
                )
            ) return@forEach

            child.drawCompat(matrixStack)
        }

        afterDrawCompat(matrixStack)
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
