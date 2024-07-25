package mrfast.sbt.config.components

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.utils.Utils
import java.awt.Color

class ToggleSwitchComponent(initValue: Boolean) : UIRoundedRectangle(0f) {
    var activated = initValue

    init {
        this.constrain {
            x = 0.pixels
            y = CenterConstraint()
            radius = 9.pixels
            width = 32.pixels
            height = 16.pixels
            color = if (activated) activatedColor.constraint else deactivatedColor.constraint
        }

        val block = UIRoundedRectangle(8f)

        block.constrain {
            x = 1.pixels
            y = 1.pixels
            width = 14.pixels
            height = 14.pixels
            color = Color(44, 44, 44).constraint
        } childOf this

        this.onMouseEnterRunnable {
            block.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(54, 54, 54).constraint)
            }
        }
        this.onMouseLeaveRunnable {
            block.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(44, 44, 44).constraint)
            }
        }

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

    companion object {
        var deactivatedColor = BasicState(CustomizationConfig.offSwitchColor)
        var activatedColor = BasicState(CustomizationConfig.onSwitchColor)
    }
}
