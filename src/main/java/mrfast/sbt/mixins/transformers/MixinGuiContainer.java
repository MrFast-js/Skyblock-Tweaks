package mrfast.sbt.mixins.transformers;

import mrfast.sbt.customevents.SlotClickedEvent;
import mrfast.sbt.customevents.SlotDrawnEvent;
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public class MixinGuiContainer {
    @Shadow
    protected int guiLeft;
    @Shadow
    protected int guiTop;

    @Inject(method = "handleMouseClick(Lnet/minecraft/inventory/Slot;III)V", at = @At("HEAD"), cancellable = true)
    private void onHandleMouseClick(Slot slot, int slotId, int clickedButton, int mode, CallbackInfo ci) {
        if (slot != null && Minecraft.getMinecraft().currentScreen != null) {
            if (MinecraftForge.EVENT_BUS.post(new SlotClickedEvent(slot, (GuiContainer) Minecraft.getMinecraft().currentScreen))) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "drawSlot", at = @At("HEAD"))
    private void preSlotDrawnEvent(Slot slotIn, CallbackInfo ci) {
        try {
            if (Minecraft.getMinecraft().currentScreen != null) {
                MinecraftForge.EVENT_BUS.post(new SlotDrawnEvent.Pre(slotIn, (GuiContainer) Minecraft.getMinecraft().currentScreen));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void postSlotDrawnEvent(Slot slotIn, CallbackInfo ci) {
        try {
            if (Minecraft.getMinecraft().currentScreen != null) {
                GlStateManager.translate(0, 0, 275f);
                MinecraftForge.EVENT_BUS.post(new SlotDrawnEvent.Post(slotIn, (GuiContainer) Minecraft.getMinecraft().currentScreen));
                GlStateManager.translate(0, 0, -275f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/inventory/GuiContainer;drawGuiContainerBackgroundLayer(FII)V", ordinal = 0, shift = At.Shift.AFTER))
    private void onGuiContainerDrawn(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GlStateManager.translate(guiLeft, guiTop, 0);
        MinecraftForge.EVENT_BUS.post(new GuiContainerBackgroundDrawnEvent((GuiContainer) Minecraft.getMinecraft().currentScreen, mouseX, mouseY, guiLeft, guiTop));
        GlStateManager.translate(-guiLeft, -guiTop, 0);
    }
}