package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object MoodyGrappleShotHelper {
    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (LocationManager.currentIsland != "Crimson Isle" || !CrimsonConfig.moodyGrappleShotHighlight) return

        if (Utils.mc.thePlayer.heldItem?.getSkyblockId() != "MOODY_GRAPPLESHOT") return

        val mob = SkyblockMobDetector.getSkyblockMob(event.entity) ?: return
        if (mob.skyblockMobId == "Smoldering Blaze") {
            if (Utils.mc.thePlayer.getDistanceToEntity(mob.skyblockMob) > 17) return
            OutlineUtils.outlineEntity(event, CrimsonConfig.moodyGrappleShotHighlightColor.colorState.get())
        }
    }
}