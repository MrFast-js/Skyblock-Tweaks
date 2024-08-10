package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getRegexGroups
import net.minecraft.client.gui.Gui
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

/*
TODO: Add saving profile data so dont require re opening cake bag every session
 */

@SkyblockTweaks.EventComponent
object NewYearsCakeHelper {
    private val sortedCakeBag = mutableListOf<String>()

    @SubscribeEvent
    fun onSlotClicked(event: SlotClickedEvent) {
        if (event.gui.chestName() != "New Year Cake Bag" || !MiscellaneousConfig.cakeBagSortingHelper) return
        lastClickedSlot = event.slot.slotIndex
    }

    private var lastClickedSlot = 0

    @SubscribeEvent
    fun onSlotDrawPost(event: SlotDrawnEvent.Post) {
        val isCake = event.slot.hasStack && event.slot.stack.getSkyblockId() == "NEW_YEAR_CAKE"

        if (MiscellaneousConfig.highlightMissingNewYearCakes && event.gui.chestName()
                .startsWith("Auctions") && isCake
        ) {
            val cleanedName = event.slot.stack.displayName.clean()
            if (!sortedCakeBag.contains(cleanedName)) {
                Gui.drawRect(
                    event.slot.xDisplayPosition,
                    event.slot.yDisplayPosition,
                    event.slot.xDisplayPosition + 16,
                    event.slot.yDisplayPosition + 16,
                    Color(85, 255, 85, 100).rgb
                )
            }
        }
        if (event.gui.chestName() != "New Year Cake Bag" || !MiscellaneousConfig.cakeBagSortingHelper) return

        if (isCake) {
            val cleanedName = event.slot.stack.displayName.clean()
            if (!sortedCakeBag.contains(cleanedName)) {
                sortedCakeBag.add(cleanedName)
                sortedCakeBag.sortBy {
                    it.getRegexGroups("New Year Cake \\(Year (.*)\\)")?.group(1)?.toInt()
                }
            }
            val idealSlotIndex = sortedCakeBag.indexOf(cleanedName)
            if (event.slot.slotIndex != idealSlotIndex) {
                Gui.drawRect(
                    event.slot.xDisplayPosition,
                    event.slot.yDisplayPosition,
                    event.slot.xDisplayPosition + 16,
                    event.slot.yDisplayPosition + 16,
                    Color(255, 0, 0, 100).rgb
                )
            }
        }


        if (Utils.mc.thePlayer.inventory.itemStack != null) {
            val heldItemClean = Utils.mc.thePlayer.inventory.itemStack.displayName.clean()

            if (sortedCakeBag.contains(heldItemClean)) {
                if (lastClickedSlot == event.slot.slotIndex) {
                    val idealSlotIndex = sortedCakeBag.indexOf(heldItemClean)
                    val idealSlot = event.gui.inventorySlots.getSlot(idealSlotIndex) ?: return

                    Gui.drawRect(
                        idealSlot.xDisplayPosition,
                        idealSlot.yDisplayPosition,
                        idealSlot.xDisplayPosition + 16,
                        idealSlot.yDisplayPosition + 16,
                        Color(85, 255, 85, 255).rgb
                    )
                }
            }
        }
    }


}
