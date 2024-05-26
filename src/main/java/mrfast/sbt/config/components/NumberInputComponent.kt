package mrfast.sbt.config.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

class NumberInputComponent(initValue: Int) : UIBlock() {
    var intValue = 0

    init {
        val box = this.constrain {
            color = Color(0x232323).constraint
            x = 0.pixels
            y = CenterConstraint()
            width = 58.pixels
            height = 16.pixels
        } effect OutlineEffect(Color(0x606060), 1f)

        val textInput = UITextInput("").constrain {
            color = Color(0xBBBBBB).constraint
            width = 54.pixels
            height = 16.pixels
            x = 3.pixels
            y = 4.pixels
        } childOf this

        this.onMouseClick {
            textInput.grabWindowFocus()
        }
        textInput.setText(initValue.toString())
        intValue = initValue


        textInput.onKeyType { typedChar, keyCode ->
            box.setColor(Color(0x232323))

            val cleanNumber: String = textInput.getText().replace("[^0-9]".toRegex(), "")
            try {
                intValue = cleanNumber.toInt()
            } catch (_: Exception) {}

            textInput.setText(cleanNumber)
            this@NumberInputComponent.keyType(typedChar,keyCode)
        }

        textInput.onFocusLost {
            if (textInput.getText().isEmpty()) {
                box.setColor(Color(0x401613))
                textInput.setText("0")
            } else {
                box.setColor(Color(0x232323))
            }
        }
    }
}