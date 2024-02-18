package mrfast.sbt.features.general

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


object LowHealthTint {
    private val VIGNETTE_TEXTURE = ResourceLocation("textures/misc/vignette.png")

    @SubscribeEvent
    fun onRenderGameOverlay(event: RenderGameOverlayEvent.Post) {
        if (event.type != RenderGameOverlayEvent.ElementType.HEALTH || !GeneralConfig.lowHealthTint) return

        renderRedTint(PlayerStats.health)
    }

    private fun renderRedTint(currentHealth: Int) {
        // For some reason these values appear when swapping worlds?
        if (currentHealth == 20 || PlayerStats.maxHealth == 1090) return

        val threshold = PlayerStats.maxHealth / 2.0
        val resolution = ScaledResolution(Utils.mc)

        if (currentHealth <= threshold) {
            val f = ((threshold - currentHealth) / threshold + 1.0f / threshold * 2.0f).toFloat()
            GlStateManager.pushMatrix()
            GlStateManager.enableBlend()
            GlStateManager.disableDepth()
            GlStateManager.depthMask(false)
            GlStateManager.tryBlendFuncSeparate(0, 769, 1, 0) // Enable blending for transparency effect

            // Set color to red with varying opacity
            GlStateManager.color(0.0F, f, f, 1.0F);

            // Bind the vignette texture
            Utils.mc.textureManager.bindTexture(VIGNETTE_TEXTURE)

            val tessellator = Tessellator.getInstance()
            val worldRenderer = tessellator.worldRenderer

            // Begin rendering
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
            // Define vertices and texture coordinates
            worldRenderer.pos(0.0, resolution.scaledHeight.toDouble(), -90.0).tex(0.0, 1.0).endVertex()
            worldRenderer.pos(resolution.scaledWidth.toDouble(), resolution.scaledHeight.toDouble(), -90.0)
                .tex(1.0, 1.0).endVertex()
            worldRenderer.pos(resolution.scaledWidth.toDouble(), 0.0, -90.0).tex(1.0, 0.0).endVertex()
            worldRenderer.pos(0.0, 0.0, -90.0).tex(0.0, 0.0).endVertex()
            tessellator.draw()

            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0) // Reset blending settings
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f) // Reset color
            GlStateManager.depthMask(true) // Restore depth mask
            GlStateManager.enableDepth() // Enable depth testing
            GlStateManager.disableBlend()
            GlStateManager.popMatrix()
        }
    }
}