package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig.defenseNumberColor
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber
import java.awt.Color

@SkyblockTweaks.EventComponent
object DefenseNumber {
    init {
        DefenseNumberGui()
    }

    class DefenseNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Defense Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("124,567")
        }

        override fun draw() {
            val centerX = this.width / 2f

            GuiUtils.drawText(PlayerStats.defense.formatNumber(), centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, defenseNumberColor, centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.defenseNumber && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}