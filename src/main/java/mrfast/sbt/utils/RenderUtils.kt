package mrfast.sbt.utils

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11
import java.awt.Color

object RenderUtils {
    val mc = Utils.mc
    fun draw3DString(text: String, worldPos: Vec3, partialTicks: Float) {
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
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()
        GlStateManager.enableBlend()
        GlStateManager.blendFunc(770, 771)

        val width = fontRenderer.getStringWidth(text) / 2.0f
        fontRenderer.drawString(text, (-width).toInt(), 0, 0xFFFFFF)

        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
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
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GlStateManager.disableLighting()
        GlStateManager.disableCull()
        GlStateManager.depthMask(false)
        GlStateManager.disableDepth()

        GL11.glLineWidth(thickness.toFloat())
        GL11.glBegin(GL11.GL_LINES)
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glVertex3d(fromX, fromY, fromZ)
        GL11.glVertex3d(toX, toY, toZ)
        GL11.glEnd()

        GlStateManager.enableDepth()
        GlStateManager.depthMask(true)
        GlStateManager.enableCull()
        GlStateManager.enableLighting()
        GlStateManager.disableBlend()
        GlStateManager.enableTexture2D()
        GlStateManager.popMatrix()
    }
}