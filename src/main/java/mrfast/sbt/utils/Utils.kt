package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

object Utils {
    val mc: Minecraft = Minecraft.getMinecraft()

    fun setTimeout(runnable: () -> Unit, delayMillis: Long) {
        Thread {
            Thread.sleep(delayMillis)
            runnable()
        }.start()
    }

    /**
     * Cleans all minecraft formatting from text
     */
    fun String.clean(): String {
        return this.replace("§[0-9a-zA-Z]".toRegex(), "")
    }

    fun Number.formatNumber(): String {
        return String.format("%,.0f", this.toDouble())
    }

    fun clamp(min: Double,value: Double,max:Double): Double {
        return max.coerceAtMost(value.coerceAtLeast(min))
    }

    fun copyToClipboard(text: String) {
        val stringSelection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }

    fun playSound(soundName: String, pitch: Double) {
        mc.thePlayer.playSound(soundName, 1.0F, pitch.toFloat())
    }
}