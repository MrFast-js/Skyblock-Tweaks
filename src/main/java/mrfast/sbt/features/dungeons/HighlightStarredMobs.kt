package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.DungeonConfig.outlineStarredMobs
import mrfast.sbt.config.categories.DungeonConfig.outlineStarredMobsColor
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.Utils.clean
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@SkyblockTweaks.EventComponent
object HighlightStarredMobs {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityModelEvent) {
        if (!LocationManager.inDungeons || !outlineStarredMobs) return

        val mob = SkyblockMobDetector.getSkyblockMob(event.entity) ?: return
        if (mob.mobNameEntity.customNameTag.clean().startsWith("✯") && event.entity == mob.skyblockMob) {
            OutlineUtils.outlineEntity(event, outlineStarredMobsColor)
        }
    }
}