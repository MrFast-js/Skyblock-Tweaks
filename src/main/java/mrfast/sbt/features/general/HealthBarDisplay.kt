package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.LocationUtils
import net.minecraft.client.gui.Gui
import java.awt.Color

object HealthBarDisplay {
    init {
        HealthBarGui()
    }

    class HealthBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Health Bar Display"
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

            val healthColor = Color.RED
            val absorbColor = Color(0xFFAA00)

            // Draw background/border
            Gui.drawRect(0, 0, 80, 10, Color.BLACK.rgb)

            Gui.drawRect(2, 2, (78.0 * healthFillPerc).toInt(), 8, healthColor.rgb)
            if (absorption != 0) {
                val fillPixels = (78.0 * absorbFillPerc).toInt() + 3
                Gui.drawRect(
                    minOf(76, maxOf(2, 2 + (78 - fillPixels))),
                    2, 78, 8, absorbColor.rgb
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