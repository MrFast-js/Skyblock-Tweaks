package mrfast.sbt.config.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.constraints.ConstraintType
import gg.essential.elementa.constraints.PaddingConstraint
import gg.essential.elementa.constraints.PositionConstraint
import gg.essential.elementa.constraints.resolution.ConstraintVisitor

/**
 * Taken from https://github.com/EssentialGG/Elementa/blob/master/src/main/kotlin/gg/essential/elementa/constraints/SiblingConstraint.kt
 * @author Elementa
 * @source https://github.com/EssentialGG/Elementa/
 * @license LGPL-3.0 license
 */
open class SiblingConstraintFixed @JvmOverloads constructor(
    val padding: Float = 0f,
    val alignOpposite: Boolean = false
) : PositionConstraint, PaddingConstraint {
    override var cachedValue = 0f
    override var recalculate = true
    override var constrainTo: UIComponent? = null

    override fun getXPositionImpl(component: UIComponent): Float {
        constrainTo?.let {
            return if (alignOpposite) {
                it.getLeft() - component.getWidth() - padding
            } else {
                it.getRight() + padding
            }
        }

        val index = component.parent.children.indexOf(component)

        if (alignOpposite) {
            if (index > 0) {
                val sibling = component.parent.children[index - 1]
                return getLeftmostPoint(sibling, component.parent, index) - component.getWidth() - padding
            } else {
                return component.parent.getRight() - component.getWidth()
            }
        } else {
            if (index > 0) {
                val sibling = component.parent.children[index - 1]
                return getRightmostPoint(sibling, component.parent, index) + padding
            } else {
                return component.parent.getLeft()
            }
        }
    }

    override fun getYPositionImpl(component: UIComponent): Float {
        constrainTo?.let {
            return if (alignOpposite) {
                it.getTop() - component.getHeight() - padding
            } else {
                it.getBottom() + padding
            }
        }

        val index = component.parent.children.indexOf(component)

        if (alignOpposite) {
            if (index > 0) {
                val sibling = component.parent.children[index - 1]
                return getHighestPoint(sibling, component.parent, index) - component.getHeight() - padding
            } else {
                return component.parent.getBottom() - component.getHeight()
            }
        } else {
            if (index > 0) {
                val sibling = component.parent.children[index - 1]
                return getLowestPoint(sibling, component.parent, index) + padding
            } else {
                return component.parent.getTop()
            }
        }

    }

    protected fun getLowestPoint(sibling: UIComponent, parent: UIComponent, index: Int): Float {
        var lowestPoint = sibling.getBottom()

        for (n in index - 1 downTo 0) {
            val child = parent.children[n]

            if (child.getTop() != sibling.getTop()) break

            if (child.getBottom() > lowestPoint) lowestPoint = child.getBottom()
        }

        return lowestPoint
    }

    protected fun getHighestPoint(sibling: UIComponent, parent: UIComponent, index: Int): Float {
        var highestPoint = sibling.getTop()

        for (n in index - 1 downTo 0) {
            val child = parent.children[n]

            if (child.getBottom() != sibling.getBottom()) break

            if (child.getTop() < highestPoint) highestPoint = child.getTop()
        }

        return highestPoint
    }

    protected fun getRightmostPoint(sibling: UIComponent, parent: UIComponent, index: Int): Float {
        var rightmostPoint = sibling.getRight()

        for (n in index - 1 downTo 0) {
            val child = parent.children[n]

            if (child.getLeft() != sibling.getLeft()) break

            if (child.getRight() > rightmostPoint) rightmostPoint = child.getRight()
        }

        return rightmostPoint
    }

    protected fun getLeftmostPoint(sibling: UIComponent, parent: UIComponent, index: Int): Float {
        var leftmostPoint = sibling.getLeft()

        for (n in index - 1 downTo 0) {
            val child = parent.children[n]

            if (child.getRight() != sibling.getRight()) break

            if (child.getLeft() < leftmostPoint) leftmostPoint = child.getLeft()
        }

        return leftmostPoint
    }

    override fun visitImpl(visitor: ConstraintVisitor, type: ConstraintType) {
        val indexInParent = visitor.component.let { it.parent.children.indexOf(it) }

        when (type) {
            ConstraintType.X -> {
                if (alignOpposite) {
                    visitor.visitSelf(ConstraintType.WIDTH)

                    if (indexInParent <= 0) {
                        visitor.visitParent(ConstraintType.X)
                        visitor.visitParent(ConstraintType.WIDTH)
                        return
                    }

                    for (n in indexInParent - 1 downTo 0) {
                        visitor.visitSibling(ConstraintType.X, n)
                        visitor.visitSibling(ConstraintType.WIDTH, n)
                    }
                } else {
                    if (indexInParent <= 0) {
                        visitor.visitParent(ConstraintType.X)
                        return
                    }

                    for (n in indexInParent - 1 downTo 0) {
                        visitor.visitSibling(ConstraintType.X, n)
                        // TODO: Avoid this width call when not actually called in getXPositionImpl
                        visitor.visitSibling(ConstraintType.WIDTH, n)
                    }
                }
            }

            ConstraintType.Y -> {
                if (alignOpposite) {
                    visitor.visitSelf(ConstraintType.HEIGHT)

                    if (indexInParent <= 0) {
                        visitor.visitParent(ConstraintType.Y)
                        visitor.visitParent(ConstraintType.HEIGHT)
                        return
                    }

                    for (n in indexInParent - 1 downTo 0) {
                        visitor.visitSibling(ConstraintType.Y, n)
                        visitor.visitSibling(ConstraintType.HEIGHT, n)
                    }
                } else {
                    if (indexInParent <= 0) {
                        visitor.visitParent(ConstraintType.Y)
                        return
                    }

                    for (n in indexInParent - 1 downTo 0) {
                        visitor.visitSibling(ConstraintType.Y, n)
                        // TODO: Avoid this width call when not actually called in getXPositionImpl
                        visitor.visitSibling(ConstraintType.HEIGHT, n)
                    }
                }
            }

            else -> throw IllegalArgumentException(type.prettyName)
        }
    }

    override fun getVerticalPadding(component: UIComponent): Float {
        val index = component.parent.children.indexOf(component)
        return if (index == 0 && constrainTo == null) 0f else padding
    }

    override fun getHorizontalPadding(component: UIComponent): Float {
        val index = component.parent.children.indexOf(component)
        return if (index == 0 && constrainTo == null) 0f else padding
    }
}