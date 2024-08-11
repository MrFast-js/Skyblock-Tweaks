package mrfast.sbt.features.general

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLog
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogItemIds
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogItemPrices
import mrfast.sbt.config.categories.GeneralConfig.itemPickupLogTextStyle
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

@SkyblockTweaks.EventComponent
object ItemPickupLog {
    var displayLines = mutableMapOf<String, PickupEntry>()

    class PickupEntry {
        var count: Int = 0
        var lastUpdated: Long = 0
        var itemId: String = ""
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
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT * 2 + 2
            this.width = 112
            this.needsExample = true
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
                val materialId = entry.value.itemId
                val colorSymbol = if (entry.value.count < 0) "§c-" else "§a+"
                val count = abs(entry.value.count).formatNumber()

                var display = "$colorSymbol$count §e$entryName"

                if (itemPickupLogItemIds) display += " §7$materialId"
                if (itemPickupLogItemPrices) {
                    val info = ItemApi.getItemPriceInfo(materialId) ?: JsonObject()
                    val price = if (info.has("sellPrice")) {
                        info.get("sellPrice").asDouble * entry.value.count
                    } else {
                        (info.get("lowestBin")?.asDouble ?: 0.0) * entry.value.count
                    }

                    display += " §6$${abs(price).formatNumber()}"
                }
                val style = when (itemPickupLogTextStyle) {
                    "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                    "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                    else -> GuiUtils.TextStyle.DEFAULT
                }

                GuiUtils.drawText(display, 0f, (10 * drawnEntries).toFloat(), style)
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