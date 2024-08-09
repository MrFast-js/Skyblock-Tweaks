package mrfast.sbt.customevents

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.inventory.Slot
import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

@Cancelable
class SlotClickedEvent(s: Slot, g: GuiContainer) : Event() {
    val slot = s
    val gui = g
}