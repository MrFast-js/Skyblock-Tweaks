package mrfast.sbt.features.generalProfitTracker

import mrfast.sbt.customevents.SkyblockInventoryItemEvent
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
    var selectedFilterMode = "Whitelist"
    val whitelistItems = mutableListOf(
        "IRON_SWORD",
        "HYPERION",
        "ROTTEN_FLESH",
        "BONE",
        "SUPERBOOM_TNT",
        "NEW_YEAR_CAKE",
        "JUJU_SHORTBOW",
        "ADD_ITEM"
    )
    val blacklistItems = mutableListOf(
        "IRON_SWORD",
        "HYPERION",
        "ROTTEN_FLESH",
        "BONE",
        "SUPERBOOM_TNT",
        "NEW_YEAR_CAKE",
        "JUJU_SHORTBOW"
    )

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