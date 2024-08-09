package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.DungeonConfig.glowingStarredMobs
import mrfast.sbt.config.categories.DungeonConfig.glowingStarredMobsColor
import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils.clean
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@SkyblockTweaks.EventComponent
object HighlightStarredMobs {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent.Normal) {
        if (!LocationUtils.inDungeons || !glowingStarredMobs) return

        for (mob in SkyblockMobDetector.getLoadedSkyblockMobs()) {
            if (mob.mobNameEntity.customNameTag.clean().startsWith("✯")) {
                event.queueEntityToOutline(mob.skyblockMob, glowingStarredMobsColor)
            }
        }
    }
}