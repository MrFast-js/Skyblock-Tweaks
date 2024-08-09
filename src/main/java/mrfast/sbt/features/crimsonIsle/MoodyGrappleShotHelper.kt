package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object MoodyGrappleShotHelper {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent.Normal) {
        if (LocationUtils.currentIsland != "Crimson Isle" || CrimsonConfig.moodyGrappleShotHighlight) return

        if(Utils.mc.thePlayer.heldItem?.getSkyblockId() != "MOODY_GRAPPLESHOT") return

        for (mob in SkyblockMobDetector.getLoadedSkyblockMobs()) {
            if(mob.skyblockMobId == "Smoldering Blaze") {
                if(Utils.mc.thePlayer.getDistanceToEntity(mob.skyblockMob) > 17) continue

                event.queueEntityToOutline(mob.skyblockMob, CrimsonConfig.moodyGrappleShotHighlightColor)
            }
        }
    }
}