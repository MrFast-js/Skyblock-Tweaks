package mrfast.sbt.config.Components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import mrfast.sbt.config.Categories.CustomizationConfig
import mrfast.sbt.config.Categories.CustomizationConfig.selectedTheme
import mrfast.sbt.utils.Utils
import java.awt.Color

class ToggleSwitchComponent(initValue: Boolean) : UIRoundedRectangle(0f) {
    var activated = initValue
    private var deactivatedColor = BasicState(Color.GRAY)
    private var activatedColor = BasicState(CustomizationConfig.enabledSwitchColor)

    init {
        this.constrain {
            x = 0.pixels
            y = CenterConstraint()
            radius = 9.pixels
            width = 32.pixels
            height = 16.pixels
            color = if (activated) CustomizationConfig.enabledSwitchColor.constraint else deactivatedColor.constraint
        }

        val block = UIRoundedRectangle(8f)

        block.constrain {
            x = 1.pixels
            y = 1.pixels
            width = 14.pixels
            height = 14.pixels
            color = Color.DARK_GRAY.constraint
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
