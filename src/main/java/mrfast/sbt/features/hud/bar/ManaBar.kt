package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.manaBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarBarBorderColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarManaColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarOverflowColor
import mrfast.sbt.config.categories.GeneralConfig.manaBarShowOverflow
import mrfast.sbt.managers.LocationManager

@SkyblockTweaks.EventComponent
object ManaBar {
    init {
        ManaBarGui()
    }

    class ManaBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.50781
            this.relativeY = 0.94027
            this.elementName = "Mana Bar"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val max = PlayerStats.maxMana
            val mana = PlayerStats.displayedMana
            val overflow = PlayerStats.overflowMana
            val total = max + if (manaBarShowOverflow) overflow else 0
            val manaFillPerc = mana.toDouble() / total
            val overflowFillPerc = overflow.toDouble() / total
            val borderWidth = 2f

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 1f, 1f, 79f, 9f, 4f, manaBarBarBorderColor.get())
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 2f, 2f, 78f, 8f, 3f, manaBarBarColor.get())

            // Draw normal blue mana
            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(),
                borderWidth,
                borderWidth,
                ((80f - borderWidth) * manaFillPerc).toFloat(),
                10f - borderWidth,
                3f,
                manaBarManaColor.get()
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
                    3f,
                    manaBarOverflowColor.get()
                )
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.manaBar && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}