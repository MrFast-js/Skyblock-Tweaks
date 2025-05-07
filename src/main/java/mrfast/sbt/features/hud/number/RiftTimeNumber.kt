package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.toFormattedDuration

@SkyblockTweaks.EventComponent
object RiftTimeNumber {
    init {
        RiftTimeGui()
    }

    class RiftTimeGui : GuiManager.Element() {
        init {
            this.relativeX = 0.4344
            this.relativeY = 0.8597
            this.elementName = "Rift Time"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("§a00m00sф Left") + 1
            this.needsExample = true
        }

        override fun draw() {
            var remainingTime = (PlayerStats.riftTimeSeconds * 1000L).toFormattedDuration()

            val style = when (RiftConfig.riftTimeTextStyle) {
                "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                else -> GuiUtils.TextStyle.DEFAULT
            }

            if(RiftConfig.riftTimeShowMax) {
                remainingTime += " / ${(PlayerStats.maxRiftTime * 1000L).toFormattedDuration()}"
            }

            val centerX = this.width / 2f
            GuiUtils.drawText(
                remainingTime,
                centerX,
                0f,
                style,
                RiftConfig.riftTimeColor.get(),
                centered = true
            )
        }

        override fun drawExample() {
            var remainingTime = (754 * 1000L).toFormattedDuration()

            val style = when (RiftConfig.riftTimeTextStyle) {
                "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                else -> GuiUtils.TextStyle.DEFAULT
            }

            if(RiftConfig.riftTimeShowMax) {
                remainingTime += " / ${(PlayerStats.maxRiftTime * 1000L).toFormattedDuration()}"
            }

            val centerX = this.width / 2f
            GuiUtils.drawText(
                remainingTime,
                centerX,
                0f,
                style,
                RiftConfig.riftTimeColor.get(),
                centered = true
            )
        }

        override fun isActive(): Boolean {
            return RiftConfig.riftTimeDisplay
        }

        override fun isVisible(): Boolean {
            return LocationManager.currentIsland == "The Rift"
        }
    }
}