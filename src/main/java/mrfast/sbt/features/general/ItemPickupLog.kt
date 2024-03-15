package mrfast.sbt.features.general

import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig.itemPickupLog
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs


/*
THIS IS NOT A FEATURE, THIS IS FOR DEBUGGING AND TESTING PURPOSES ONLY
 */
object ItemPickupLog {
    var displayLines = mutableMapOf<String, PickupEntry>()

    class PickupEntry {
        var count: Int = 0
        var lastUpdated: Long = 0
    }

    @SubscribeEvent
    fun onItemGainLoss(event: SkyblockInventoryItemEvent.InventoryItemEvent) {
        if (event is SkyblockInventoryItemEvent.SackItemEvent) {
            val old = displayLines[event.materialName] ?: PickupEntry()
            old.lastUpdated = System.currentTimeMillis()
            old.count += event.amount
            displayLines[event.materialName] = old
        }

        if (event is SkyblockInventoryItemEvent.ItemStackEvent) {
            val old = displayLines[event.itemName] ?: PickupEntry()
            old.lastUpdated = System.currentTimeMillis()
            old.count += event.amount
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
            this.elementName = "Pickup Log Gui"
            this.addToList()
            this.height = 16
        }

        override fun draw() {
            // Clear out logs after 3s
            displayLines = displayLines.filterValues {
                System.currentTimeMillis() - it.lastUpdated < 3000
            } as MutableMap<String, PickupEntry>

            val sorted = displayLines.toList().sortedByDescending { (_, value) -> value.lastUpdated }.toMap()

            var drawnEntries = 0
            for (entry in sorted) {
                if (entry.value.count == 0) continue

                val entryName = entry.key
                val colorSymbol = if (entry.value.count < 0) "§c-" else "§a+"
                val count = abs(entry.value.count).formatNumber()
                val display = "$colorSymbol $count §e$entryName"
                GuiUtils.drawText(display, 0f, (10 * drawnEntries).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
                drawnEntries++
            }
        }

        override fun isActive(): Boolean {
            return itemPickupLog && CustomizationConfig.developerMode
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}