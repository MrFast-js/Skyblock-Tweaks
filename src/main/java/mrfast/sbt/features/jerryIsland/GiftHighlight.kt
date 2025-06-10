package mrfast.sbt.features.jerryIsland

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.matches
import net.minecraft.block.BlockAir
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color


@SkyblockTweaks.EventComponent
object GiftHighlight {
    private val collectedGifts = mutableListOf<BlockPos>()

    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if(!RenderingConfig.highlightGiftLocations) return

        val armorStands = Utils.getWorld().loadedEntityList.filterIsInstance<EntityArmorStand>()

        armorStands.forEach { armorStand ->
            if(!isGift(armorStand)) return@forEach

            val blockPos = armorStand.position.up()
            val block = Utils.getWorld().getBlockState(blockPos).block
            if(block !is BlockAir) return@forEach

            val vector = armorStand.positionVector.addVector(0.0, 2.0, 0.0)
            val isCollected = collectedGifts.contains(blockPos)
            val color = if (isCollected) RenderingConfig.highlightGiftLocationsCollectedColor else RenderingConfig.highlightGiftLocationsUncollectedColor

            RenderUtils.drawSpecialBB(blockPos, color.get(), event.partialTicks)

            if(!isCollected) {
                RenderUtils.drawLineToPos(
                    vector,
                    2,
                    color.get(),
                    event.partialTicks
                )
            }
        }
    }

    private fun isGift(armorStand: EntityArmorStand): Boolean {
        val headSlot = armorStand.getCurrentArmor(3) ?: return false
        val isPlayerGift = Utils.getWorld().getEntityByID(armorStand.entityId+1)?.displayName?.unformattedText?.clean()?.matches("""To: .*""".toRegex())?: false
        return headSlot.getSkyblockId() == "WHITE_GIFT" && !isPlayerGift
    }

    @SubscribeEvent
    fun onAttack(event: AttackEntityEvent) {
        if(event.target is EntityArmorStand && isGift(event.target as EntityArmorStand)) {
            val armorStand = event.target as EntityArmorStand
            val blockPos = armorStand.position.up()

            if (!collectedGifts.contains(blockPos)) {
                collectedGifts.add(blockPos)
            }
        }
    }

}