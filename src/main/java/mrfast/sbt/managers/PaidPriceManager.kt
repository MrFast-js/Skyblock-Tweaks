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
    private val binCostRegex = """Price: (.*) coins""".toRegex()
    private val aucBidCostRegex = """New bid: (.*) coins""".toRegex()

    init {
        pricePaid = DataManager.getDataDefault("pricePaidMap", JsonObject()) as JsonObject
    }

    private var lastViewedPrice = -1L
    private var lastViewedItem = ""

    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if(event.gui !is GuiChest) return

        if (event.slot.slotNumber == 11 && event.slot.hasStack) {
            if (!event.gui.inventorySlots.getSlot(13).hasStack) return

            val itemBeingBought = event.gui.inventorySlots.getSlot(13).stack!!
            val uuid = itemBeingBought.getItemUUID() ?: return
            val cost = lastViewedPrice

            if (cost == -1L) return

            saveItemPricePaid(uuid, cost)
        }

        if (event.gui.chestName().contains("Auction View")) {
            val itemBeingBought = event.gui.inventorySlots.getSlot(13).stack ?: return
            val uuid = itemBeingBought.getItemUUID() ?: return

            val binItem = event.gui.inventorySlots.getSlot(31).stack
            val aucItem = event.gui.inventorySlots.getSlot(29).stack
            lastViewedPrice = -1L

            binItem.getLore(true).forEach {
                if (it.matches(binCostRegex)) {
                    val costString = it.getRegexGroups(binCostRegex)?.get(1)?.value!!
                    lastViewedPrice = costString.replace(",", "").toLongOrNull() ?: 0L
                    lastViewedItem = uuid
                }
            }

            aucItem.getLore(true).forEach {
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