package mrfast.sbt.guis

import mrfast.sbt.utils.GuiUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11
import kotlin.random.Random

class SnowingEffect {
    private val snowflakes = mutableListOf<Snowflake>()
    private var lastUpdateTime: Long = System.nanoTime()
    private var fadeStartTime: Long = System.nanoTime()
    private var isFading: Boolean = false

    init {
        initializeSnowflakes()
    }

    private fun initializeSnowflakes() {
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

        fun update(deltaTime: Float) {
            val sr = ScaledResolution(Minecraft.getMinecraft())

            y += speed * deltaTime * 0.25f
            rotation += rotationSpeed * deltaTime * 0.2f

            if (y > sr.scaledHeight) {
                y = y - sr.scaledHeight - 15f
                x = Random.nextFloat() * sr.scaledWidth
            }
        }

        fun draw(opacity: Float) {
            GlStateManager.pushMatrix()
            GlStateManager.color(0.5f, 0.5f, 0.5f, opacity)
            GlStateManager.translate(x + 7.5f, y + 7.5f, 0f)
            GlStateManager.rotate(rotation, 0f, 0f, 1f)
            GlStateManager.translate(-(x + 7.5f), -(y + 7.5f), 0f)
            Minecraft.getMinecraft().textureManager.bindTexture(snowFlakeTexture)
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
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastUpdateTime) / 10_000_000.0f // Convert nanoseconds to milliseconds

        // Handle fade-in based on elapsed time
        if (deltaTime > 25) { // Render paused for too long
            opacity = 0f
            fadeStartTime = System.nanoTime()
            isFading = true
        }

        if (isFading) {
            val fadeDuration = 600_000_000L // 0.6 seconds in nanoseconds
            val elapsedFadeTime = currentTime - fadeStartTime

            opacity = if (elapsedFadeTime >= fadeDuration) {
                isFading = false
                0.7f
            } else {
                val calculatedOpacity = (elapsedFadeTime.toFloat() / fadeDuration) * 0.7f
                calculatedOpacity
            }
        }

        for (snowflake in snowflakes) {
            snowflake.update(deltaTime)
            snowflake.draw(opacity)
        }

        lastUpdateTime = currentTime
    }

    companion object {
        private var opacity = 0f
        private val snowFlakeTexture = ResourceLocation("skyblocktweaks", "gui/snowflake.png")
    }
}