package mrfast.sbt.config.components

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.ConstantColorConstraint
import gg.essential.universal.UMatrixStack

class OutlinedRoundedRectangle(val borderColor: ConstantColorConstraint, val borderWidth: Float, val borderRadius: Float) : UIRoundedRectangle(borderRadius) {
    override fun beforeDraw() {
        super.beforeDraw()

        val left = getLeft() - borderWidth
        val top = getTop() - borderWidth
        val right = getRight() + borderWidth
        val bottom = getBottom() + borderWidth
        val adjustedRadius = borderRadius + 1

        drawRoundedRectangle(UMatrixStack(), left, top, right, bottom, adjustedRadius, borderColor.color)
    }
}
