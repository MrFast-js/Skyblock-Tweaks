package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

object EffectiveHealthDisplay {
    init {
        EffectiveHealthDisplayGui()
    }

    class EffectiveHealthDisplayGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Effective Health Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val display = "§2${PlayerStats.effectiveHealth.formatNumber()}"
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
            this.width = Utils.mc.fontRendererObj.getStringWidth(display)+1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.effectiveHealthDisplay && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}