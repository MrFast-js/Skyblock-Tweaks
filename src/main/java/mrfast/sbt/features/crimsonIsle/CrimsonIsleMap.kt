package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

@SkyblockTweaks.EventComponent
object CrimsonIsleMap {
    val mapTexture = ResourceLocation("skyblocktweaks", "map/crimsonIslesMap.png")
    val playerIcon = ResourceLocation("skyblocktweaks", "map/playerIcon.png")
    var lastPlayerX = 0.0
    var lastPlayerZ = 0.0
    var lastPlayerR = 0.0

    init {
        Map()
    }

    class Map : GuiManager.Element() {
        init {
            this.relativeX = 0.0
            this.relativeY = 0.0
            this.elementName = "Crimson Isle Map"
            this.addToList()
            // Image size scaled down by 8x
            this.height = 1429 / 8
            this.width = 1653 / 8
            this.needsExample = true
        }

        override fun draw() {
            GlStateManager.pushMatrix()
            Minecraft.getMinecraft().textureManager.bindTexture(mapTexture);
            GlStateManager.color(1f, 1f, 1f)
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

            val player = Utils.mc.thePlayer
            var x = lastPlayerX
            var z = lastPlayerZ
            var rotation = lastPlayerR

            val newX = (Math.round((player.posX + 360) / 2 + 821 / 4) / 2).toDouble()
            val newZ = (Math.round((player.posZ + 423) / 2 + 1391 / 4) / 2).toDouble()
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

            GlStateManager.pushMatrix()
            Minecraft.getMinecraft().textureManager.bindTexture(playerIcon);
            GlStateManager.color(1f, 1f, 1f)
            GlStateManager.translate(x, z, 0.0)
            GlStateManager.rotate(newRotation.toFloat() - 180, 0f, 0f, 1f)
            GlStateManager.translate(-x, -z, 0.0)
            GuiUtils.drawTexture((x - 2.5).toFloat(), (z - 3.5).toFloat(), 5f, 7f, 0f, 1f, 0f, 1f, GL11.GL_NEAREST)
            GlStateManager.popMatrix()
        }

        override fun drawExample() {
            GlStateManager.pushMatrix()
            Minecraft.getMinecraft().textureManager.bindTexture(mapTexture);
            GlStateManager.color(1f, 1f, 1f)
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

            val fakePosition = BlockPos(-357, 84, -612)
            val x = (Math.round((fakePosition.x + 360f) / 2 + 821 / 4) / 2).toDouble()
            val z = (Math.round((fakePosition.z + 423f) / 2 + 1391 / 4) / 2).toDouble()
            val rotation = 45

            GlStateManager.pushMatrix()
            Minecraft.getMinecraft().textureManager.bindTexture(playerIcon);
            GlStateManager.color(1f, 1f, 1f)
            GlStateManager.translate(x, z, 0.0)
            GlStateManager.rotate(rotation.toFloat() - 180, 0f, 0f, 1f)
            GlStateManager.translate(-x, -z, 0.0)
            GuiUtils.drawTexture((x - 2.5).toFloat(), (z - 3.5).toFloat(), 5f, 7f, 0f, 1f, 0f, 1f, GL11.GL_NEAREST)
            GlStateManager.popMatrix()
        }

        override fun isActive(): Boolean {
            return CrimsonConfig.crimsonIslesMap
        }

        override fun isVisible(): Boolean {
            return LocationUtils.inSkyblock && LocationUtils.currentIsland == "Crimson Isle"
        }
    }
}