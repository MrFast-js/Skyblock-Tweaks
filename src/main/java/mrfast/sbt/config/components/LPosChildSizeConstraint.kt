package mrfast.sbt.config.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PaddingConstraint
import gg.essential.elementa.constraints.SizeConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

/*
Modified size constraint that gets the lowest height of everything combines
 */
class LPosChildSizeConstraint(private val excludedItems: MutableList<UIComponent> = mutableListOf()) : SizeConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getWidthImpl(component: UIComponent): Float {
        val holder = (constrainTo ?: component)
        return holder.children.sumOf {
            it.getWidth() + ((it.constraints.x as? PaddingConstraint)?.getHorizontalPadding(it) ?: 0f).toDouble()
        }.toFloat()
    }

    override fun getHeightImpl(component: UIComponent): Float {
        val holder = (constrainTo ?: component)
        val filtered = holder.children.filter { it !in excludedItems }
        return filtered.sumOf {
            it.getHeight() + ((it.constraints.y as? PaddingConstraint)?.getVerticalPadding(it) ?: 0f).toDouble()
        }.toFloat()
    }

    override fun getRadiusImpl(component: UIComponent): Float {
        return (constrainTo ?: component).children.sumOf { it.getHeight().toDouble() }.toFloat() * 2f
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        when (type) {
            ConstraintType.WIDTH -> visitor.visitChildren(ConstraintType.WIDTH)
            ConstraintType.HEIGHT -> visitor.visitChildren(ConstraintType.HEIGHT)
            ConstraintType.RADIUS -> visitor.visitChildren(ConstraintType.HEIGHT)
            else -> throw IllegalArgumentException(type.prettyName)
        }
    }
}