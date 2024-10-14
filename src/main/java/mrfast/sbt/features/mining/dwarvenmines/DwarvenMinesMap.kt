package mrfast.sbt.features.mining.dwarvenmines

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.MiningConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11


@SkyblockTweaks.EventComponent
object DwarvenMinesMap {
    val map = ResourceLocation("skyblocktweaks", "map/dwarvenMinesMap.png")
    val playerIcon = ResourceLocation("skyblocktweaks", "map/playerIcon.png")

    var lastPlayerX = 0.0
    var lastPlayerZ = 0.0
    var lastPlayerR = 0.0

    init {
        DwarvenMinesMapGui()
    }

    class DwarvenMinesMapGui : GuiManager.Element() {
        init {
            this.relativeX = 0.83203125
            this.relativeY = 0.0
            this.elementName = "Dwarven Mines Map"
            this.addToList()
            this.height = 955 / 4
            this.width = 854 / 4
        }

        override fun draw() {
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.color(1f, 1f, 1f, 1f)
            GlStateManager.pushMatrix()
            Utils.mc.textureManager.bindTexture(map)
            GuiUtils.drawTexture(
                0f,
                0f,
                this.width.toFloat(),
                this.height.toFloat(),
                0f,
                1f,
                0f,
                1f,
                GL11.GL_NEAREST
            )
            GlStateManager.popMatrix()

            val player: EntityPlayerSP = Utils.mc.thePlayer
            var x = lastPlayerX
            var z = lastPlayerZ
            var rotation = lastPlayerR
            val newX = Math.round((player.posX + 217) / 2).toDouble()
            val newZ = Math.round((player.posZ + 179) / 2).toDouble()
            val newRotation = player.rotationYawHead.toDouble()

            val deltaX = newX - x
            val deltaZ = newZ - z
            val deltaR = newRotation - rotation

            x += deltaX / 50
            z += deltaZ / 50
            rotation += deltaR / 50

            lastPlayerX = x
            lastPlayerZ = z
            lastPlayerR = rotation

            GlStateManager.color(1f, 1f, 1f, 1f)
            Utils.mc.textureManager.bindTexture(playerIcon)
            GlStateManager.pushMatrix()
            GlStateManager.translate(x, z, 0.0)
            GlStateManager.rotate(player.rotationYawHead - 180, 0f, 0f, 1f)
            GlStateManager.translate(-x, -z, 0.0)
            GuiUtils.drawTexture((x - 2.5).toFloat(), (z - 3.5).toFloat(), 5f, 7f, 0f, 1f, 0f, 1f, GL11.GL_NEAREST)
            GlStateManager.popMatrix()
            GlStateManager.popMatrix()
        }

        override fun isActive(): Boolean {
            return MiningConfig.dwarvenMinesMap && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            return LocationManager.currentIsland == "Dwarven Mines"
        }
    }
}