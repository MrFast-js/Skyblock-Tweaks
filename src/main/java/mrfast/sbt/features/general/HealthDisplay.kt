package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.Categories.GeneralConfig
import mrfast.sbt.config.Categories.OverlaysConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils

object HealthDisplay {
    init {
        HealthDisplayGui()
    }

    class HealthDisplayGui : GuiManager.Element() {
        init {
            this.relativeX = 0.3729
            this.relativeY = 0.8629
            this.elementName = "Health Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val display = "§c${Utils.formatNumber(PlayerStats.health)}/${Utils.formatNumber(PlayerStats.maxHealth)}"
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
            this.width = Utils.mc.fontRendererObj.getStringWidth(display)+1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthDisplay
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}