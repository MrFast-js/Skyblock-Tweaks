package mrfast.sbt.managers

import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.FontRenderer
import net.minecraft.util.ResourceLocation

object FontManager {
    private var minecraftSmoothFont = FontRenderer(
        Utils.getGameSettings(),
        ResourceLocation("skyblocktweaks", "font/smooth_ascii.png"),
        Utils.mc.renderEngine,
        false
    )
    init {
        minecraftSmoothFont.onResourceManagerReload(Utils.mc.resourceManager)
    }

    fun getFontRenderer(): FontRenderer {
        if(CustomizationConfig.selectedFont == "Smooth") return minecraftSmoothFont
        return Utils.mc.fontRendererObj
    }
}