package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.Categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.LocationUtils
import net.minecraft.client.gui.Gui
import java.awt.Color

object ManaBarDisplay {
    init {
        ManaBarGui()
    }

    class ManaBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Mana Bar Display"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val max = PlayerStats.maxMana
            val mana = PlayerStats.mana
            val overflow = PlayerStats.overflowMana
            val total = max + overflow
            val manaFillPerc = mana.toDouble() / total
            val overflowFillPerc = overflow.toDouble() / total

            val manaColor = Color(0x5555FF)
            val overflowColor = Color(0x55FFFF)

            // Draw background/border
            Gui.drawRect(0, 0, 80, 10, Color.BLACK.rgb)

            // Draw normal blue mana
            Gui.drawRect(2, 2, (78.0 * manaFillPerc).toInt(), 8, manaColor.rgb)
            // Draw extra cyan overflow
            if (overflow != 0) {
                val fillPixels = (78.0 * overflowFillPerc).toInt() + 3
                Gui.drawRect(
                    minOf(76, maxOf(2, 2 + (78 - fillPixels))),
                    2, 78, 8, overflowColor.rgb
                )
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.manaBar && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}