package mrfast.sbt.customevents

import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event

open class SkyblockInventoryItemEvent : Event() {
    enum class EventType {
        GAINED,
        LOST
    }

    open class InventoryItemEvent(val eventType: EventType, val amount: Int = 1, val itemId: String) :
        SkyblockInventoryItemEvent()

    class ItemStackEvent(eventType: EventType, amount: Int = 1, var itemName: String, itemId: String, var stack: ItemStack) :
        InventoryItemEvent(eventType, amount, itemId)

    class SackItemEvent(eventType: EventType, amount: Int = 1, val materialName: String, itemId: String) :
        InventoryItemEvent(eventType, amount, itemId)
}
