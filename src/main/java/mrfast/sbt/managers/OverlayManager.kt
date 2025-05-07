package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.SignDrawnEvent
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object OverlayManager {
    private var containerOverlays = mutableListOf<Overlay>()
    private var signOverlays = mutableListOf<Overlay>()

    @SubscribeEvent
    fun onContainerDrawn(event: GuiContainerBackgroundDrawnEvent) {
        containerOverlays.filter { it.isActive(event) }.forEach {
            if (!it.leftAlign) {
                GlStateManager.translate(180f + it.x, it.y, 0.0)
                it.draw(event.mouseX, event.mouseY, event)
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.translate(-(180f + it.x), -it.y, 0.0)
            }
        }
    }

    @SubscribeEvent
    fun onSignDrawn(event: SignDrawnEvent) {
        for (overlay in signOverlays) {
            if (!overlay.isActive(event)) continue

            if (!overlay.leftAlign) {
                GlStateManager.translate(overlay.x, overlay.y, 0.0)
                overlay.draw(event.mouseX, event.mouseY, event)
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
                GlStateManager.translate(-(overlay.x), -overlay.y, 0.0)
            }
        }
    }

    enum class OverlayType {
        CHEST,
        SIGN
    }

    open class Overlay {
        var x: Double = 0.0
        var y: Double = 0.0
        var leftAlign = false
        var height = 1
        var width = 1

        fun addToList(type: OverlayType) {
            if (type == OverlayType.CHEST) {
                containerOverlays.add(this)
            }
            if (type == OverlayType.SIGN) {
                signOverlays.add(this)
            }
        }

        open fun draw(mouseX: Int, mouseY: Int, event: Event) {

        }

        open fun isActive(event: Event): Boolean {
            return false
        }
    }
}