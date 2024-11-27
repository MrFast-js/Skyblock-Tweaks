package mrfast.sbt.guis

import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import kotlin.random.Random

class SnowingEffect {
    private val snowflakes = mutableListOf<Snowflake>()

    init {
        initializeSnowflakes()
    }

    private fun initializeSnowflakes() {
        snowflakes.clear()
        for (i in 0 until 150) {
            snowflakes.add(Snowflake())
        }
    }

    class Snowflake {
        private val sr = ScaledResolution(Minecraft.getMinecraft())
        var x: Float = Random.nextFloat() * sr.scaledWidth
        var y: Float = Random.nextFloat() * sr.scaledHeight
        private val speed: Float = Random.nextFloat() * 2 + 1
        private var rotation: Float = Random.nextFloat() * 360
        private val rotationSpeed: Float = Random.nextFloat() * 2 - 1
        private var lastUpdateTime: Long = System.nanoTime()

        fun update() {
            val currentTime = System.nanoTime()
            var deltaTime = (currentTime - lastUpdateTime) / 10_000_000.0f
            val sr = ScaledResolution(Minecraft.getMinecraft())

            // Reset after not moving for a while
            if (deltaTime > 250) {
                x = Random.nextFloat() * sr.scaledWidth
                y = Random.nextFloat() * sr.scaledHeight
                deltaTime = 0f
            }

            lastUpdateTime = currentTime

            y += speed * deltaTime * 0.25f
            rotation += rotationSpeed * deltaTime * 0.2f

            if (y > sr.scaledHeight) {
                y = -15f
                x = Random.nextFloat() * sr.scaledWidth
            }
        }

        fun draw() {
            GlStateManager.pushMatrix()
            GlStateManager.translate(x + 7.5f, y + 7.5f, 0f)
            GlStateManager.rotate(rotation, 0f, 0f, 1f)
            GlStateManager.translate(-(x + 7.5f), -(y + 7.5f), 0f)
            Minecraft.getMinecraft().textureManager.bindTexture(snowFlakeTexture);
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