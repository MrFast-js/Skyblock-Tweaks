package mrfast.sbt.customevents

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.eventhandler.Event

class GuiContainerBackgroundDrawnEvent(
    var gui: GuiContainer?,
    var mouseX: Int,
    var mouseY: Int,
    var guiLeft: Int,
    var guiTop: Int
) : Event() {
}