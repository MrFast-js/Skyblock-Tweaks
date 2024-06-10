package mrfast.sbt.features.hud.number

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

object ManaNumber {
    init {
        ManaNumberGui()
    }

    class ManaNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Mana Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val number = "§9${PlayerStats.mana.formatNumber()}/${PlayerStats.maxMana.formatNumber()}"
            GuiUtils.drawText(number, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
            this.width = Utils.mc.fontRendererObj.getStringWidth(number) + 1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.manaNumber && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}