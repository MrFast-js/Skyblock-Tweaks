package mrfast.sbt.mixins.transformers;

import mrfast.sbt.managers.EntityOutlineManager;
import mrfast.sbt.utils.LocationUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "at.hannibal2.skyhanni.utils.EntityOutlineRenderer")
public class MixinEntityOutlineRenderer {

    // Inject into skyhanni's isEnabled so glow code is still processed even with glow features disabled on sh if their enabled on SBT
    @Inject(method = "isEnabled", at = @At("RETURN"), cancellable = true)
    public void beforeReturnFalse(CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue()) {
            if (EntityOutlineManager.INSTANCE.glowFeaturesEnabled() && LocationUtils.INSTANCE.getInSkyblock()) {
                callback.setReturnValue(true);
            }
        }
    }
}