package mrfast.sbt.guis.components

import gg.essential.elementa.state.BasicState
import gg.essential.elementa.utils.withAlpha
import mrfast.sbt.utils.GuiUtils
import java.awt.Color

class CustomColor(var r: Int, var g: Int, var b: Int, var a: Int, var chroma: Boolean = false) {
    @Transient
    var colorState = BasicState(Color(r, g, b, a))
    @Transient
    var initialColor = Color(r, g, b, a)

    // Secondary constructor: accepts a Color object
    constructor(color: Color) : this(color.red, color.green, color.blue, color.alpha, false) {
        // Calls the primary constructor
    }

    constructor(r: Int, g: Int, b: Int) : this(r, g, b, 255) {
        // Converts the hexCode to RGB values and calls the primary constructor
    }

    // Secondary constructor: accepts a single color hex code (e.g., #FFFFFF)
    constructor(hex: Int) : this(Color(hex)) {
        // Converts the hexCode to RGB values and calls the primary constructor
    }

    fun get(): Color {
        val rainbow = BasicState(GuiUtils.rainbowColor.get().withAlpha(a))
        if (chroma) {
            colorState.set(rainbow.get())
        } else {
            if (colorState == rainbow) {
                colorState.set(Color(r, g, b, a))
            }
        }
        return colorState.get()
    }

    fun fromString(string: String): CustomColor {
        try {
            val split =
                string.substringAfter('{').substringBefore('}').split(",") // Extract content between brackets and split
            if (split.size == 5) {
                r = split[0].split("=")[1].toDouble().toInt()
                g = split[1].split("=")[1].toDouble().toInt()
                b = split[2].split("=")[1].toDouble().toInt()
                a = split[3].split("=")[1].toDouble().toInt()
                chroma = split[4].split("=")[1].toBoolean()
            }
        } catch (e: Exception) {
            println("Error parsing color string: $string")
            e.printStackTrace()
        }
        return CustomColor(r, g, b, a, chroma)
    }

    fun set(r: Int, g: Int, b: Int) {
        colorState.set(Color(r, g, b))
        updateRGB()
    }

    fun set(hex: Int) {
        colorState.set(Color(hex))
        updateRGB()
    }

    fun set(color: Color) {
        colorState.set(color)
        updateRGB()
    }

    private fun updateRGB() {
        this.r = colorState.get().red
        this.g = colorState.get().green
        this.b = colorState.get().blue
        this.a = colorState.get().alpha
    }
}