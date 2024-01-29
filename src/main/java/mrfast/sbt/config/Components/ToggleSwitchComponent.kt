package mrfast.sbt.config.Components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.animate
import gg.essential.elementa.dsl.childOf
import gg.essential.elementa.dsl.constrain
import gg.essential.elementa.dsl.pixels
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import mrfast.sbt.utils.Utils
import java.awt.Color

class ToggleSwitchComponent(initValue: Boolean) : UIBlock() {
    var activated = initValue
    private var deactivatedColor = BasicState(Color.GRAY)
    private var activatedColor = BasicState(Color.GREEN)

    init {
        this.constrain {
            x = 0.pixels
            y = CenterConstraint()
            width = 32.pixels
            height = 16.pixels
            color = if (activated) activatedColor.constraint else deactivatedColor.constraint
        }

        val block = UIBlock(Color.DARK_GRAY).constrain {
            x = 1.pixels
            y = 1.pixels
            width = 14.pixels
            height = 14.pixels
        } childOf this

        if (activated) {
            block.setX(1.pixels(alignOpposite = true))
        }

        this.onMouseClick {
            activated = !activated
            block.animate {
                setXAnimation(Animations.OUT_EXP, 0.5f, if (activated) 1.pixels(alignOpposite = true) else 1.pixels)
            }
            this.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, if (activated) activatedColor.constraint else deactivatedColor.constraint)
            }
            Utils.mc.thePlayer.playSound("gui.button.press", 0.25f, 1f)
        }
    }
}
