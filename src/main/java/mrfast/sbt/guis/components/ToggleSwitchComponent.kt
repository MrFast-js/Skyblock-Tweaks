package mrfast.sbt.guis.components

import gg.essential.elementa.components.UICircle
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
            radius = (9.5f).pixels
            width = 28.pixels
            height = 16.pixels
            color = if (activated) CustomizationConfig.onSwitchColor.colorState.constraint else CustomizationConfig.offSwitchColor.colorState.constraint
        }

        val block = UICircle(7.5f)

        block.constrain {
            x = 8.pixels
            y = CenterConstraint()
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
            block.setX((20).pixels)
        }

        this.onMouseClick {
            activated = !activated
            block.animate {
                setXAnimation(Animations.IN_OUT_ELASTIC, 0.5f, if (activated) (20).pixels else 8.pixels)
            }
            this.animate {
                setColorAnimation(
                    Animations.IN_OUT_CIRCULAR,
                    0.5f,
                    if (activated) CustomizationConfig.onSwitchColor.colorState.constraint else  CustomizationConfig.offSwitchColor.colorState.constraint
                )
            }
            Utils.mc.thePlayer.playSound("gui.button.press", 0.25f, 1f)
        }
    }

}
