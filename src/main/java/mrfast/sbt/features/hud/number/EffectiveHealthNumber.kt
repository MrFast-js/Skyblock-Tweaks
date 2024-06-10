package mrfast.sbt.features.hud.number

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
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
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Effective Health Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val color = if (PlayerStats.effectiveHealth > PlayerStats.maxEffectiveHealth) "§2" else "§a"
            val maxEffectiveHealth = if (GeneralConfig.showMaxEffectiveHealth) "§c/${PlayerStats.maxEffectiveHealth.formatNumber()}" else ""
            val number = "$color${PlayerStats.effectiveHealth.formatNumber()} $maxEffectiveHealth"
            GuiUtils.drawText(number, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
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
