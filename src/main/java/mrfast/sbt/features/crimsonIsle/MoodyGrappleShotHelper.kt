package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object MoodyGrappleShotHelper {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent.Normal) {
        if (LocationManager.currentIsland != "Crimson Isle" || !CrimsonConfig.moodyGrappleShotHighlight) return

        if (Utils.mc.thePlayer.heldItem?.getSkyblockId() != "MOODY_GRAPPLESHOT") return

        for (mob in SkyblockMobDetector.getLoadedSkyblockMobs()) {
            if (mob.skyblockMobId == "Smoldering Blaze") {
                if (Utils.mc.thePlayer.getDistanceToEntity(mob.skyblockMob) > 17) continue

                event.queueEntityToOutline(mob.skyblockMob, CrimsonConfig.moodyGrappleShotHighlightColor)
            }
        }
    }
}