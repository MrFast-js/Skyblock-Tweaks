package mrfast.sbt.mixins.transformers;

import mrfast.sbt.customevents.SlotClickedEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseClick(Slot slot, int slotId, int clickedButton, int mode, CallbackInfo ci) {
        if(slot!=null) {
            if(MinecraftForge.EVENT_BUS.post(new SlotClickedEvent(slot, (GuiContainer) Minecraft.getMinecraft().currentScreen))) {
                ci.cancel();
            }
        }
    }
}