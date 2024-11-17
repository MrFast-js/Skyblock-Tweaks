package mrfast.sbt.features.general

import com.google.gson.Gson
import com.google.gson.JsonArray
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.config.categories.DungeonConfig.trashHighlightType
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.guis.GuiItemFilterPopup.*
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.managers.LocationManager
import net.minecraft.client.gui.Gui
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object TrashHighlighter {
    var defaultList = listOf(
        FilteredItem("CRYPT_DREADLORD_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("MACHINE_GUN_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("Healing VIII", FilterType.CONTAINS, InputType.DISPLAY_NAME),
        FilteredItem("DUNGEON_LORE_PAPER", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_BONE", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("CRYPT_BOW", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("(?:SKELETON|ZOMBIE)_(?:GRUNT|MASTER|SOLDIER)_(?:BOOTS|LEGGINGS|CHESTPLATE|HELMET)", FilterType.REGEX, InputType.ITEM_ID),
        FilteredItem("SUPER_HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("INFLATABLE_JERRY", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("DUNGEON_TRAP", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("SNIPER_HELMET", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("SKELETOR", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ROTTEN", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("HEAVY", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("PREMIUM_FLESH", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("TRAINING", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("CONJURING_SWORD", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("FEL_PEARL", FilterType.EQUALS, InputType.ITEM_ID),
        FilteredItem("ZOMBIE_KNIGHT", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("ENCHANTED_ROTTEN_FLESH", FilterType.CONTAINS, InputType.ITEM_ID)
    )
    var trashList = defaultList.toMutableList()

    init {
        val itemTrashFilePath = ConfigManager.modDirectoryPath.resolve("data/itemTrash.json")

        if(itemTrashFilePath.exists()) {
            val profileData = DataManager.loadDataFromFile(itemTrashFilePath)
            val jsonFilters = profileData.getAsJsonArray("filters") ?: JsonArray()
            trashList = Gson().fromJson(jsonFilters, Array<FilteredItem>::class.java).toMutableList()
        }
    }

    @SubscribeEvent
    fun onDrawSlots(event: SlotDrawnEvent.Post) {
        if (!LocationManager.inSkyblock || !event.slot.hasStack || !DungeonConfig.highlightTrash) return

        val stack = event.slot.stack
        val x = event.slot.xDisplayPosition
        val y = event.slot.yDisplayPosition

        if (stack.getSkyblockId() != null && trashList.isNotEmpty()) {
            for (filter in trashList) {
                if(!filter.matches(stack)) continue

                if (trashHighlightType == "Slot") {
                    Gui.drawRect(x, y, x + 16, y + 16, Color(255, 0, 0, 100).rgb)
                }
                if (trashHighlightType == "Border") {
                    Gui.drawRect(x, y, x + 16, y + 1, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x, y, x + 1, y + 16, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x + 15, y, x + 16, y + 16, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x, y + 15, x + 16, y + 16, Color(255, 0, 0, 255).rgb)
                }
                return
            }
        }
    }
}