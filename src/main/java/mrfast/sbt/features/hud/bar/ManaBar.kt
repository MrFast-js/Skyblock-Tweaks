package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.manaBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarManaColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarOverflowColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarShowOverflow
import mrfast.sbt.utils.LocationUtils

@SkyblockTweaks.EventComponent
object ManaBar {
    init {
        ManaBarGui()
    }

    class ManaBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Mana Bar"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val max = PlayerStats.maxMana
            val mana = PlayerStats.mana
            val overflow = PlayerStats.overflowMana
            val total = max + if (manaBarShowOverflow) overflow else 0
            val manaFillPerc = mana.toDouble() / total
            val overflowFillPerc = overflow.toDouble() / total
            val borderWidth = 2f

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 6f, manaBarBarColor)

            // Draw normal blue mana
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(),
                borderWidth,
                borderWidth,
                ((80f - borderWidth) * manaFillPerc).toFloat(),
                10f - borderWidth,
                4f,
                manaBarManaColor
            )

            // Draw extra cyan overflow
            if (overflow != 0 && manaBarShowOverflow) {
                val fillPixels = ((80f - borderWidth) * overflowFillPerc).toInt() + 3

                UIRoundedRectangle.drawRoundedRectangle(
                    UMatrixStack(),
                    minOf(
                        (80f - borderWidth).toInt(),
                        maxOf(borderWidth, borderWidth + ((80f - borderWidth) - fillPixels)).toInt()
                    ).toFloat(),
                    2f,
                    (80 - borderWidth),
                    (10 - borderWidth),
                    4f,
                    manaBarOverflowColor
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