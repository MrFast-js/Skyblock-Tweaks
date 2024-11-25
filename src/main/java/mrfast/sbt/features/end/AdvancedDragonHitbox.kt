package mrfast.sbt.features.end

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.entity.boss.EntityDragon
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object AdvancedDragonHitbox {
    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || Utils.mc.theWorld == null || !RenderingConfig.advancedDragonHitbox) return

        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if(entity !is EntityDragon) continue

            for (entityDragonPart in entity.dragonPartArray) {
                val aabb = AxisAlignedBB(
                    entityDragonPart.posX,
                    entityDragonPart.posY,
                    entityDragonPart.posZ,
                    entityDragonPart.posX + entityDragonPart.width,
                    entityDragonPart.posY + entityDragonPart.height,
                    entityDragonPart.posZ + entityDragonPart.width
                )
                RenderUtils.drawSpecialBB(
                    aabb,
                    RenderingConfig.advancedDragonHitboxColor.get(),
                    event.partialTicks
                )
            }
        }
    }
}