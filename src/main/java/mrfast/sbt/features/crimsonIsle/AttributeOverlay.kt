package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.customevents.RenderItemStackEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getAttributes
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object AttributeOverlay {
    private val GodRollCombos = mapOf(
        Pair("crimson", Pair("magic_find", "veteran")),
        Pair("aurora", Pair("mana_pool", "mana_regeneration")),
        Pair("terror", Pair("mana_pool", "lifeline"))
    )

    @SubscribeEvent
    fun onItemStackDrawn(event: RenderItemStackEvent) {
        if (!CrimsonConfig.itemAttributeOverlay) return

        val itemStack = event.itemStack
        val attributes = itemStack.getAttributes()

        if (attributes.isEmpty()) return

        if(CrimsonConfig.itemAttributeOverlayShowGodRoll) {
            val suggestedPrice = ItemUtils.getPriceMatch(itemStack)
            var highestAttributeTier = 0
            var godRoll = false

            attributes.entries.forEach {
                val attributeValue = it.value
                if (attributeValue > highestAttributeTier) {
                    highestAttributeTier = attributeValue
                }
            }

            if (highestAttributeTier > CrimsonConfig.itemAttributeOverlayGRTier && suggestedPrice != null) {
                val price = suggestedPrice.first
                if (price > CrimsonConfig.itemAttributeOverlayGRPrice) {
                    godRoll = true
                }
            }

            if(!godRoll) {
                val itemId = itemStack.getSkyblockId() ?: return
                val godRollCombo = GodRollCombos.keys.firstOrNull { itemId.toLowerCase().contains(it) }?.let { GodRollCombos[it] }

                if (godRollCombo?.first in attributes && godRollCombo?.second in attributes) {
                    godRoll = true
                }
            }

            if (godRoll) {
                GuiUtils.drawText(
                    "GOD",
                    event.x.toFloat(),
                    event.y.toFloat(),
                    GuiUtils.TextStyle.BLACK_OUTLINE,
                    CrimsonConfig.itemAttributeOverlayGodRollTextColor.get(),
                    scale = CrimsonConfig.itemAttributeOverlayTextScale / 100f
                )
                return
            }
        }

        attributes.entries.forEach {
            val attributeName = it.key
            val shortAttributeName = ItemUtils.attributeShortNames[attributeName] ?: attributeName
            val attributeText = "${shortAttributeName}${it.value}"

            GuiUtils.drawText(
                attributeText,
                event.x.toFloat(),
                event.y.toFloat(),
                GuiUtils.TextStyle.BLACK_OUTLINE,
                CrimsonConfig.itemAttributeOverlayTextColor.get(),
                scale = CrimsonConfig.itemAttributeOverlayTextScale / 100f
            )
            event.y += 6 // Adjust y position for each attribute
        }
    }
}