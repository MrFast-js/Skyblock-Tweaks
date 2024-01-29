package mrfast.sbt.apis

import mrfast.sbt.config.Categories.GeneralConfig
import mrfast.sbt.utils.Utils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object PlayerStats {
    var health = 0
    var maxHealth = 0
    var absorption = 0

    var mana = 0
    var maxMana = 0
    var overflowMana = 0

    var defense = 0
    var effectiveHealth = 0

    @SubscribeEvent
    fun onEvent(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) {
            var actionBar: String = event.message.formattedText
            val actionBarSplit: List<String> = actionBar.split(" ")

            for (piece in actionBarSplit) {
                val trimmed: String = piece.trim()
                val colorsStripped: String = Utils.cleanColor(trimmed).replace(",", "")

                if (trimmed.isEmpty()) continue
                val shortString: String = colorsStripped.substring(0, colorsStripped.length - 1).replace(",", "")

                when {
                    colorsStripped.endsWith("❤") -> parseAndSetHealth(shortString)
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
                        (s.contains("❈") || s.contains("Defense")) && GeneralConfig.hideDefenseFromBar -> actionBar = actionBar.replace(s, "")
                        (s.contains("✎") || s.contains("Mana")) && GeneralConfig.hideManaFromBar -> actionBar = actionBar.replace(s, "")
                        s.contains("ʬ") && GeneralConfig.hideOverflowManaFromBar -> actionBar = actionBar.replace(s, "")
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
        effectiveHealth = (health * (1f + defense / 100f).toInt())
        absorption = max(health-maxHealth,0)
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