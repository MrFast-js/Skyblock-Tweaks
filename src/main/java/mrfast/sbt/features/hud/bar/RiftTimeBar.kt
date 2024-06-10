package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.utils.LocationUtils

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
        }

        override fun draw() {
            val max = PlayerStats.maxRiftTime
            val riftTime = PlayerStats.riftTimeSeconds
            val timeFillPerc = riftTime.toDouble() / max

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 6f, RiftConfig.riftBarBarColor)

            // Draw normal blue mana
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(),
                2f,
                2f,
                (78f * timeFillPerc).toFloat(),
                8f,
                4f,
                RiftConfig.riftBarFillColor
            )
        }

        override fun isActive(): Boolean {
            return RiftConfig.riftTimeBar && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return LocationUtils.currentIsland == "The Rift"
        }
    }
}