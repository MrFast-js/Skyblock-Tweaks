package mrfast.sbt.mixins.transformers;

import mrfast.sbt.managers.EntityOutlineManager;
import mrfast.sbt.utils.LocationUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adapted from Skyhanni under GNU LGPL v2.1 license
 *
 * @link https://github.com/hannibal002/SkyHanni/blob/beta/LICENSE
 */
@Mixin(RenderGlobal.class)
public abstract class MixinRenderGlobal {

    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z", ordinal = 0))
    private boolean onRenderEntities(RenderGlobal renderGlobal) {
        return false;
    }

    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isSpectator()Z", ordinal = 0))
    private boolean isSpectatorDisableCheck(EntityPlayerSP entityPlayerSP) {
        return LocationUtils.INSTANCE.getInSkyblock();
    }

    @Redirect(method = "isRenderEntityOutlines", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z", ordinal = 0))
    private boolean isKeyDownDisableCheck(KeyBinding keyBinding) {
        return EntityOutlineManager.INSTANCE.shouldRenderEntityOutlines();
    }

    @Inject(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderGlobal;isRenderEntityOutlines()Z", shift = At.Shift.BEFORE))
    public void renderEntities(Entity renderViewEntity, ICamera camera, float partialTicks, CallbackInfo ci) {
        if (EntityOutlineManager.INSTANCE.shouldRenderEntityOutlines()) {
            double x = renderViewEntity.lastTickPosX + (renderViewEntity.posX - renderViewEntity.lastTickPosX) * partialTicks;
            double y = renderViewEntity.lastTickPosY + (renderViewEntity.posY - renderViewEntity.lastTickPosY) * partialTicks;
            double z = renderViewEntity.lastTickPosZ + (renderViewEntity.posZ - renderViewEntity.lastTickPosZ) * partialTicks;
            EntityOutlineManager.INSTANCE.renderEntityOutlines(camera, partialTicks, x, y, z);
        }
    }
}