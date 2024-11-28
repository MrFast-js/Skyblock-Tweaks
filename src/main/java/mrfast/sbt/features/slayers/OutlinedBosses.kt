package mrfast.sbt.features.slayers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.SlayerConfig
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.customevents.SlayerEvent
import mrfast.sbt.utils.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
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

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if(SlayerManager.spawnedSlayer != null && SlayerConfig.highlightSlayerBosses) {
            val slayer = SlayerManager.spawnedSlayer!!.skyblockMob
            val skullPos = slayer.positionVector.add(Vec3(0.0, slayer.eyeHeight.toDouble(), 0.0))
            val playerPos = Utils.mc.thePlayer.getPositionEyes(event.partialTicks)

            if(!Utils.mc.thePlayer.canEntityBeSeen(slayer)) return

            GlStateManager.disableDepth()
            if (SlayerConfig.highlightSlayerBosses) {
                RenderUtils.drawLine(
                    playerPos,
                    skullPos,
                    2,
                    SlayerConfig.slayerBossColor.get(),
                    event.partialTicks
                )
            }
            GlStateManager.enableDepth()
        }
    }
}