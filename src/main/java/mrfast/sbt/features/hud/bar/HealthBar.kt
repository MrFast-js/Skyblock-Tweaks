package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.healthBarAbsorbColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarBarBorderColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarHealthColor
import mrfast.sbt.managers.LocationManager

@SkyblockTweaks.EventComponent
object HealthBar {
    init {
        HealthBarGui()
    }

    class HealthBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.4297
            this.relativeY = 0.9389
            this.elementName = "Health Bar"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val max = PlayerStats.maxHealth
            val absorption = PlayerStats.absorption
            val health = PlayerStats.displayedHealth
            val total = max + absorption
            val healthFillPerc = health.toDouble() / total
            val absorbFillPerc = absorption.toDouble() / total

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 1f, 1f, 79f, 9f, 4f, healthBarBarBorderColor.get())
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 2f, 2f, 78f, 8f, 3f, healthBarBarColor.get())

            // Draw Health
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(), 2f, 2f,
                (78f * healthFillPerc).toFloat(), 8f, 3f, healthBarHealthColor.get()
            )
            // Draw Absorption
            if (absorption != 0) {
                val fillPixels = (78.0 * absorbFillPerc).toInt() + 3

                UIRoundedRectangle.drawRoundedRectangle(
                    UMatrixStack(), minOf(76, maxOf(2, (78 - fillPixels))).toFloat(), 2f,
                    78f, 8f, 3f, healthBarAbsorbColor.get()
                )
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthBar && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}