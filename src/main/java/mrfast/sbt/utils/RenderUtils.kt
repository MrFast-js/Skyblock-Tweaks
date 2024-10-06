package mrfast.sbt.utils

import gg.essential.elementa.utils.withAlpha
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

object RenderUtils {
    val mc = Utils.mc
    fun draw3DString(
        text: String,
        worldPos: Vec3,
        partialTicks: Float,
        depth: Boolean? = false,
        shadow: Boolean? = true
    ) {
        val renderManager = mc.renderManager
        val fontRenderer: FontRenderer = mc.fontRendererObj

        val player = mc.thePlayer
        val viewerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks.toDouble()
        val viewerPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks.toDouble()
        val viewerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks.toDouble()

        val posX = worldPos.xCoord - viewerPosX
        val posY = worldPos.yCoord - viewerPosY
        val posZ = worldPos.zCoord - viewerPosZ

        GlStateManager.pushMatrix()
        GlStateManager.translate(posX.toFloat(), posY.toFloat(), posZ.toFloat())
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        GlStateManager.scale(-0.03f, -0.03f, 0.03f)

        GlStateManager.disableLighting()
        if (depth == false) {
            GlStateManager.depthMask(false)
            GlStateManager.disableDepth()
        }
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        val width = fontRenderer.getStringWidth(text) / 2.0f
        fontRenderer.drawString(text, (-width), 0f, 0xFFFFFF, shadow == true)

        if (depth == false) {
            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
        }
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.enableLighting()

        GlStateManager.popMatrix()
    }

    fun drawLine(from: Vec3, to: Vec3, thickness: Int, color: Color, partialTicks: Float) {
        val player = mc.thePlayer
        val viewerPosX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks.toDouble()
        val viewerPosY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks.toDouble()
        val viewerPosZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks.toDouble()

        val fromX = from.xCoord - viewerPosX
        val fromY = from.yCoord - viewerPosY
        val fromZ = from.zCoord - viewerPosZ

        val toX = to.xCoord - viewerPosX
        val toY = to.yCoord - viewerPosY
        val toZ = to.zCoord - viewerPosZ

        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f
        val alpha = color.alpha / 255.0f

        GlStateManager.pushMatrix()
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableCull()

        glLineWidth(thickness.toFloat())
        glBegin(GL_LINES)
        glColor4f(red, green, blue, alpha)
        glVertex3d(fromX, fromY, fromZ)
        glVertex3d(toX, toY, toZ)
        glEnd()

        GlStateManager.enableCull()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }

    fun drawSpecialBB(pos: BlockPos, fillColor: Color, partialTicks: Float) {
        val bb = AxisAlignedBB(pos, pos.add(1, 1, 1)).offset(-0.001, -0.001, -0.001).expand(0.002, 0.002, 0.002)
        drawSpecialBB(bb, fillColor, partialTicks)
    }

    fun drawSpecialBB(bb: AxisAlignedBB, fillColor: Color, partialTicks: Float) {
        GlStateManager.pushMatrix()
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.depthMask(false)

        val width = max(1 - (Utils.mc.thePlayer.getDistance(bb.minX, bb.minY, bb.minZ) / 10 - 2), 2.0)
        drawFilledBB(bb, fillColor.withAlpha(0.6f), partialTicks)
        drawOutlinedBB(bb, fillColor.withAlpha(0.9f), width.toFloat(), partialTicks)
        GlStateManager.depthMask(true)
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
        GlStateManager.popMatrix()
    }

    fun drawOutlinedBB(aabbbb: AxisAlignedBB?, color: Color, width: Float, partialTicks: Float) {
        val render = Minecraft.getMinecraft().renderViewEntity
        val realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks
        val realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks
        val realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-realX, -realY, -realZ)
        GlStateManager.enableBlend()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        GlStateManager.disableTexture2D()
        glLineWidth(width)
        RenderGlobal.drawOutlinedBoundingBox(aabbbb, color.red, color.green, color.blue, color.alpha)
        GlStateManager.translate(realX, realY, realZ)
        GlStateManager.popMatrix()
    }

    fun drawFilledBB(bb: AxisAlignedBB, c: Color, partialTicks: Float) {
        val aabb = bb.offset(-0.002, -0.001, -0.002).expand(0.004, 0.005, 0.004)
        val render = Minecraft.getMinecraft().renderViewEntity
        val realX = render.lastTickPosX + (render.posX - render.lastTickPosX) * partialTicks
        val realY = render.lastTickPosY + (render.posY - render.lastTickPosY) * partialTicks
        val realZ = render.lastTickPosZ + (render.posZ - render.lastTickPosZ) * partialTicks
        GlStateManager.pushMatrix()
        GlStateManager.translate(-realX, -realY, -realZ)
        GlStateManager.disableTexture2D()
        GlStateManager.enableBlend()
        GlStateManager.disableLighting()
        GlStateManager.disableAlpha()
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        val color = c.rgb
        var a = (color shr 24 and 255).toFloat() / 255.0f
        a = (a.toDouble() * 0.15).toFloat()
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).color(r, g, b, a).endVertex()
        worldRenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).color(r, g, b, a).endVertex()
        tessellator.draw()
        GlStateManager.translate(realX, realY, realZ)
        GlStateManager.disableBlend()
        GlStateManager.enableAlpha()
        GlStateManager.enableTexture2D()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        GlStateManager.popMatrix()
    }

    fun drawFilledCircleWithBorder(
        center: Vec3,
        radius: Float,
        segments: Int,
        borderColor: Color,
        fillColor: Color,
        partialTicks: Float
    ) {
        val player = Utils.mc.thePlayer
        val interpolatedX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks
        val interpolatedY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks
        val interpolatedZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks

        val centerX = center.xCoord - interpolatedX
        val centerY = center.yCoord - interpolatedY + 0.01 // add to stop ground clipping
        val centerZ = center.zCoord - interpolatedZ

        // Enable blending for transparency
        GlStateManager.enableBlend()
        GlStateManager.disableTexture2D()
        GlStateManager.tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        GlStateManager.disableLighting()
        GlStateManager.disableCull()

        // Draw filled circle
        GlStateManager.color(
            fillColor.red / 255f,
            fillColor.green / 255f,
            fillColor.blue / 255f,
            0.15f
        )
        glBegin(GL_TRIANGLE_FAN)
        glVertex3d(centerX, centerY, centerZ) // Center of the circle
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            glVertex3d(centerX + x, centerY, centerZ + z)
        }
        glEnd()

        GlStateManager.disableDepth()

        // Draw border (line loop)
        GlStateManager.color(
            borderColor.red / 255f,
            borderColor.green / 255f,
            borderColor.blue / 255f,
            0.5f
        )
        glLineWidth(3f)
        glBegin(GL_LINE_LOOP)
        for (i in 0..segments) {
            val angle = Math.PI * 2 * i / segments
            val x = radius * cos(angle)
            val z = radius * sin(angle)
            glVertex3d(centerX + x, centerY, centerZ + z)
        }
        glEnd()

        GlStateManager.enableDepth()

        // Restore OpenGL settings
        GlStateManager.depthFunc(GL11.GL_LEQUAL)
        GlStateManager.enableCull()
        GlStateManager.enableLighting()
        GlStateManager.enableTexture2D()
        GlStateManager.disableBlend()
    }
}