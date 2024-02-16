package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.healthBarAbsorbColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.healthBarHealthColor
import mrfast.sbt.utils.LocationUtils
import net.minecraft.client.gui.Gui

object HealthBarDisplay {
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
            Gui.drawRect(0, 0, 80, 10, healthBarBarColor.rgb)

            Gui.drawRect(2, 2, (78.0 * healthFillPerc).toInt(), 8, healthBarHealthColor.rgb)
            if (absorption != 0) {
                val fillPixels = (78.0 * absorbFillPerc).toInt() + 3
                Gui.drawRect(
                    minOf(76, maxOf(2, 2 + (78 - fillPixels))),
                    2, 78, 8, healthBarAbsorbColor.rgb
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