package mrfast.sbt.guis.components.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.BufferUtils
import kotlin.math.exp
import kotlin.math.sqrt


class GaussianBlur(
    var radius: Float = 40f,
    val compression: Float = 2f
) : ShaderProgram(
    shaders = arrayOf(
        Shader.createShaderFromPath("/assets/skyblocktweaks/shaders/gaussian.fsh", Shader.FRAGMENT),
        Shader.DEFAULT_VERTEX_SHADER
    )
) {


    private inline val mc get() = Minecraft.getMinecraft()
    private var framebuffer = Framebuffer(1, 1, false)

    private fun setupUniforms(dir1: Float, dir2: Float) {
        this["textureIn"] = 0
        this["textureSize"] = floatArrayOf(1f / mc.displayWidth, 1f / mc.displayHeight)
        this["direction"] = floatArrayOf(dir1, dir2)
        this["radius"] = radius

        val weightBuffer = BufferUtils.createFloatBuffer(256)
        for (i in 0..radius.toInt()) {
            weightBuffer.put(calculateGaussianValue(i.toFloat(), radius / 2))
        }

        weightBuffer.rewind()
        this["weights"] = weightBuffer
    }

    fun start() {
        ShaderManager.use()
    }

    fun end() {
        ShaderManager.configureStencilReadReference()

        framebuffer = createFrameBuffer(framebuffer)

        framebuffer.framebufferClear()
        framebuffer.bindFramebuffer(false)
        this.activate()
        setupUniforms(compression, 0f)

        GlStateManager.bindTexture(mc.framebuffer.framebufferTexture)
        ShaderManager.drawQuads()
        framebuffer.unbindFramebuffer()
        this.deactivate()

        mc.framebuffer.bindFramebuffer(false)
        this.activate()
        setupUniforms(0f, compression)

        GlStateManager.bindTexture(framebuffer.framebufferTexture)
        ShaderManager.drawQuads()
        this.deactivate()

        ShaderManager.deactivateStencilTest()
        GlStateManager.color(1f, 1f, 1f, 1f)
        GlStateManager.bindTexture(0)
    }

    override fun drawShader(block: () -> Unit) {
        start()
        block()
        end()
    }

    private fun calculateGaussianValue(x: Float, sigma: Float): Float {
        val output = 1.0 / sqrt(2.0 * Math.PI * (sigma * sigma))
        return (output * exp(-(x * x) / (2.0 * (sigma * sigma)))).toFloat()
    }

    fun createFrameBuffer(framebuffer: Framebuffer?, depth: Boolean = false): Framebuffer {
        if (needsNewFramebuffer(framebuffer)) {
            framebuffer?.deleteFramebuffer()
            return Framebuffer(mc.displayWidth, mc.displayHeight, depth)
        }
        return framebuffer!!
    }

    private fun needsNewFramebuffer(framebuffer: Framebuffer?): Boolean {
        return framebuffer == null || framebuffer.framebufferWidth != mc.displayWidth || framebuffer.framebufferHeight != mc.displayHeight
    }

}