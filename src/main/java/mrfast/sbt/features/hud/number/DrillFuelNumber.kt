package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.drillFuelDisplayColor
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber

@SkyblockTweaks.EventComponent
object DrillFuelNumber {
    init {
        DrillFuelNumberGui()
    }

    class DrillFuelNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.459375
            this.relativeY = 0.852778
            this.elementName = "Drill Fuel Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
            this.width = Utils.mc.fontRendererObj.getStringWidth("20000/20000 Drill Fuel")
        }

        override fun draw() {
            val display = "${PlayerStats.drillFuel.formatNumber()}/${PlayerStats.maxDrillFuel.formatNumber()} Drill Fuel"
            val centerX = this.width / 2f
            GuiUtils.drawText(display, centerX, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, drillFuelDisplayColor, centered = true)
        }

        override fun isActive(): Boolean {
            return GeneralConfig.drillFuelDisplay && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            val heldItemName = Utils.mc.thePlayer?.heldItem?.getSkyblockId() ?: ""
            return heldItemName.contains("MITHRIL_DRILL") || heldItemName.contains("GEMSTONE_DRILL")
        }
    }
}