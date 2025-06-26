package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLog
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogItemIds
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogItemPrices
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogTextStyle
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.managers.FontManager
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.abs

@SkyblockTweaks.EventComponent
object ItemPickupLog {
    var displayLines = mutableMapOf<String, PickupEntry>()

    class PickupEntry {
        var count: Int = 0
        var lastUpdated: Long = 0
        var itemId: String = ""

        private val fadeInDuration = GeneralConfig.itemPickupLogFadeIn
        private val fadeOutDuration = GeneralConfig.itemPickupLogFadeOut
        private val totalDuration = GeneralConfig.itemPickupLogTotalTime

        fun timeSinceUpdate(): Long = System.currentTimeMillis() - lastUpdated

        fun isExpired(): Boolean = timeSinceUpdate() >= totalDuration

        fun getAlpha(): Int {
            val elapsed = timeSinceUpdate()

            return when {
                elapsed < fadeInDuration -> {
                    val t = elapsed.toFloat() / fadeInDuration
                    (t * 255).toInt().coerceIn(0, 255)
                }
                elapsed > totalDuration - fadeOutDuration -> {
                    val t = (totalDuration - elapsed).toFloat() / fadeOutDuration
                    (t * 255).toInt().coerceIn(0, 255)
                }
                else -> 255
            }
        }

        fun getVerticalOffset(baseY: Float): Float {
            val elapsed = timeSinceUpdate()

            return when {
                elapsed < fadeInDuration -> {
                    val t = 1f - (elapsed.toFloat() / fadeInDuration)
                    baseY + (10f * t) // Slide up into place
                }
                elapsed > totalDuration - fadeOutDuration -> {
                    val t = (elapsed - (totalDuration - fadeOutDuration)).toFloat() / fadeOutDuration
                    baseY + (10f * t) // Slide down while fading out
                }
                else -> baseY
            }
        }
    }

    // Stop from rift showing every item gain/loss when going between dimensions
    private var changedWorlds = false

    @SubscribeEvent
    fun onLoad(event: WorldLoadEvent) {
        changedWorlds = true
        Utils.setTimeout({
            changedWorlds = false
        }, 3000)
    }

    @SubscribeEvent
    fun onItemGainLoss(event: SkyblockInventoryItemEvent.InventoryItemEvent) {
        if (changedWorlds) return

        if (event is SkyblockInventoryItemEvent.SackItemEvent) {
            val old = displayLines[event.materialName] ?: PickupEntry()
            old.lastUpdated = System.currentTimeMillis()
            old.count += event.amount
            old.itemId = event.itemId
            displayLines[event.materialName] = old
        }

        if (event is SkyblockInventoryItemEvent.ItemStackEvent) {
            if (event.itemName.clean() == "Enchanted Book") {
                event.itemName = event.stack.getLore()[0]
            }
            val old = displayLines[event.itemName] ?: PickupEntry()
            old.lastUpdated = System.currentTimeMillis()
            old.count += event.amount
            old.itemId = event.itemId
            displayLines[event.itemName] = old
        }
    }

    init {
        PickupLogGui()
    }

    class PickupLogGui : GuiManager.Element() {
        init {
            this.relativeX = 0.0
            this.relativeY = 0.0
            this.elementName = "Item Pickup Log"
            this.addToList()
            this.height = FontManager.getFontRenderer().FONT_HEIGHT * 2 + 2
            this.width = 112
            this.needsExample = true
        }

        override fun draw() {
            val now = System.currentTimeMillis()

            // Remove expired entries before drawing
            displayLines = displayLines.filterValues { !it.isExpired() } as MutableMap<String, PickupEntry>

            val sorted = displayLines.toList().sortedByDescending { (_, value) -> value.lastUpdated }

            var drawnEntries = 0
            for ((entryName, entry) in sorted) {
                if (entry.count == 0) continue

                val alpha = entry.getAlpha()
                if (alpha <= 0) continue

                val baseY = 10f * drawnEntries
                val yWithOffset = entry.getVerticalOffset(baseY)

                val colorSymbol = if (entry.count < 0) "§c-" else "§a+"
                val count = abs(entry.count).formatNumber()
                var display = "$colorSymbol$count §e$entryName"

                if (itemPickupLogItemIds) display += " §7${entry.itemId}"
                if (itemPickupLogItemPrices) {
                    val price = ItemUtils.getItemBasePrice(entry.itemId) * entry.count
                    display += " §6$${abs(price).formatNumber()}"
                }

                val style = when (itemPickupLogTextStyle) {
                    "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                    "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                    else -> GuiUtils.TextStyle.DEFAULT
                }

                val color = Color(255, 255, 255, alpha)
                GuiUtils.drawText(display, 0f, yWithOffset, style, color)

                drawnEntries++
            }
        }

        override fun drawExample() {
            val style = when (itemPickupLogTextStyle) {
                "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                else -> GuiUtils.TextStyle.DEFAULT
            }
            GuiUtils.drawText("§a+5 §fPotato §6$16", 0f, 0f, style)
            GuiUtils.drawText("§c-4 §fHay Bale §6$54", 0f, 10f, style)
        }

        override fun isActive(): Boolean {
            return itemPickupLog
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}