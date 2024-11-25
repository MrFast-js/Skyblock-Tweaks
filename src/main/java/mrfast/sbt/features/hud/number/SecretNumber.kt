package mrfast.sbt.features.hud.number

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import java.awt.Color

@SkyblockTweaks.EventComponent
object SecretNumber {
    init {
        SecretNumberGui()
    }

    class SecretNumberGui : GuiManager.Element() {
        init {
            this.relativeX = 0.392
            this.relativeY = 0.93
            this.elementName = "Secret Number"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT * 2
            this.width = Utils.mc.fontRendererObj.getStringWidth("§7Secrets") + 1
            this.needsExample = true
        }

        override fun draw() {
            if (PlayerStats.currentRoomSecrets == -1) return

            val secretCount = styleSecretCount(PlayerStats.currentRoomSecrets, PlayerStats.currentRoomMaxSecrets)
            val style = when (GeneralConfig.secretNumberTextStyle) {
                "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                else -> GuiUtils.TextStyle.DEFAULT
            }

            secretCount.forEachIndexed { i, it ->
                GuiUtils.drawText(
                    it,
                    this.width / 2f,
                    Utils.mc.fontRendererObj.FONT_HEIGHT * i.toFloat(),
                    style,
                    Color(0xFFFFFF),
                    centered = true
                )
            }
            this.width = Utils.mc.fontRendererObj.getStringWidth("§7Secrets") + 1
        }

        override fun drawExample() {
            val secretCount = listOf("§7Secrets", "§a1§7/§c9")
            val style = when (GeneralConfig.secretNumberTextStyle) {
                "Shadowed" -> GuiUtils.TextStyle.DROP_SHADOW
                "Outlined" -> GuiUtils.TextStyle.BLACK_OUTLINE
                else -> GuiUtils.TextStyle.DEFAULT
            }

            secretCount.forEachIndexed { i, it ->
                GuiUtils.drawText(
                    it,
                    this.width / 2f,
                    Utils.mc.fontRendererObj.FONT_HEIGHT * i.toFloat(),
                    style,
                    GeneralConfig.speedNumberColor.get(),
                    centered = true
                )
            }
        }

        override fun isActive(): Boolean {
            return GeneralConfig.secretNumber && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return LocationManager.inDungeons
        }
    }

    private fun styleSecretCount(secrets: Int, maxSecrets: Int): List<String> {
        val text = ArrayList<String>()

        val color: String = when {
            secrets == maxSecrets -> "§a"
            secrets > maxSecrets / 2 -> "§e"
            else -> "§c"
        }

        text.add("§7Secrets")

        if (secrets == -1) {
            text.add("§7None")
        } else {
            text.add("$color$secrets§7/$color$maxSecrets")
        }
        return text
    }
}