package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.client.Minecraft

@SkyblockTweaks.EventComponent
object SpeedNumber {
    init {
        SpeedNumberGui()
    }

    class SpeedNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.37
            this.relativeY = 0.97
            this.elementName = "Speed Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            val speed = ((Minecraft.getMinecraft().thePlayer?.capabilities?.walkSpeed ?: 0f) * 1000)
            val number = "${speed.formatNumber()}%"
            GuiUtils.drawText(number, 0f, 0f, GuiUtils.TextStyle.BLACK_OUTLINE, GeneralConfig.speedNumberColor)
            this.width = Utils.mc.fontRendererObj.getStringWidth(number) + 1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.speedNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}