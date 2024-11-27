package mrfast.sbt.features.eastereggs

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.guis.ConfigGui
import mrfast.sbt.guis.SnowingEffect
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

@SkyblockTweaks.EventComponent
object SnowingGuis {
    private var snowingEffect = SnowingEffect()

    @SubscribeEvent
    fun onDrawGuiBackground(event: GuiScreenEvent.BackgroundDrawnEvent) {
        if(!CustomizationConfig.snowEffect) return

        if(Calendar.getInstance().get(Calendar.MONTH) != Calendar.DECEMBER && !CustomizationConfig.snowEffectForce) return

        if(CustomizationConfig.snowEffectExclusive && event.gui !is ConfigGui) return

        // Darken the snowflakes
        GlStateManager.color(0.5f, 0.5f, 0.5f, 0.7f)
        snowingEffect.drawSnowflakes()
        GlStateManager.color(1f, 1f, 1f)
    }
}