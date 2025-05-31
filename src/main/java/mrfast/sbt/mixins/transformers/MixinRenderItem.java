package mrfast.sbt.mixins.transformers;

import mrfast.sbt.customevents.RenderItemStackEvent;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderItem.class)
public class MixinRenderItem {
    @Inject(method = "renderItemOverlayIntoGUI", at = @At("TAIL"))
    private void onRenderItemOverlays(FontRenderer fr, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if(stack == null) return;

        GlStateManager.translate(0, 0, 70f);
        MinecraftForge.EVENT_BUS.post(new RenderItemStackEvent(stack, x, y));
        GlStateManager.translate(0, 0, -70f);
    }
}
