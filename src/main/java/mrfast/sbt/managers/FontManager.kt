package mrfast.sbt.managers

import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation

object FontManager {
    private var minecraftSmoothFont = FontRenderer(
        Utils.mc.gameSettings,
        ResourceLocation("skyblocktweaks", "font/smooth_ascii.png"),
        Utils.mc.renderEngine,
        false
    )
    init {
        minecraftSmoothFont.onResourceManagerReload(Utils.mc.resourceManager)
    }

    fun getSmoothFontRenderer(): FontRenderer {
        return minecraftSmoothFont
    }
}