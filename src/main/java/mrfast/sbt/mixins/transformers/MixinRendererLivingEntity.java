package mrfast.sbt.mixins.transformers;

import mrfast.sbt.config.categories.RenderingConfig;
import mrfast.sbt.customevents.PacketEvent;
import mrfast.sbt.customevents.RenderEntityModelEvent;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RendererLivingEntity.class)
public class MixinRendererLivingEntity<T extends EntityLivingBase> {
    @Shadow
    protected ModelBase mainModel;

    @Inject(method = "setBrightness", at = @At("HEAD"), cancellable = true)
    private void onSetBrightness(T entitylivingbaseIn, float partialTicks, boolean combineTextures, CallbackInfoReturnable<Boolean> cir) {
        boolean flag1 = entitylivingbaseIn.hurtTime > 0 || entitylivingbaseIn.deathTime > 0;
        if (RenderingConfig.INSTANCE.getDisableDamageTint() && flag1) {
            // Stops the red tint effect from being drawn
            cir.cancel();
        }
    }

    @Inject(method = "renderLayers", at = @At("RETURN"), cancellable = true)
    private void onRenderLayers(T entitylivingbaseIn, float p_177093_2_, float p_177093_3_, float partialTicks, float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new RenderEntityModelEvent(
                entitylivingbaseIn, p_177093_2_, p_177093_3_, p_177093_5_, p_177093_6_, p_177093_7_, p_177093_8_, mainModel
        ))) {
            ci.cancel();
        }
    }
}