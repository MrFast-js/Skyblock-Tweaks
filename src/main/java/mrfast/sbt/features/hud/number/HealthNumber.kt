package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.FontManager
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object HealthNumber {
    init {
        HealthNumberGui()
    }

    class HealthNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.4359
            this.relativeY = 0.9166
            this.elementName = "Health Number"
            this.addToList()
            this.height = FontManager.getFontRenderer().FONT_HEIGHT
            this.width = FontManager.getFontRenderer().getStringWidth("12345/12345")
        }

        override fun draw() {
            val color = if (PlayerStats.absorption != 0) {
                GeneralConfig.healthDisplayAbsorptionColor
            } else {
                GeneralConfig.healthDisplayColor
            }

            val maxHealth = if (GeneralConfig.showMaxHealth) "/${PlayerStats.maxHealth.formatNumber()}" else ""
            val number = "${(PlayerStats.displayedHealth+PlayerStats.absorption).formatNumber()}$maxHealth"

            val centerX = this.width / 2f
            GuiUtils.drawText(number, centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, color.get(), centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}