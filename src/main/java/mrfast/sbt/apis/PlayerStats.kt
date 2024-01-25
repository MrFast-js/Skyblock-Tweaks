package mrfast.sbt.apis

import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

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
    fun onActionBarUpdate(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) {
            println(event.message.formattedText)

            event.message = ChatComponentText(updateStatsAndRemoveSegments(event.message.unformattedTextForChat))
            println("Current Health: $health")
            println("Max Health: $maxHealth")
            println("Defense: $defense")
            println("Current Mana: $mana")
            println("Max Mana: $maxMana")
            println("Overflow Mana: $overflowMana")
        }
    }

    val removeHealth: Boolean = true
    val removeMana: Boolean = true

    fun updateStatsAndRemoveSegments(input: String): String {
        // Define regex patterns for each segment
        val healthPattern = Regex("§c(\\d{1,3}(?:,\\d{3})*)/(\\d{1,3}(?:,\\d{3})*)❤")
        val defensePattern = Regex("§a(\\d{1,3}(?:,\\d{3})*)§a❈ Defense")
        val manaPattern = Regex("§b(\\d{1,3}(?:,\\d{3})*)/(\\d{1,3}(?:,\\d{3})*)✎")
        val overflowManaPattern = Regex("§3(\\d{1,3}(?:,\\d{3})*)ʬ")

        // Extract values from regex matches or default to 0
        val healthMatch = healthPattern.find(input)
        val defenseMatch = defensePattern.find(input)
        val manaMatch = manaPattern.find(input)
        val overflowManaMatch = overflowManaPattern.find(input)

        health = healthMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toInt() ?: health
        maxHealth = healthMatch?.groupValues?.getOrNull(2)?.replace(",", "")?.toInt() ?: maxHealth
        defense = defenseMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toInt() ?: defense
        mana = manaMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toInt() ?: mana
        maxMana = manaMatch?.groupValues?.getOrNull(2)?.replace(",", "")?.toInt() ?: maxHealth
        overflowMana = overflowManaMatch?.groupValues?.getOrNull(1)?.replace(",", "")?.toInt() ?: overflowMana


        // Remove specified segments from the input string
        var modifiedInput = input
        if (removeHealth) {
            modifiedInput = modifiedInput.replace(healthPattern, "")
        }
        if (removeMana) {
            modifiedInput = modifiedInput.replace(manaPattern, "")
        }

        // Return the modified input string
        return modifiedInput
    }
}