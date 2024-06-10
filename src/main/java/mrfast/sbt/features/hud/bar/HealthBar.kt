package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.healthBarAbsorbColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarHealthColor
import mrfast.sbt.utils.LocationUtils

object HealthBar {
    init {
        HealthBarGui()
    }

    class HealthBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Health Bar"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val max = PlayerStats.maxHealth
            val absorption = PlayerStats.absorption
            val health = PlayerStats.health - absorption
            val total = max + absorption
            val healthFillPerc = health.toDouble() / total
            val absorbFillPerc = absorption.toDouble() / total

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 6f, healthBarBarColor)

            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(), 2f, 2f,
                (78f * healthFillPerc).toFloat(), 8f, 4f, healthBarHealthColor
            )
            if (absorption != 0) {
                val fillPixels = (78.0 * absorbFillPerc).toInt() + 3

                UIRoundedRectangle.drawRoundedRectangle(
                    UMatrixStack(), minOf(76, maxOf(2, 2 + (78 - fillPixels))).toFloat(), 2f,
                    78f, 8f, 4f, healthBarAbsorbColor
                )
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthBar && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}