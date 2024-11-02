package mrfast.sbt.features.profitTracking

import com.google.gson.JsonArray
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.PurseChangeEvent
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.ItemUtils.getSkyblockEnchants
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object ProfitTracker {
    var itemsGainedDuringSession = mutableMapOf<String, Int>()
    var sessionStartedAt = System.currentTimeMillis()
    var started = false
    var purseGainLoss = 0
    var paused = false
    var pausedDuration: Long = 0
    var selectedFilterMode = "Blacklist"

    var whitelistItems = mutableListOf(
        "ADD_ITEM"
    )
    var blacklistItems = mutableListOf(
        "ADD_ITEM"
    )

    @SubscribeEvent
    fun onPurseChange(event: PurseChangeEvent) {
        if (!started || paused) return

        purseGainLoss += event.amount
    }

    @SubscribeEvent
    fun onProfileConfigLoad(event: ProfileLoadEvent) {
        val wL = DataManager.getDataDefault("GPTwhitelist", JsonArray()) as JsonArray
        val bL = DataManager.getDataDefault("GPTblacklist", JsonArray()) as JsonArray

        blacklistItems.clear()
        whitelistItems.clear()
        for (item in wL) whitelistItems.add(item.asString)
        for (item in bL) blacklistItems.add(item.asString)

        if (!whitelistItems.contains("ADD_ITEM")) whitelistItems.add("ADD_ITEM")
        if (!blacklistItems.contains("ADD_ITEM")) blacklistItems.add("ADD_ITEM")
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (paused && started) {
            // Add 50ms every tick
            pausedDuration += 50
        }
    }

    @SubscribeEvent
    fun onItemGainLoss(event: SkyblockInventoryItemEvent.InventoryItemEvent) {
        if (!started || paused) return

        // Stop from counting gains from items pulled/put into chests
        if (Utils.mc.currentScreen != null) {
            if (Utils.mc.currentScreen !is GuiInventory) {
                return
            }
        }

        if (filterOutItem(event.itemId)) return

        if (event is SkyblockInventoryItemEvent.SackItemEvent) {
            val lastCount = itemsGainedDuringSession[event.itemId] ?: 0
            itemsGainedDuringSession[event.itemId] = lastCount + event.amount

            if (itemsGainedDuringSession[event.itemId]!! == 0) {
                itemsGainedDuringSession.remove(event.itemId)
            }
        }

        if (event is SkyblockInventoryItemEvent.ItemStackEvent) {
            val id = getCustomItemId(event)
            if (filterOutItem(id)) return

            val lastCount = itemsGainedDuringSession[id] ?: 0
            itemsGainedDuringSession[id] = lastCount + event.amount

            if (itemsGainedDuringSession[id]!! == 0) {
                itemsGainedDuringSession.remove(id)
            }
        }
    }

    fun filterOutItem(id: String): Boolean {
        if (selectedFilterMode == "Whitelist") {
            if (!whitelistItems.contains(id)) return true
        } else {
            if (blacklistItems.contains(id)) return true
        }
        return false
    }

    private fun getCustomItemId(event: SkyblockInventoryItemEvent.ItemStackEvent): String {
        var id = event.itemId

        if (id == "ENCHANTED_BOOK") {
            val enchants = event.stack.getSkyblockEnchants()
            for (enchant in enchants) {
                id = enchant.key.uppercase() + ";" + enchant.value
                break
            }
        }
        return id
    }
}
