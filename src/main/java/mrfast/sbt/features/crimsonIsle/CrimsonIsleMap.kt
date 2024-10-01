package mrfast.sbt.features.crimsonIsle

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.CrimsonConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.containsCoordinates
import mrfast.sbt.utils.Utils.extractCoordinates
import mrfast.sbt.utils.Utils.getRegexGroups
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.round

@SkyblockTweaks.EventComponent
object CrimsonIsleMap {
    val mapTexture = ResourceLocation("skyblocktweaks", "map/crimsonIslesMap.png")
    val playerIcon = ResourceLocation("skyblocktweaks", "map/playerIcon.png")
    val waypointIcon = ResourceLocation("skyblocktweaks", "map/mapPoint.png")

    var lastPlayerX = 0.0
    var lastPlayerZ = 0.0
    var lastPlayerR = 0.0

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "Crimson Isle" || !CrimsonConfig.crimsonIslesMap) return

        val message = event.message.unformattedText.clean()

        if (message.containsCoordinates()) {
            val sentBy = message.getRegexGroups("""(?:\[\w+\]\s*)?(\w+):\s""".toRegex())!![1]!!.value
            val coordinates = message.extractCoordinates() ?: return

            crimsonIslesWaypoints[sentBy] = coordinates
        }
    }

    private var crimsonIslesWaypoints = mutableMapOf<String, BlockPos>()

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

            for (crimsonIslesWaypoint in crimsonIslesWaypoints) {
                val blockX = crimsonIslesWaypoint.value.x
                val blockZ = crimsonIslesWaypoint.value.z

                val normalizedX = (round((blockX + 360) / 2 + 821 / 4.0) / 2)
                val normalizedZ = (round((blockZ + 423) / 2 + 1391 / 4.0) / 2)

                GlStateManager.pushMatrix()
                Minecraft.getMinecraft().textureManager.bindTexture(waypointIcon)
                GlStateManager.color(1f, 1f, 1f)

                // Draw the icon
                GuiUtils.drawTexture(
                    (normalizedX - 2.5).toFloat(),
                    (normalizedZ - 3.5).toFloat(),
                    5f,
                    5f,
                    0f,
                    1f,
                    0f,
                    1f,
                    GL11.GL_NEAREST
                )

                // Draw the label above the icon
                val waypointName = crimsonIslesWaypoint.key

                // Calculate the position for the label
                val labelX = normalizedX
                val labelY = normalizedZ - 9 // Adjust the vertical position as needed

                // Draw a transparent background rectangle behind the text
                val backgroundWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("§b$waypointName") * (DeveloperConfig.valueTest / 10.0f)
                val backgroundHeight = 8 // Height of the background rectangle
                val backgroundColor = 0x80000000 // Semi-transparent black (0x80 for 50% alpha)

                // Draw the rectangle for the background
                Gui.drawRect(
                    (labelX - backgroundWidth / 2).toInt(),  // X position adjusted to center
                    (labelY + backgroundHeight).toInt(),     // Y position adjusted above the label
                    (labelX + backgroundWidth / 2).toInt(),  // X position adjusted to center
                    labelY.toInt(),                           // Y position (the bottom of the rectangle)
                    backgroundColor.toInt()                   // Background color
                )

                GuiUtils.drawText(
                    "§b$waypointName",
                    labelX.toFloat(),
                    labelY.toFloat(),
                    GuiUtils.TextStyle.BLACK_OUTLINE,
                    Color.WHITE,
                    true,
                    DeveloperConfig.valueTest/10.0f
                )

                GlStateManager.popMatrix()
            }
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
            return LocationManager.inSkyblock && LocationManager.currentIsland == "Crimson Isle"
        }
    }
}