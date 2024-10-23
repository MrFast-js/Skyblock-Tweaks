package mrfast.sbt.config.components.shader

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11
import javax.vecmath.Vector2d

object ShaderManager {

    private inline val mc get() = Minecraft.getMinecraft()

    fun drawQuads(
        left: Float = 0f,
        top: Float = 0f,
        right: Float = ScaledResolution(mc).scaledWidth_double.toFloat(),
        bottom: Float = ScaledResolution(mc).scaledHeight_double.toFloat(),
    ) {
        GL11.glBegin(GL11.GL_QUADS)
        GL11.glTexCoord2f(0f, 1f)
        GL11.glVertex2f(left, top)
        GL11.glTexCoord2f(0f, 0f)
        GL11.glVertex2f(left, bottom)
        GL11.glTexCoord2f(1f, 0f)
        GL11.glVertex2f(right, bottom)
        GL11.glTexCoord2f(1f, 1f)
        GL11.glVertex2f(right, top)
        GL11.glEnd()
    }

    /*
     * MUST GO CLOCK WISE IN POINTS
     */
    fun drawPolygon(points: List<Vector2d>) {
        // Ensure there are enough points to form a polygon
        if (points.size < 3) return // A polygon must have at least 3 points

        // Begin drawing the polygon using triangles
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer

        // Enable necessary OpenGL states
        GlStateManager.enableBlend() // Enable blending for transparency
        GlStateManager.disableTexture2D() // Disable texturing for solid color
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0) // Set blend function

        // Draw the polygon using triangles
        worldRenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION)

        // Use the first point as the reference for triangulation
        val referencePoint = points[0]

        for (i in 1 until points.size - 1) {
            // Draw a triangle between the reference point and two other points
            worldRenderer.pos(referencePoint.x.toDouble(), referencePoint.y.toDouble(), 0.0).endVertex()
            worldRenderer.pos(points[i].x.toDouble(), points[i].y.toDouble(), 0.0).endVertex()
            worldRenderer.pos(points[i + 1].x.toDouble(), points[i + 1].y.toDouble(), 0.0).endVertex()
        }

        // Finish drawing
        tessellator.draw()

        // Restore OpenGL state
        GlStateManager.enableTexture2D() // Re-enable texturing
        GlStateManager.disableBlend() // Disable blending
    }

    fun drawTriangle(left: Int, top: Int, bottom: Int, right: Int, color: Int) {
        var i: Int
        var left = left
        var right = right
        var top = top
        var bottom = bottom

        if (left < right) {
            i = left
            left = right
            right = i
        }

        if (top < bottom) {
            i = top
            top = bottom
            bottom = i
        }

        val f = (color shr 24 and 255).toFloat() / 255.0f
        val g = (color shr 16 and 255).toFloat() / 255.0f
        val h = (color shr 8 and 255).toFloat() / 255.0f
        val j = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.color(g, h, j, f)
        worldRenderer.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION)
        worldRenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).endVertex()
        worldRenderer.pos((left + right shr 1).toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }


    fun checkFbo(framebuffer: Framebuffer?) {
        if (framebuffer != null && framebuffer.depthBuffer >= 0) {
            setupFbo(framebuffer)
            framebuffer.depthBuffer = -1
        }
    }

    fun setupFbo(framebuffer: Framebuffer?) {
        if (framebuffer != null) {
            EXTFramebufferObject.glDeleteRenderbuffersEXT(framebuffer.depthBuffer);
            val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
            EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID);
            EXTFramebufferObject.glRenderbufferStorageEXT(
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
                mc.displayWidth,
                mc.displayHeight
            );
            EXTFramebufferObject.glFramebufferRenderbufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                stencilDepthBufferID
            );
            EXTFramebufferObject.glFramebufferRenderbufferEXT(
                EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
                EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
                EXTFramebufferObject.GL_RENDERBUFFER_EXT,
                stencilDepthBufferID
            );
        }
    }

    fun use() {
        mc.framebuffer.bindFramebuffer(false);
        checkFbo(mc.framebuffer);
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
    }

    /**
     * Configures the stencil buffer to read based on the specified reference value.
     *
     * @param reference The reference value used for stencil comparison. Defaults to 1.
     */
    fun configureStencilReadReference(reference: Int = 1) {
        GL11.glColorMask(true, true, true, true)  // Enable writing to the color buffer
        GL11.glStencilFunc(GL11.GL_EQUAL, reference, 1)  // Set stencil function to equal reference
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP)  // Keep the stencil buffer unchanged
    }

    fun deactivateStencilTest() {
        GL11.glDisable(GL11.GL_STENCIL_TEST)  // Disable stencil testing
    }
}