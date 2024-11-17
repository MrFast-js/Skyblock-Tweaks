package mrfast.sbt.guis.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.constraint
import gg.essential.elementa.dsl.pixels
import gg.essential.universal.UMatrixStack
import mrfast.sbt.utils.GuiUtils
import net.minecraft.item.ItemStack
import java.awt.Color

class ItemComponent(var stack: ItemStack, var resolution: Float? = 16f) : UIBlock() {
    private val itemContainer = UIBlock().constrain {
        color = Color(0, 0, 0, 0).constraint
        width = resolution!!.pixels
        height = resolution!!.pixels
        x = CenterConstraint()
        y = CenterConstraint()
    } childOf this

    override fun draw(matrixStack: UMatrixStack) {
        GuiUtils.renderItemStackOnScreen(
            stack,
            itemContainer.getLeft(),
            itemContainer.getTop(),
            itemContainer.getWidth(),
            itemContainer.getHeight()
        )
    }

    init {
        val box = this.constrain {
            color = Color(0, 0, 0, 0).constraint
            width = resolution!!.pixels
            height = resolution!!.pixels
        }
    }
}