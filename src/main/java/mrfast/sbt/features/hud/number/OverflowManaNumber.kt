package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.manaOverflowNumberColor
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object OverflowManaNumber {
    init {
        OverflowManaNumberGui()
    }

    class OverflowManaNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Overflow Mana Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("400ʬ")
        }

        override fun draw() {
            val number = "§3${PlayerStats.overflowMana.formatNumber()}ʬ"
            val centerX = this.width / 2f
            GuiUtils.drawText(number, centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, manaOverflowNumberColor, centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.overflowManaNumber && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}