package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.features.slayers.SlayerManager
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@SkyblockTweaks.EventComponent
object AutoSteakSmoker {
    private var swapping = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (Utils.mc.thePlayer == null || !RiftConfig.AutoSteakSmoker) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || SlayerManager.spawnedSlayer == null) return

        val slayer = SlayerManager.spawnedSlayer!!.skyblockMob as EntityOtherPlayerMP
        val attributeMap = slayer.attributeMap

        // Get specific attributes by name
        val maxHealthAttribute = attributeMap.getAttributeInstance(SharedMonsterAttributes.maxHealth)
        val maxHealth = maxHealthAttribute.baseValue

        val currentHealth = slayer.health

        if (currentHealth < maxHealth * 0.2f) {
            // Eat Melon
            val hotbar = Utils.mc.thePlayer.inventory.mainInventory.slice(0..8)
            for (itemStack in hotbar) {
                val id = itemStack?.getSkyblockId() ?: continue
                if (id.contains("STEAK_STAKE")) {
                    swapping = true
                    Utils.mc.thePlayer.inventory.currentItem = hotbar.indexOf(itemStack)

                    if (RiftConfig.AutoSteakSmokerForce) {
                        swapping = true
                    } else {
                        Utils.setTimeout({
                            swapping = false
                        }, 20_000)
                    }

                    break
                }
            }
        }
    }
}