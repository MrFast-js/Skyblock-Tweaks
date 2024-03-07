package mrfast.sbt.mixins.transformers;

import mrfast.sbt.config.categories.RenderingConfig;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Inject(method = "setBrightness", at = @At("HEAD"),cancellable = true)
    private void onSetBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;
        if (RenderingConfig.INSTANCE.getDisableDamageTint() && flag1) {
            // Stops the red tint effect from being drawn
            cir.cancel();
        }
    }
}