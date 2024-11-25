package mrfast.sbt.features.slayers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.SlayerConfig
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object OutlinedBosses {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityModelEvent) {
        if (Utils.mc.theWorld == null || !SlayerConfig.highlightSlayerBosses) return

        val sbMob = SkyblockMobDetector.getSkyblockMob(event.entity) ?: return
        if (sbMob.skyblockMob == event.entity && sbMob.skyblockMobId != null) {
            if (SlayerManager.isMiniboss(sbMob)) {
                OutlineUtils.outlineEntity(event, SlayerConfig.miniBossColor.get())
            }

            val id = sbMob.skyblockMobId ?: return
            if (id.endsWith("Slayer")) {
                if (id.contains("Voidgloom")) {
                    val hitPhase = sbMob.mobNameEntity.customNameTag.contains("Hits")
                    val laserPhase = sbMob.skyblockMob.isRiding
                    val color =
                        if (laserPhase) SlayerConfig.voidgloomLaserPhase else if (hitPhase) SlayerConfig.voidgloomHitsPhase else SlayerConfig.slayerBossColor
                    OutlineUtils.outlineEntity(event, color.get())
                } else {
                    OutlineUtils.outlineEntity(event, SlayerConfig.slayerBossColor.get())
                }
            }
        }
    }
}