package mrfast.sbt.features.statDisplays

import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.client.Minecraft

object SpeedDisplay {
    init {
        SpeedDisplayGui()
    }

    class SpeedDisplayGui : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Speed Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val speed = ((Minecraft.getMinecraft().thePlayer?.capabilities?.walkSpeed ?: 0f) * 1000).toFloat();
            val display = "§r${speed.formatNumber()}%"
            GuiUtils.drawText(display, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE)
            this.width = Utils.mc.fontRendererObj.getStringWidth(display)+1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.speedDisplay && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}