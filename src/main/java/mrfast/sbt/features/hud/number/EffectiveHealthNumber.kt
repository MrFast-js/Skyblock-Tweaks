package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.effectiveHealthNumberColor
import mrfast.sbt.managers.FontManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object EffectiveHealthNumber {
    init {
        EffectiveHealthNumberGui()
    }

    class EffectiveHealthNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.559
            this.relativeY = 0.986
            this.elementName = "Effective Health Number"
            this.addToList()
            this.height = FontManager.getFontRenderer().FONT_HEIGHT
            this.width = FontManager.getFontRenderer().getStringWidth("380,000/420,000")
        }

        override fun draw() {
            val maxEffectiveHealth =
                if (GeneralConfig.showMaxEffectiveHealth) "/${PlayerStats.maxEffectiveHealth.formatNumber()}" else ""
            val number = "§2${PlayerStats.effectiveHealth.formatNumber()}$maxEffectiveHealth"
            val centerX = this.width / 2f
            GuiUtils.drawText(
                number,
                centerX,
                0f,
                GuiUtils.TextStyle.BLACK_OUTLINE,
                effectiveHealthNumberColor.get(),
                centered = true
            )
        }

        override fun isActive(): Boolean {
            return GeneralConfig.effectiveHealthNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}
