package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.manaOverflowNumberColor
import mrfast.sbt.managers.FontManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object OverflowManaNumber {
    init {
        OverflowManaNumberGui()
    }

    class OverflowManaNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.5773
            this.relativeY = 0.9236
            this.elementName = "Overflow Mana Number"
            this.addToList()
            this.height = FontManager.getFontRenderer().FONT_HEIGHT
            this.width = FontManager.getFontRenderer().getStringWidth("400ʬ")
        }

        override fun draw() {
            val number = "${PlayerStats.overflowMana.formatNumber()}ʬ"
            val centerX = this.width / 2f
            GuiUtils.drawText(
                number,
                centerX,
                0f,
                GuiUtils.TextStyle.BLACK_OUTLINE,
                manaOverflowNumberColor.get(),
                centered = true
            )
        }

        override fun isActive(): Boolean {
            return GeneralConfig.overflowManaNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}