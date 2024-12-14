package mrfast.sbt.mixins.transformers;

import mrfast.sbt.config.categories.CustomizationConfig;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public abstract class MixinRenderPlayer {

    @Inject(method = {"preRenderCallback(Lnet/minecraft/client/entity/AbstractClientPlayer;F)V"}, at = {@At("HEAD")})
    public void beforeRender(AbstractClientPlayer entitylivingbaseIn, float partialTickTime, CallbackInfo ci) {
        if (CustomizationConfig.INSTANCE.getSmallPlayers()) {
            double scale = CustomizationConfig.INSTANCE.getSmallPlayersScale() / 100.0;
            GlStateManager.scale(scale, scale, scale);
        }
    }

}
