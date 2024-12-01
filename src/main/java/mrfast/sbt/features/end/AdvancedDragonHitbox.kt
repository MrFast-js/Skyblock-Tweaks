package mrfast.sbt.features.end

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.Utils
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object AdvancedDragonHitbox {
    @SubscribeEvent
    fun onRenderPart(event: RenderEntityModelEvent) {
        if (!LocationManager.inSkyblock || Utils.mc.theWorld == null || !RenderingConfig.highlightDragon) return

        if (event.entity !is EntityDragon) return

        OutlineUtils.outlineEntity(event, RenderingConfig.highlightDragonColor.get())
    }
}