package mrfast.sbt.utils

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.IInventory
import net.minecraft.util.BlockPos
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

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


    fun String.cleanRanks(): String {
        return this.replace("""\[[^\]]+\]\s*""".toRegex(),"")
    }

    fun String.getStringWidth(): Int {
        return mc.fontRendererObj.getStringWidth(this)
    }

    fun String.matches(regex: Regex): Boolean {
        return regex.containsMatchIn(this)
    }

    fun String.getRegexGroups(regex: Regex): MatchGroupCollection? {
        val regexMatchResult = regex.find(this) ?: return null
        return regexMatchResult.groups
    }

    fun String.toTitleCase(): String {
        return this.split(" ").joinToString(" ") { word ->
            word.lowercase().replaceFirstChar {
                if (it.isLowerCase()) it.titlecase() else it.toString()
            }
        }
    }

    fun Number.formatNumber(): String {
        return String.format("%,.0f", this.toDouble())
    }

    fun Number.abbreviateNumber(): String {
        val num = this.toDouble()
        return when {
            num <= -1_000_000_000_000 || num >= 1_000_000_000_000 -> {
                val value = num / 1_000_000_000_000
                if (value % 1.0 == 0.0) "${value.toInt()}T" else String.format("%.1fT", value)
            }
            num <= -1_000_000_000 || num >= 1_000_000_000 -> {
                val value = num / 1_000_000_000
                if (value % 1.0 == 0.0) "${value.toInt()}B" else String.format("%.1fB", value)
            }
            num <= -1_000_000 || num >= 1_000_000 -> {
                val value = num / 1_000_000
                if (value % 1.0 == 0.0) "${value.toInt()}M" else String.format("%.1fM", value)
            }
            num <= -1_000 || num >= 1_000 -> {
                val value = num / 1_000
                if (value % 1.0 == 0.0) "${value.toInt()}k" else String.format("%.1fk", value)
            }
            else -> String.format("%.0f", num)
        }
    }

    fun Long.toFormattedDuration(short: Boolean? = false): String {
        val seconds = this / 1000
        val minutes = (seconds % 3600) / 60
        val remainingSeconds = seconds % 60
        val hours = (seconds % 86400) / 3600
        val days = seconds / 86400

        if (short == true) {
            return when {
                days > 0 -> "${days}d"
                hours > 0 -> "${hours}h"
                minutes > 0 -> "${minutes}m"
                else -> "${remainingSeconds}s"
            }
        }

        return "${if (days > 0) "${days}d " else ""}${if (hours > 0) "${hours}h " else ""}${if (minutes > 0) "${minutes}m " else ""}${if(remainingSeconds>0) "${remainingSeconds}s" else ""}"
    }

    private val COORD_REGEX = """(?:x:\s*(-?\d+)[,\s]*y:\s*(-?\d+)[,\s]*z:\s*(-?\d+))|(-?\d+)\s+(-?\d+)\s+(-?\d+)""".toRegex()

    fun String.containsCoordinates(): Boolean {
        return COORD_REGEX.containsMatchIn(this)
    }

    fun String.extractCoordinates(): BlockPos? {
        val regexMatchResult = COORD_REGEX.find(this) ?: return null

        val (x, y, z) = when {
            // Check for the first format (x: -359, y: 86, z: -530)
            regexMatchResult.groups[1]?.value != null -> {
                listOf(
                    regexMatchResult.groups[1]!!.value.toInt(),
                    regexMatchResult.groups[2]!!.value.toInt(),
                    regexMatchResult.groups[3]!!.value.toInt()
                )
            }
            // Check for the second format (-333 147 -1003)
            else -> {
                listOf(
                    regexMatchResult.groups[4]!!.value.toInt(),
                    regexMatchResult.groups[5]!!.value.toInt(),
                    regexMatchResult.groups[6]!!.value.toInt()
                )
            }
        }

        return BlockPos(x, y, z)
    }

    // System uses 12-hour: 3/20/25, 3:45 PM
    // User uses 24-hour: 20.03.25, 15:45
    fun Long.toDateTimestamp(onlyTime: Boolean = false): String {
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())
        val formatter = if (onlyTime) {
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
        } else {
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
        }.withLocale(Locale.getDefault()) // Uses system locale settings

        return dateTime.format(formatter)
    }

    fun Long.toFormattedSeconds(): String {
        val seconds = this / 1000.0
        return "${"%.2f".format(seconds)}s"
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

    fun BlockPos.toString(): String {
        return "($x, $y, $z)"
    }

    // Returns the formatted date in the format "March 21st, 2024"
    fun getFormattedDate(): String {
        val today = LocalDate.now()
        val day = today.dayOfMonth
        val suffix = getDaySuffix(day)
        val formatter = DateTimeFormatter.ofPattern("MMMM d'$suffix', yyyy", Locale.ENGLISH)
        return today.format(formatter)
    }

    private fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }
}