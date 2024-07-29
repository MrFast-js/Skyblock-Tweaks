package mrfast.sbt.features.slayers

import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.SlayerConfig
import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.utils.Utils
import net.minecraft.entity.Entity
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object GlowingBosses {
    @SubscribeEvent
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent.Normal) {
        if (Utils.mc.theWorld == null || !SlayerConfig.highlightSlayerBosses) return

        for (entity: Entity in Utils.mc.theWorld.loadedEntityList) {
            val sbMob = SkyblockMobDetector.getSkyblockMob(entity) ?: continue
            if (sbMob.skyblockMob == entity && sbMob.skyblockMobId != null) {
                if (SlayerManager.isMiniboss(sbMob)) {
                    event.queueEntityToOutline(sbMob.skyblockMob, SlayerConfig.miniBossColor)
                }

                val id = sbMob.skyblockMobId ?: continue
                if (id.endsWith("Slayer")) {
                    if (id.contains("Voidgloom")) {
                        val hitPhase = sbMob.mobNameEntity.customNameTag.contains("Hits")
                        val laserPhase = sbMob.skyblockMob.isRiding
                        val color = if (laserPhase) SlayerConfig.voidgloomLaserPhase else if (hitPhase) SlayerConfig.voidgloomHitsPhase else SlayerConfig.slayerBossColor
                        event.queueEntityToOutline(sbMob.skyblockMob, color)
                    } else {
                        event.queueEntityToOutline(sbMob.skyblockMob, SlayerConfig.slayerBossColor)
                    }
                }
            }
        }
    }
}