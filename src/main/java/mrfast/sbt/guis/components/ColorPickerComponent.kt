package mrfast.sbt.guis.components

import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import gg.essential.vigilance.gui.settings.SettingComponent
import java.awt.Color

class ColorPickerComponent(initialColor: Color) : SettingComponent() {
    private var currentColor = BasicState(initialColor)
    private var colorPicker: ColorPicker? = null
    fun setPickerColor(color: Color) {
        currentColor.set(color)
        val hsb = Color.RGBtoHSB(color.red, color.green, color.blue, null)
        colorPicker?.setHSB(hsb[0],hsb[1],hsb[2])
        colorPicker?.setAlpha(color.alpha / 255f)
    }
    init {
        constrain {
            color = Color(0x232323).constraint
            width = 120.pixels
            height = 90.pixels
        }

        val box = OutlinedRoundedRectangle(currentColor.constraint, 2f, 4f).constrain {
            width = 100.percent
            height = 100.percent
            x = CenterConstraint()
            y = CenterConstraint()
            color = Color(18, 18, 18).constraint
        } childOf this

        colorPicker = ColorPicker(initialColor).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            width = 90.percent
            height = 78.pixels
        } childOf box

        colorPicker!!.onValueChange { color ->
            changeValue(color)
            currentColor.set(color)
        }
    }
}