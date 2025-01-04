package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object AutoMelonMuncher {
    private var eating = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (Utils.mc.thePlayer == null || !RiftConfig.AutoMelonMuncher) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift") return

        val percent = RiftConfig.autoMelonMuncherPercent / 100.0
        if (PlayerStats.health < PlayerStats.maxHealth * percent && !eating) {
            eating = true
            // Eat Melon
            val hotbar = Utils.mc.thePlayer.inventory.mainInventory.slice(0..8)
            for (itemStack in hotbar) {
                val id = itemStack?.getSkyblockId() ?: continue
                if (id.contains("HEALING_MELON")) {
                    val lastSlot = Utils.mc.thePlayer.inventory.currentItem

                    Utils.mc.thePlayer.inventory.currentItem = hotbar.indexOf(itemStack)

                    Utils.setTimeout({
                        Utils.mc.playerController.sendUseItem(Utils.mc.thePlayer, Utils.mc.theWorld, itemStack)
                    }, RiftConfig.autoMelonMuncherMelonDelay.toLong())

                    Utils.setTimeout({
                        Utils.mc.thePlayer.inventory.currentItem = lastSlot
                    }, RiftConfig.autoMelonMuncherMelonDelay.toLong() + RiftConfig.autoMelonMuncherUseDelay.toLong())

                    Utils.setTimeout({
                        eating = false
                    }, RiftConfig.autoMelonMuncherTotalDelay.toLong())

                    break
                }
            }
        }
    }
}