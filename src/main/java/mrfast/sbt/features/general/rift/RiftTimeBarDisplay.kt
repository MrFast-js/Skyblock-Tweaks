package mrfast.sbt.features.general.rift

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.utils.LocationUtils
import net.minecraft.client.gui.Gui

object RiftTimeBarDisplay {
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
            Gui.drawRect(0, 0, 80, 10, RiftConfig.riftBarBarColor.rgb)

            Gui.drawRect(2, 2, (78.0 * timeFillPerc).toInt(), 8, RiftConfig.riftBarFillColor.rgb)
        }

        override fun isActive(): Boolean {
            return RiftConfig.riftTimeBar && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}