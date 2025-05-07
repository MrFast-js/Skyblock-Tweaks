package mrfast.sbt.managers

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getItemUUID
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object PaidPriceManager {
    private var pricePaid = JsonObject()
    private val binCostRegex = """Cost: (.*) coins""".toRegex()
    private val aucBidCostRegex = """Your previous bid: (.*) coins""".toRegex()

    init {
        pricePaid = DataManager.getDataDefault("pricePaidMap", JsonObject()) as JsonObject
    }

    private var lastViewedPrice = 0L
    private var lastViewedItem = ""

    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if(event.gui !is GuiChest || !event.gui.chestName().startsWith("Confirm")) return

        if (event.slot.slotNumber == 11 && event.slot.hasStack) {
            if (!event.gui.inventorySlots.getSlot(13).hasStack) return

            val itemBeingBought = event.gui.inventorySlots.getSlot(13).stack!!
            val uuid = itemBeingBought.getItemUUID() ?: return
            val confirmationItem = event.slot.stack
            var cost = -1L

            confirmationItem.getLore(true).forEach {
                if (it.matches(binCostRegex)) {
                    val costString = it.getRegexGroups(binCostRegex)?.get(1)?.value!!
                    cost = costString.replace(",", "").toLongOrNull() ?: 0L
                }
            }
            if (cost == -1L) {
                println("Failed to parse cost from lore: ${confirmationItem.getLore(true)}")
                return
            }
            if (lastViewedItem == uuid) {
                cost += lastViewedPrice
            }

            saveItemPricePaid(uuid, cost)
        }
        if (event.slot.slotNumber == 29 && event.slot.hasStack) {
            if (!event.gui.inventorySlots.getSlot(13).hasStack) return

            val confirmationItem = event.slot.stack
            val itemBeingBought = event.gui.inventorySlots.getSlot(13).stack!!
            val uuid = itemBeingBought.getItemUUID() ?: return

            confirmationItem.getLore(true).forEach {
                if (it.matches(aucBidCostRegex)) {
                    val costString = it.getRegexGroups(aucBidCostRegex)?.get(1)?.value!!
                    lastViewedPrice = costString.replace(",", "").toLongOrNull() ?: 0L
                    lastViewedItem = uuid
                }
            }
        }
    }

    fun getPricePaid(uuid: String): Long? {
        return if (pricePaid.has(uuid)) {
            pricePaid[uuid].asLong
        } else {
            null
        }
    }

    fun pricePaidItemCount(): Int {
        return pricePaid.entrySet().size
    }

    fun clearPricePaid() {
        pricePaid = JsonObject()
        DataManager.saveData("pricePaidMap", pricePaid)
    }

    private fun saveItemPricePaid(uuid: String, price: Long) {
        pricePaid.addProperty(uuid, price)
        DataManager.saveData("pricePaidMap", pricePaid)
    }
}