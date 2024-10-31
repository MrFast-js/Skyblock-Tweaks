package mrfast.sbt.config.components

import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import java.awt.Color

class TextInputComponent(initValue: String, placeholder: String = "") : UIBlock() {
    var text = ""

    init {
        this.constrain {
            color = Color(0x232323).constraint
            x = 0.pixels
            y = CenterConstraint()
            width = 154.pixels
            height = 16.pixels
        } effect OutlineEffect(Color(0x606060), 1f)

        val textInput = UITextInput(placeholder).constrain {
            color = Color(0xBBBBBB).constraint
            width = 100.percent - 4.pixels
            height = 16.pixels
            x = 3.pixels
            y = 4.pixels
        } childOf this

        this.onMouseClick {
            textInput.grabWindowFocus()
        }
        textInput.setText(initValue)
        text = initValue

        textInput.onKeyType { typedChar, keyCode ->
            text = textInput.getText()
            this@TextInputComponent.keyType(typedChar, keyCode)
        }
    }
}