package mrfast.sbt.customevents

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Event

open class SlotDrawnEvent : Event() {
    // Inner class representing the pre-draw event
    class Pre(s: Slot, g: GuiContainer) : SlotDrawnEvent() {
        val slot = s
        val gui = g
    }

    // Inner class representing the post-draw event
    class Post(s: Slot, g: GuiContainer) : SlotDrawnEvent() {
        val slot = s
        val gui = g
    }
}

