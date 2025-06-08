package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object NearlyCoherentFishingRodRadius {
    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.nearlyCoherentRodRadius) return

        if (Utils.getPlayer()!!.heldItem?.getSkyblockId() != "NEARLY_COHERENT_ROD") return

        RenderUtils.drawFilledCircleWithBorder(
            Utils.getPlayer()!!.positionVector,
            8f,
            72,
            RiftConfig.nearlyCoherentRodRadiusColor.get(),
            RiftConfig.nearlyCoherentRodRadiusColor.get(),
            event.partialTicks
        )
    }
}