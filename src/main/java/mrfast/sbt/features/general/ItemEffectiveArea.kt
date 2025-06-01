package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.config.categories.RenderingConfig.showItemEffectiveAreaColor
import mrfast.sbt.config.categories.RenderingConfig.showItemEffectiveAreaMobsNearby
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/*
@PotentialFeature: Swap to enums and make into extensive feature with many items
 */
@SkyblockTweaks.EventComponent
object ItemEffectiveArea {
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || !RenderingConfig.showItemEffectiveArea) return

        val heldItemId = Utils.mc.thePlayer.heldItem?.getSkyblockId() ?: return

        val items = listOf(
            "BAT_WAND",
            "STARRED_BAT_WAND",
            "HYPERION",
            "ASTRAEA",
            "SCYLLA",
            "VALKYRIE",
        )

        if (heldItemId in items) {
            val lookingBlock = Utils.mc.thePlayer.rayTrace(if(heldItemId == "HYPERION") 9.0 else 45.0, event.partialTicks)
            if (lookingBlock.blockPos != null && lookingBlock.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (!showItemEffectiveAreaMobsNearby || SkyblockMobDetector.getLoadedSkyblockMobs()
                        .any { it.skyblockMobId != null && it.skyblockMob.positionVector.distanceTo(lookingBlock.hitVec) < 7 }
                ) {
                    RenderUtils.drawFilledCircleWithBorder(
                        Vec3(lookingBlock.blockPos).addVector(0.5, 1.0, 0.5),
                        7f,
                        72,
                        showItemEffectiveAreaColor.get(),
                        showItemEffectiveAreaColor.get(),
                        event.partialTicks
                    )
                }
            }
        }
    }

//    enum class RenderType {
//        LOOKING_CIRCLE,
//        PLAYER_CIRCLE
//    }
//    enum class MainItems() {
//        FIRE_VEIL_WAND("FIRE_VEIL_WAND", 3f),
//        FIRE_FREEZE_STAFF("FIRE_FREEZE_STAFF", 5f),

//        HYPERION("HYPERION", 7f),
//        BONZO_STAFF("BONZO_STAFF", 4f)
//        SPIRIT_SCEPTER("BAT_WAND", 7f),
//        SPIRIT_SCEPTER_STAR("STARRED_BAT_WAND", 7f)
//        JERRY_GUN("JERRY_STAFF", 2f)
//        DREADLORD_SWORD("DREADLORD_SWORD", 2f)
//        EXPLOSIVE_BOW("EXPLOSIVE_BOW", 4f)
//        FLORIDE_ZOMBIE_SWORD("FLORIDE_ZOMBIE_SWORD", 8f)
//        AURORA_STAFF("AURORA_STAFF", 4f)
//        GOLEM_SWORD("GOLEM_SWORD", 4f)
//        GIANTS_SWORD("GIANTS_SWORD", 8f)
//        GYRO_WAND("GYROKINETIC_WAND", 8f)
//        WEIRD_TUBA("WEIRD_TUBA", 8f)
//        VAMP_MASK("VAMPIRE_WITCH_MASK", 8f)
//    }
}