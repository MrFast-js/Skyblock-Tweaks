package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.manaNumberColor
import mrfast.sbt.managers.FontManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object ManaNumber {
    init {
        ManaNumberGui()
    }

    class ManaNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.513
            this.relativeY = 0.933
            this.elementName = "Mana Number"
            this.addToList()
            this.height = FontManager.getFontRenderer().FONT_HEIGHT
            this.width = FontManager.getFontRenderer().getStringWidth("12345/12345")
        }

        override fun draw() {
            val number = "${PlayerStats.mana.formatNumber()}/${PlayerStats.maxMana.formatNumber()}"
            val centerX = this.width / 2f
            GuiUtils.drawText(number, centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, manaNumberColor.get(), centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.manaNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}