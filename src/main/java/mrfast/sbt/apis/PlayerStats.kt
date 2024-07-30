package mrfast.sbt.apis

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.utils.Utils.clean
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

@SkyblockTweaks.EventComponent
object PlayerStats {
    var health = 0
    var maxHealth = 0
    var absorption = 0

    var mana = 0
    var maxMana = 0
    var overflowMana = 0

    var defense = 0
    var effectiveHealth = 0
    var maxEffectiveHealth = 0

    var maxRiftTime = 0
    var riftTimeSeconds = 0

    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        maxRiftTime = 0
    }

    @SubscribeEvent
    fun onEvent(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) {
            var actionBar: String = event.message.formattedText
            val actionBarSplit: List<String> = actionBar.split(" ")

            for (piece in actionBarSplit) {
                val trimmed: String = piece.trim()
                val colorsStripped: String = trimmed.clean().replace(",", "")

                if (trimmed.isEmpty()) continue
                val shortString: String = colorsStripped.substring(0, colorsStripped.length - 1).replace(",", "")

                when {
                    colorsStripped.endsWith("❤") -> parseAndSetHealth(shortString)
                    colorsStripped.endsWith("ф") -> parseAndSetRiftTime(shortString)
                    colorsStripped.endsWith("❈") -> parseAndSetDefense(shortString)
                    colorsStripped.endsWith("✎") -> parseAndSetMana(shortString)
                    colorsStripped.endsWith("ʬ") -> parseAndSetOverflow(shortString)
                }

                actionBar = actionBar.trim()
                event.message = ChatComponentText(actionBar)
            }

            if (GeneralConfig.cleanerActionBar) {
                val arr: List<String> = actionBar.split(" ")

                for (s in arr) {
                    when {
                        s.contains("❤") && GeneralConfig.hideHealthFromBar -> actionBar = actionBar.replace(s, "")
                        (s.contains("❈") || s.contains("Defense")) && GeneralConfig.hideDefenseFromBar -> actionBar =
                            actionBar.replace(s, "")

                        (s.contains("✎") || s.contains("Mana")) && GeneralConfig.hideManaFromBar -> actionBar =
                            actionBar.replace(s, "")

                        s.contains("ʬ") && GeneralConfig.hideOverflowManaFromBar -> actionBar = actionBar.replace(s, "")
                        s.contains("ф") && GeneralConfig.hideRiftTimeFromBar -> actionBar = actionBar.replace(s, "")
                    }
                }

                event.message = ChatComponentText(actionBar.trim())
            }
        }
    }

    private fun parseAndSetHealth(actionBarSegment: String) {
        val split: List<String> = actionBarSegment.split("/")
        health = split[0].toInt()
        maxHealth = split[1].toInt()
        effectiveHealth = (health * (1 + defense / 100))
        maxEffectiveHealth = (maxHealth * (1 + defense / 100))
        absorption = max(health - maxHealth, 0)
    }

    private fun parseAndSetRiftTime(actionBarSegment: String) {
        var minutes = 0
        var seconds = 0
        val containsMinutes = actionBarSegment.contains("m")

        // Split the string into parts separated by 'm'
        val parts = actionBarSegment.split("[m,s]".toRegex())

        if (containsMinutes) {
            minutes = parts[0].toIntOrNull() ?: 0
            seconds = parts[1].toIntOrNull() ?: 0
        } else {
            seconds = parts[0].toIntOrNull() ?: 0
        }

        riftTimeSeconds = minutes * 60 + seconds

        // Update max rift time
        if (riftTimeSeconds > maxRiftTime) maxRiftTime = riftTimeSeconds
    }

    private fun parseAndSetDefense(actionBarSegment: String) {
        defense = actionBarSegment.toInt()
    }

    private fun parseAndSetMana(actionBarSegment: String) {
        val split: List<String> = actionBarSegment.split("/")
        mana = split[0].toInt()
        maxMana = split[1].toInt()
    }

    private fun parseAndSetOverflow(actionBarSegment: String) {
        overflowMana = actionBarSegment.toInt()
    }
}