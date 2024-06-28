package mrfast.sbt.customevents

import net.minecraft.tileentity.TileEntitySign
import net.minecraftforge.fml.common.eventhandler.Event

class SignDrawnEvent(var mouseX: Int, var mouseY: Int, var sign: TileEntitySign) : Event()
