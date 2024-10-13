package mrfast.sbt.features.hud.bar

import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.GeneralConfig.drillFuelBarBarColor
import mrfast.sbt.config.categories.GeneralConfig.drillFuelBarColor
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils

@SkyblockTweaks.EventComponent
object DrillFuelBar {
    init {
        DrillFuelBarGui()
    }

    class DrillFuelBarGui : GuiManager.Element() {
        init {
            this.relativeX = 0.459375
            this.relativeY = 0.852778
            this.elementName = "Drill Fuel Bar"
            this.addToList()
            this.height = 10
            this.width = 80
        }

        override fun draw() {
            val healthFillPerc = PlayerStats.drillFuel.toDouble() / PlayerStats.maxDrillFuel

            // Draw background/border
            UIRoundedRectangle.drawRoundedRectangle(UMatrixStack(), 0f, 0f, 80f, 10f, 5f, drillFuelBarBarColor)

            UIRoundedRectangle.drawRoundedRectangle(
                UMatrixStack(), 2f, 2f,
                (78f * healthFillPerc).toFloat(), 8f, 3f, drillFuelBarColor
            )
        }

        override fun isActive(): Boolean {
            return GeneralConfig.drillFuelBar && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            val heldItemName = Utils.mc.thePlayer?.heldItem?.getSkyblockId() ?: ""
            return heldItemName.contains("MITHRIL_DRILL") || heldItemName.contains("GEMSTONE_DRILL")
        }
    }
}