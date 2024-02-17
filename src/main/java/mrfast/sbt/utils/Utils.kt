package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.util.regex.Matcher
import java.util.regex.Pattern

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
    /**
     * Cleans all minecraft color formatting from text
     */
    fun String.cleanColor(): String {
        return this.replace(Regex("(?i)§[0-9A-F]"), "")
    }

    fun String.matches(regex: String): Boolean {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(this)
        return matcher.find()
    }

    fun String.getRegexGroups(regex: String): Matcher? {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(this)
        if(!matcher.find()) return null

        return matcher
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