package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.LocationManager

@SkyblockTweaks.EventComponent
object RiftTimeBar {
    init {
        RiftBarGui()
    }

    class RiftBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.458
            this.relativeY = 0.806
            this.elementName = "Rift Time Bar"
            this.addToList()
            this.height = 10
            this.width = 80
            this.needsExample = true
        }

        override fun draw() {
            val max = PlayerStats.maxRiftTime
            val riftTime = PlayerStats.riftTimeSeconds
            val timeFillPerc = riftTime.toDouble() / max

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 5f, RiftConfig.riftBarBarColor)

            // Draw normal blue mana
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(),
                2f,
                2f,
                (78f * timeFillPerc).toFloat(),
                8f,
                3f,
                RiftConfig.riftBarFillColor
            )
        }

        override fun drawExample() {
            val max = 10f
            val riftTime = 7f
            val timeFillPerc = riftTime.toDouble() / max

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 5f, RiftConfig.riftBarBarColor)

            // Draw normal blue mana
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(),
                2f,
                2f,
                (78f * timeFillPerc).toFloat(),
                8f,
                3f,
                RiftConfig.riftBarFillColor
            )
        }

        override fun isActive(): Boolean {
            return RiftConfig.riftTimeBar && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return LocationManager.currentIsland == "The Rift"
        }
    }
}