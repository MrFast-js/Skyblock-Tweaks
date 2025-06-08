package mrfast.sbt.features.general

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getRegexGroups
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object NewYearsCakeHelper {
    private var cakebagJson = JsonObject()
    private var cakebagArray = JsonArray()
    private var sortedCakebagArray = JsonArray()

    @SubscribeEvent
    fun onProfileSwap(event: ProfileLoadEvent?) {
        cakebagJson = DataManager.getProfileDataDefault("cakebag", JsonObject()) as JsonObject
        cakebagArray = cakebagJson.getAsJsonArray("bag") ?: JsonArray()
        sortCakeBag()
    }

    @SubscribeEvent
    fun onSlotClicked(event: SlotClickedEvent) {
        if (event.gui.chestName() != "New Year Cake Bag" || !MiscellaneousConfig.cakeBagSortingHelper) return
        lastClickedSlot = event.slot.slotIndex
        cakebagJson.add("bag", cakebagArray)
        DataManager.saveProfileData("cakebag", cakebagJson)
    }

    private fun sortCakeBag() {
        // Convert JsonArray to a List, sort by the extracted year, and convert back to JsonArray
        sortedCakebagArray = cakebagArray
            .map { it.asString }
            .sortedBy { it.getRegexGroups(newYearCakeRegex)!![1]!!.value.toInt() }
            .let { sortedList ->
                JsonArray().apply {
                    sortedList.forEach { add(JsonPrimitive(it)) }
                }
            }
    }

    private var lastClickedSlot = 0
    private val newYearCakeIDRegex = """NEW_YEAR_CAKE-.*""".toRegex()
    private val newYearCakeRegex = """New Year Cake \(Year (.*)\)""".toRegex()

    @SubscribeEvent
    fun onSlotDrawPost(event: SlotDrawnEvent.Post) {
        if(!MiscellaneousConfig.cakeBagSortingHelper && !MiscellaneousConfig.highlightMissingNewYearCakes) return

        val isCake = event.slot.stack?.getSkyblockId()?.matches(newYearCakeIDRegex) ?: false
        val inCakeBag = event.gui.chestName() == "New Year Cake Bag"

        // Highlight held item to show where it should go
        if (inCakeBag && Utils.getPlayer()!!.inventory.itemStack != null && MiscellaneousConfig.cakeBagSortingHelper) {
            val heldItemClean = JsonPrimitive(Utils.getPlayer()!!.inventory.itemStack.displayName.clean())

            if (cakebagArray.contains(heldItemClean)) {
                if (lastClickedSlot == event.slot.slotIndex) {
                    val idealSlotIndex = sortedCakebagArray.indexOf(heldItemClean)
                    val idealSlot = event.gui.inventorySlots.getSlot(idealSlotIndex) ?: return

                    GuiUtils.highlightSlot(idealSlot, Color(85, 255, 85, 100))
                }
            }
        }

        if(!isCake) return

        val cleanedName = JsonPrimitive(event.slot.stack.displayName.clean())

        // Highlight slots that are not in the correct order
        val idealSlotIndex = sortedCakebagArray.indexOf(cleanedName)
        if (inCakeBag && event.slot.slotIndex != idealSlotIndex && MiscellaneousConfig.cakeBagSortingHelper) {
            GuiUtils.highlightSlot(event.slot, Color(255, 0, 0, 100))
        }

        // Highlight missing cakes
        if (event.gui.chestName().startsWith("Auctions") && MiscellaneousConfig.highlightMissingNewYearCakes) {
            if (!cakebagArray.contains(cleanedName)) {
                GuiUtils.highlightSlot(event.slot, Color(85, 255, 85, 100))
            }
        }

        // Add missing cakes to cakebag
        if (inCakeBag && !cakebagArray.contains(cleanedName)) {
            cakebagArray.add(cleanedName)
            sortCakeBag()
        }
    }
}
