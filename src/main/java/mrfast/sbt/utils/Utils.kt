package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
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


    /*
     * Cleans all minecraft formatting from text
     */
    fun String.clean(): String {
        return this.replace("§[0-9a-zA-Z]".toRegex(), "")
    }

    /*
     * Cleans all minecraft color formatting from text
     */
    fun String.cleanColor(): String {
        return this.replace(Regex("(?i)§[0-9A-F]"), "")
    }

    fun String.getNameNoRank(): String {
        val clean = this.clean()
        val noRankName =  if (clean.contains("]")) clean.split("] ")[1] else clean
        return noRankName.split(" ")[0]
    }

    fun String.getStringWidth(): Int {
        return mc.fontRendererObj.getStringWidth(this)
    }

    fun String.matches(regex: String): Boolean {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(this)
        return matcher.find()
    }

    fun String.getRegexGroups(regex: String): Matcher? {
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(this)
        if (!matcher.find()) return null

        return matcher
    }

    fun Number.formatNumber(): String {
        return String.format("%,.0f", this.toDouble())
    }

    fun Number.abbreviateNumber(): String {
        val num = this.toDouble()
        return when {
            num <= -1_000_000_000_000 || num >= 1_000_000_000_000 -> String.format("%.1fT", num / 1_000_000_000_000)
            num <= -1_000_000_000 || num >= 1_000_000_000 -> String.format("%.1fB", num / 1_000_000_000)
            num <= -1_000_000 || num >= 1_000_000 -> String.format("%.1fM", num / 1_000_000)
            num <= -1_000 || num >= 1_000 -> String.format("%.1fk", num / 1_000)
            else -> String.format("%.0f", num)
        }
    }

    fun Long.toFormattedTime(): String {
        val seconds = this / 1000
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60

        return "${if (hours > 0) "${hours}h " else ""}${if (minutes > 0) "${minutes}m " else ""}${remainingSeconds}s"
    }

    fun copyToClipboard(text: String) {
        val stringSelection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(stringSelection, null)
    }

    fun playSound(soundName: String, pitch: Double) {
        mc.thePlayer.playSound(soundName, 1.0F, pitch.toFloat())
    }

    fun GuiChest.getInventory(): IInventory {
        return ((this.inventorySlots) as ContainerChest).lowerChestInventory
    }
}