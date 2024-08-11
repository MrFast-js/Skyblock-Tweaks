package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object HealthNumber {
    init {
        HealthNumberGui()
    }

    class HealthNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Health Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("12345/12345")
        }

        override fun draw() {
            val color = if (PlayerStats.health > PlayerStats.maxHealth) {
                GeneralConfig.healthDisplayAbsorptionColor
            } else {
                GeneralConfig.healthDisplayColor
            }

            val maxHealth = if (GeneralConfig.showMaxHealth) "/${PlayerStats.maxHealth.formatNumber()}" else ""
            val number = "${PlayerStats.health.formatNumber()}$maxHealth"

            val centerX = this.width / 2f
            GuiUtils.drawText(number, centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, color, centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}