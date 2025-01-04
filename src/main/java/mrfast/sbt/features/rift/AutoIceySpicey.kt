package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@SkyblockTweaks.EventComponent
object AutoIceySpicey {
    private var activating = false

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Received) {
        if (Utils.mc.thePlayer == null || !RiftConfig.AutoIceySpicy) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift") return

        if (event.packet is S29PacketSoundEffect) {
            if(event.packet.soundName.contains("note.pling") && event.packet.pitch == 2.095f) {
                activating = true
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (Utils.mc.thePlayer == null || !RiftConfig.AutoIceySpicy) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift") return

        if (activating) {
            val hotbar = Utils.mc.thePlayer.inventory.mainInventory.slice(0..8)
            activating = false

            for (itemStack in hotbar) {
                val id = itemStack?.getSkyblockId() ?: continue
                if (id.contains("HOLY_ICE")) {
                    val lastSlot = Utils.mc.thePlayer.inventory.currentItem

                    Utils.mc.thePlayer.inventory.currentItem = hotbar.indexOf(itemStack)

                    Utils.setTimeout({
                        Utils.mc.playerController.sendUseItem(Utils.mc.thePlayer, Utils.mc.theWorld, itemStack)
                    }, RiftConfig.autoIceySpicySwapDelay.toLong())

                    Utils.setTimeout({
                        Utils.mc.thePlayer.inventory.currentItem = lastSlot
                    }, RiftConfig.autoIceySpicySwapDelay.toLong() + RiftConfig.autoIceySpicyUseDelay.toLong())

                    Utils.setTimeout({
                        activating = false
                    }, RiftConfig.autoIceySpicyTotalDelay.toLong())

                    break
                }
            }
        }
    }
}