package mrfast.sbt.guis.components

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.svg.data.plus
import mrfast.sbt.utils.Utils.getStringWidth
import java.awt.Color

class TextInputComponent(
    initValue: String,
    placeholder: String = "",
    boxWidth: Int = 154,
    fancy: Boolean = true,
    textColor: Color = Color(0xBBBBBB),
    maxLength: Int = 100,
    dynamicWidth: Boolean = false,
) : UIBlock() {
    var text = ""
    var enterRunnable = Runnable {
        this.loseFocus()
    }

    fun onEnterPressed(method: UIComponent.() -> Unit) = apply {
        enterRunnable = Runnable {
            method()
        }
    }

    val textInput = UITextInput(placeholder).constrain {
        color = textColor.constraint
        width = 100.percent - 4.pixels
        height = 16.pixels
        x = 3.pixels
        y = 4.pixels
    } childOf this

    fun setTextValue(text: String) {
        textInput.setText(text)
        this.text = text
    }

    init {
        this.constrain {
            color = Color(0x232323).constraint
            x = 0.pixels
            y = CenterConstraint()
            width = boxWidth.pixels
            height = 16.pixels
        }
        if (fancy) {
            this.effect(OutlineEffect(Color(0x606060), 1f))
        } else {
            this.setColor(Color(0, 0, 0, 0))
        }

        if (!fancy) {
            textInput.setY(0.pixels)
            textInput.setX(0.pixels)
        }

        this.onMouseClick {
            textInput.grabWindowFocus()
        }
        textInput.setText(initValue)
        text = initValue
        textInput.onActivate {
            enterRunnable.run()
        }
        textInput.onKeyType { typedChar, keyCode ->
            if(textInput.getText().length > maxLength) {
                textInput.setText(textInput.getText().substring(0, maxLength))
            }
            if(dynamicWidth) {
                textInput.setWidth((textInput.getText().getStringWidth()+3).pixels)
            }

            text = textInput.getText()
            this@TextInputComponent.keyType(typedChar, keyCode)
        }
    }
}