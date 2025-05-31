package mrfast.sbt.customevents

import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event

class RenderItemStackEvent(var itemStack: ItemStack, var x: Int, var y: Int) : Event()