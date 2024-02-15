package mrfast.sbt.features.general

import mrfast.sbt.config.categories.GeneralConfig
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object HideHotbarElements {
    @SubscribeEvent
    fun onRenderElement(event: RenderGameOverlayEvent.Pre) {
        if (GeneralConfig.cleanerHotbarArea && shouldHide(event.type)) {
            event.isCanceled = true
        }
    }

    private fun shouldHide(type:ElementType):Boolean {
        return  type == ElementType.ARMOR && GeneralConfig.hideArmorBar ||
                type == ElementType.HEALTH && GeneralConfig.hideHealthHearts ||
                type == ElementType.FOOD && GeneralConfig.hideHungerBar
    }
}