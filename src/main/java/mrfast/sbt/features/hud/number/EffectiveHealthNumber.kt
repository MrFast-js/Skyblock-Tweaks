package mrfast.sbt.features.hud.number

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.effectiveHealthNumberColor
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

object EffectiveHealthNumber {
    init {
        EffectiveHealthNumberGui()
    }

    class EffectiveHealthNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.602
            this.relativeY = 0.980
            this.elementName = "Effective Health Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val maxEffectiveHealth = if (GeneralConfig.showMaxEffectiveHealth) "/${PlayerStats.maxEffectiveHealth.formatNumber()}" else ""
            val number = "§2${PlayerStats.effectiveHealth.formatNumber()}$maxEffectiveHealth"
            GuiUtils.drawText(number, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, effectiveHealthNumberColor)
            this.width = Utils.mc.fontRendererObj.getStringWidth(number) + 1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.effectiveHealthNumber && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}
