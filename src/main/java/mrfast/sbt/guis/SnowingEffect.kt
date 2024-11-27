package mrfast.sbt.guis

import gg.essential.elementa.WindowScreen
import mrfast.sbt.utils.GuiUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import kotlin.random.Random

class SnowingEffect(private val window: WindowScreen) {
    private val snowflakes = mutableListOf<Snowflake>()
    init {
        // Initialize snowflakes
        for (i in 0 until 100) {
            snowflakes.add(Snowflake(window.width, window.height))
        }
    }

    class Snowflake(private val width: Int, private val height: Int) {
        var x: Float = Random.nextFloat() * width
        var y: Float = Random.nextFloat() * height
        private val speed: Float = Random.nextFloat() * 2 + 1
        private var rotation: Float = Random.nextFloat() * 360
        private val rotationSpeed: Float = Random.nextFloat() * 2 - 1
        private var lastUpdateTime: Long = System.nanoTime()

        fun update() {
            val currentTime = System.nanoTime()
            val deltaTime = (currentTime - lastUpdateTime) / 10_000_000.0f
            lastUpdateTime = currentTime

            y += speed * deltaTime * 0.25f
            rotation += rotationSpeed * deltaTime * 0.2f

            if (y > height) {
                y = -15f
                x = Random.nextFloat() * width
            }
        }

        fun draw() {
            GlStateManager.pushMatrix()
            GlStateManager.translate(x + 7.5f, y + 7.5f, 0f)
            GlStateManager.rotate(rotation, 0f, 0f, 1f)
            GlStateManager.translate(-(x + 7.5f), -(y + 7.5f), 0f)
            Minecraft.getMinecraft().textureManager.bindTexture(snowFlakeTexture);
            GlStateManager.color(1f, 1f, 1f)
            GuiUtils.drawTexture(
                x,
                y,
                15f,
                15f,
                0F,
                1f,
                0F,
                1f,
                GL11.GL_NEAREST
            )
            GlStateManager.popMatrix()
        }
    }

    fun drawSnowflakes() {
        for (snowflake in snowflakes) {
            snowflake.update()
            snowflake.draw()
        }
    }

    companion object {
        private val snowFlakeTexture = ResourceLocation("skyblocktweaks", "gui/snowflake.png")
    }
}