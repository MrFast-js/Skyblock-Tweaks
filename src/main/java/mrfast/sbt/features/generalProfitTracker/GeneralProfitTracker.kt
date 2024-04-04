package mrfast.sbt.features.generalProfitTracker

import com.google.gson.JsonArray
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object GeneralProfitTracker {
    var itemsGainedDuringSession = mutableMapOf<String, Int>()
    var sessionStartedAt = System.currentTimeMillis()
    var started = false
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
    fun onProfileConfigLoad(event: ProfileLoadEvent) {
        val wL = DataManager.getProfileDataDefault("GPTwhitelist",JsonArray()) as JsonArray
        val bL = DataManager.getProfileDataDefault("GPTblacklist",JsonArray()) as JsonArray

        blacklistItems.clear()
        whitelistItems.clear()
        for (item in wL) whitelistItems.add(item.asString)
        for (item in bL) blacklistItems.add(item.asString)

        if(!whitelistItems.contains("ADD_ITEM")) whitelistItems.add("ADD_ITEM")
        if(!blacklistItems.contains("ADD_ITEM")) blacklistItems.add("ADD_ITEM")
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
        if (selectedFilterMode == "Whitelist") {
            if (!whitelistItems.contains(event.itemId)) return
        } else {
            if (blacklistItems.contains(event.itemId)) return
        }
        if (event is SkyblockInventoryItemEvent.SackItemEvent) {
            val lastCount = itemsGainedDuringSession[event.itemId] ?: 0
            itemsGainedDuringSession[event.itemId] = lastCount + event.amount
        }

        if (event is SkyblockInventoryItemEvent.ItemStackEvent) {
            val lastCount = itemsGainedDuringSession[event.itemId] ?: 0
            itemsGainedDuringSession[event.itemId] = lastCount + event.amount
        }

        if (itemsGainedDuringSession[event.itemId]!! <= 0) {
            itemsGainedDuringSession.remove(event.itemId)
        }
    }
}