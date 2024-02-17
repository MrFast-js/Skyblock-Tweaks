package mrfast.sbt.utils

import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3

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
}