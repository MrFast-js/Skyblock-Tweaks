package mrfast.sbt.managers

import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs


object InventoryItemManager {
    private var previousInventory = mutableMapOf<String, Int>()
    private var preOpenInventory = mutableMapOf<String, Int>()
    private var isOpen = false
    private var ignoreStacksRegex = listOf("^§8Quiver.*","^§aSkyBlock Menu §7\\(Click\\)","^§bMagical Map")

    @SubscribeEvent
    fun onTickEvent(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationUtils.inSkyblock || Utils.mc.theWorld == null) return

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
        isOpen = false
    }

    private fun getCurrentInventoryState(): Map<String, Int> {
        val inventoryState = mutableMapOf<String, Int>()
        val mainInventory = Utils.mc.thePlayer.inventory.mainInventory

        loop@ for (element in mainInventory) {
            var displayName = element?.displayName ?: "Empty slot"
            // Filters
            for (regex in ignoreStacksRegex) {
                if (displayName.matches(regex)) {
                    continue@loop
                }
            }

            // npc shop fix
            val npcSellingStackRegex = "(.*) §8x\\d+"
            if(displayName.matches(npcSellingStackRegex)) {
                displayName = displayName.getRegexGroups(npcSellingStackRegex)?.group(1)?:continue
            }

            val itemCount = element?.stackSize ?: 0
            inventoryState.merge(displayName, itemCount, Int::plus)
        }

        return inventoryState
    }

    private fun compareInventories(previous: Map<String, Int>, current: Map<String, Int>) {
        if(previous.isEmpty() || current.isEmpty()) return

        // Compare current inventory with previous inventory
        current.forEach { (displayName, currentCount) ->
            val previousCount = previous[displayName] ?: 0
            val countDifference = currentCount - previousCount

            // Check if there is a change in count
            if (countDifference != 0) {
                val itemName = if (displayName != "Empty slot") displayName else getPreviousItemName(current)

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
            }
        }

        // Check for items that were removed entirely
        previous.forEach { (displayName, previousCount) ->
            val currentCount = current[displayName] ?: 0
            if (previousCount > 0 && currentCount == 0) {
                MinecraftForge.EVENT_BUS.post(
                    SkyblockInventoryItemEvent.ItemStackEvent(
                        SkyblockInventoryItemEvent.EventType.LOST,
                        -abs(previousCount),
                        displayName
                    )
                )
            }
        }
    }

    private fun getPreviousItemName(currentInventory: Map<String, Int>): String {
        // Look up the item name from the previous inventory
        val previousItem = previousInventory.entries.find { it.key != "Empty slot" && it.value == 0 }
        return previousItem?.key ?: "Unknown item"
    }
}