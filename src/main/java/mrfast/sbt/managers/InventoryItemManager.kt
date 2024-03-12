package mrfast.sbt.managers

import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.GuiChat
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs


object InventoryItemManager {
    private var previousInventory = mutableMapOf<String, Int>()
    private var preOpenInventory = mutableMapOf<String, Int>()
    private var isOpen = false

    @SubscribeEvent
    fun onTickEvent(event: ClientTickEvent) {
        if (!LocationUtils.inSkyblock || Utils.mc.theWorld == null) return

        val currentInventory = getCurrentInventoryState()

        // Compare current inventory with previous inventory when the GUI is closed
        if (!isOpen && currentInventory != previousInventory) {
            compareInventories(previousInventory, currentInventory)
            previousInventory = currentInventory.toMutableMap()
        }

        // Update previous inventory state when the GUI is opened
        if (isOpen) {
            preOpenInventory = currentInventory.toMutableMap()
        }

        // Update the isOpen flag
        isOpen = Utils.mc.currentScreen != null && Utils.mc.currentScreen !is GuiChat
    }

    private fun getCurrentInventoryState(): Map<String, Int> {
        val inventoryState = mutableMapOf<String, Int>()
        val mainInventory = Utils.mc.thePlayer.inventory.mainInventory

        for (i in 0 until mainInventory.size) {
            val itemStack = mainInventory[i]
            val displayName = itemStack?.displayName ?: "Empty slot"
            val itemCount = itemStack?.stackSize ?: 0
            inventoryState[displayName] = itemCount
        }

        return inventoryState
    }

    /*
    HAS MAJOR PROBLEM, WORKS GOOD, EXCEPT IF THEIR IS MULTIPLE OF SAME ITEM IN DIFFERENT SLOTS

    EXAMPLE: IF YOU HAVE 5 SLOTS OF 1 CARROT, IT WONT RECOGNIZE THAT ANY HAVE BEEN DROPPED FROM ANY OF THEM UNLESS THERE IS ONLY ONE STACK

    EXAMPLE 2: IF GAINING ITEMS AND IT GOES OVER 64 IT BECOMES TWO SLOTS, THUS BREAKING IT
     */
    private fun compareInventories(previous: Map<String, Int>, current: Map<String, Int>) {
        // Compare current inventory with previous inventory
        current.forEach { (displayName, currentCount) ->
            val previousCount = previous[displayName] ?: 0
            val countDifference = currentCount - previousCount

            // Check if there is a change in count
            if (countDifference != 0) {
                val action = if (countDifference > 0) "+" else "-"
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName(current)
                val itemChangeMessage = "$action${abs(countDifference)} $itemName"

                if (countDifference > 0) {
                    MinecraftForge.EVENT_BUS.post(
                        SkyblockInventoryItemEvent.ItemStackEvent(
                            SkyblockInventoryItemEvent.EventType.GAINED,
                            countDifference,
                            itemName
                        )
                    )
                } else {
                    MinecraftForge.EVENT_BUS.post(
                        SkyblockInventoryItemEvent.ItemStackEvent(
                            SkyblockInventoryItemEvent.EventType.LOST,
                            -abs(countDifference),
                            itemName
                        )
                    )
                }
                println(itemChangeMessage)
            }
        }

        // Check for items that were removed entirely
        previous.forEach { (displayName, previousCount) ->
            val currentCount = current[displayName] ?: 0
            if (previousCount > 0 && currentCount == 0) {
                val itemChangeMessage = "-$previousCount $displayName"
                MinecraftForge.EVENT_BUS.post(
                    SkyblockInventoryItemEvent.ItemStackEvent(
                        SkyblockInventoryItemEvent.EventType.LOST,
                        -abs(previousCount),
                        displayName
                    )
                )
                println(itemChangeMessage)
            }
        }
    }

    private fun getPreviousItemName(currentInventory: Map<String, Int>): String {
        // Look up the item name from the previous inventory
        val previousItem = previousInventory.entries.find { it.key != "Empty slot" && it.value == 0 }
        return previousItem?.key ?: "Unknown item"
    }


}