package mrfast.sbt.config.components

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.UIText
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.utils.withIndex
import gg.essential.universal.UMatrixStack
import gg.essential.vigilance.gui.settings.SelectorComponent
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.ConfigManager
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import java.awt.Color
import java.io.File

class GuiItemFilterPopup(title: String) : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    var filters = mutableListOf<FilteredItem>()

    class FilteredItem(
        var textInput: String,
        selectedFilter: FilterType,
        selectedInput: InputType
    ) {
        var itemFilter = ItemFilter()

        init {
            itemFilter.selectedFilter = selectedFilter
            itemFilter.selectedInput = selectedInput
        }

        fun matches(stack: ItemStack): Boolean {
            val input = when (itemFilter.selectedInput) {
                InputType.ITEM_ID -> stack.getSkyblockId() ?: return false
                InputType.DISPLAY_NAME -> stack.displayName
            }

            return when (itemFilter.selectedFilter) {
                FilterType.CONTAINS -> input.contains(textInput)
                FilterType.EQUALS -> input == textInput
                FilterType.REGEX -> input.matches(textInput.toRegex())
            }
        }

    }

    enum class FilterType {
        REGEX,
        EQUALS,
        CONTAINS
    }

    enum class InputType {
        ITEM_ID,
        DISPLAY_NAME
    }

    class ItemFilter {
        var selectedFilter = FilterType.CONTAINS
        var selectedInput = InputType.ITEM_ID
    }

    private var bodyContent = ""
    private var runOnClose: Runnable? = null

    override fun onScreenClose() {
        super.onScreenClose()
        runOnClose?.run()
    }

    fun getContent(): String {
        return bodyContent
    }

    fun setContent(text: String) {
        bodyContent = text
    }

    fun runOnClose(runnable: Runnable) {
        runOnClose = runnable
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (CustomizationConfig.backgroundBlur) GuiUtils.drawBackgroundBlur()
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
    }

    private val mainBorderRadius = 6f

    init {
        filters = loadFiltersFromDataFile() as MutableList<FilteredItem>
        // Create a background panel
        val background =
            OutlinedRoundedRectangle(CustomizationConfig.windowBorderColor.constraint, 2f, mainBorderRadius).constrain {
                color = CustomizationConfig.mainBackgroundColor.constraint
                width = MinConstraint(70.percent, 600.pixels)
                height = MinConstraint(70.percent, 400.pixels)
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf window

        Inspector(background) childOf window

        UIText("§7$title", true).constrain {
            x = CenterConstraint()
            y = 6.pixels
            textScale = 2.pixels
        } childOf background

        val block = UIRoundedRectangle(4f).constrain {
            color = CustomizationConfig.headerBackgroundColor.constraint
            x = CenterConstraint()
            y = SiblingConstraintFixed(4f)
            width = 100.percent - 16.pixels
            height = 90.percent
        } childOf background

        val body = ScrollComponent().constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 100.percent
        } childOf block

        filters.forEach {
            createItemFilterComponent(body, it)
        }

        createAddFilterButton(body)
    }

    private fun createAddFilterButton(parent: UIComponent) {
        val addFilterButton = UIBlock(Color(0x2A2A2A)).constrain {
            x = CenterConstraint()
            y = SiblingConstraint(4f)
            width = 60.percent
            height = 18.pixels
        } childOf parent

        addFilterButton.onMouseClick {
            filters.add(FilteredItem("", FilterType.CONTAINS, InputType.ITEM_ID))
            parent.removeChild(addFilterButton)
            createItemFilterComponent(parent, filters.last())
            parent.addChildren(addFilterButton)
            saveFilters()
        }

        addFilterButton.onMouseEnterRunnable {
            addFilterButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(0x3A3A3A).constraint)
            }
        }
        addFilterButton.onMouseLeaveRunnable {
            addFilterButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(0x2A2A2A).constraint)
            }
        }

        UIText("§7Add Filter", true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf addFilterButton
    }

    private fun createItemFilterComponent(parent: UIComponent, filterItem: FilteredItem) {
        val backgroundBlock = UIBlock(Color(0x1E1E1E)).constrain {
            x = 0.pixels
            y = SiblingConstraint(4f)
            width = 100.percent
            height = 25.pixels
        } childOf parent

        val itemIcon = ItemComponent(ItemStack(Items.sign)).constrain {
            x = 2.pixels
            y = CenterConstraint()
            height = 20.pixels
            width = 20.pixels
        } childOf backgroundBlock

        val textInput = TextInputComponent(filterItem.textInput).constrain {
            width = 25.percent
            x = SiblingConstraint(4f)
            y = CenterConstraint()
        } childOf backgroundBlock

        textInput.onKeyType { typedChar, keyCode ->
            if (textInput.text.contains("§")) textInput.text = textInput.text.replace("§", "&")
            filterItem.textInput = textInput.text
            updateItemIcons(textInput.text, itemIcon)
            saveFilters()
        }
        updateItemIcons(textInput.text, itemIcon)

        val initialIndex = when (filterItem.itemFilter.selectedInput) {
            InputType.ITEM_ID -> 0
            InputType.DISPLAY_NAME -> 1
        }
        val inputTypeDropdown = SelectorComponent(initialIndex, listOf("Item ID", "Display Name")).constrain {
            x = SiblingConstraint(10f)
            y = CenterConstraint()
            width = ChildBasedSizeConstraint()
            height = 17.pixels
        } childOf backgroundBlock effect OutlineEffect(Color(85, 255, 85), 1f)

        inputTypeDropdown.onValueChange { value: Any? ->
            if (value == 1) {
                filterItem.itemFilter.selectedInput = InputType.DISPLAY_NAME
            } else {
                filterItem.itemFilter.selectedInput = InputType.ITEM_ID
            }
            saveFilters()
        }

        val deleteFilterButton = UIBlock(Color(0x2A2A2A)).constrain {
            x = 6.pixels(true)
            y = CenterConstraint()
            width = 18.pixels
            height = 18.pixels
        } childOf backgroundBlock effect OutlineEffect(CustomizationConfig.defaultCategoryColor, 1f)

        UIText("§c✖", true).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf deleteFilterButton

        deleteFilterButton.onMouseEnterRunnable {
            deleteFilterButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(0x522020).constraint)
            }
        }
        deleteFilterButton.onMouseLeaveRunnable {
            deleteFilterButton.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(0x2A2A2A).constraint)
            }
        }
        deleteFilterButton.onMouseClick {
            filters.remove(filterItem)
            parent.removeChild(backgroundBlock)
            saveFilters()
        }

        val buttonContainer = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = SiblingConstraint(6f, true)
            y = CenterConstraint()
            width = 38.percent
            height = 100.percent
        } childOf backgroundBlock

        createButton(
            "REGEX",
            buttonContainer,
            selected = (filterItem.itemFilter.selectedFilter == FilterType.REGEX)
        ) {
            filterItem.itemFilter.selectedFilter = FilterType.REGEX
            saveFilters()
            println("REGEX")
        }
        createButton(
            "CONTAINS",
            buttonContainer,
            selected = (filterItem.itemFilter.selectedFilter == FilterType.CONTAINS)
        ) {
            filterItem.itemFilter.selectedFilter = FilterType.CONTAINS
            saveFilters()
            println("CONTAINS")
        }
        createButton(
            "EQUALS",
            buttonContainer,
            selected = (filterItem.itemFilter.selectedFilter == FilterType.EQUALS)
        ) {
            filterItem.itemFilter.selectedFilter = FilterType.EQUALS
            saveFilters()
            println("equals")
        }
    }

    private fun createButton(
        buttonText: String,
        parent: UIComponent,
        selected: Boolean,
        runnable: Runnable,
    ): UIBlock {
        val button = UIBlock(Color.DARK_GRAY).constrain {
            width = 33.percent
            height = 16.pixels
            x = SiblingConstraint(4f, true)
            y = CenterConstraint()
        } childOf parent effect OutlineEffect(CustomizationConfig.defaultCategoryColor, 1f)

        button.onMouseEnterRunnable {
            button.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color(0x2D2D2D).constraint)
            }
        }
        button.onMouseLeaveRunnable {
            button.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, Color.DARK_GRAY.constraint)
            }
        }

        UIText(buttonText).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf button

        if (selected) {
            button.enableEffect(OutlineEffect(CustomizationConfig.selectedCategoryColor, 1f))
        }

        button.onMouseClick {
            runnable.run()
            parent.children.forEach {
                it.enableEffect(OutlineEffect(CustomizationConfig.defaultCategoryColor, 1f))
            }
            button.enableEffect(OutlineEffect(CustomizationConfig.selectedCategoryColor, 1f))
        }

        return button
    }

    private fun updateItemIcons(text: String, itemIcon: ItemComponent) {
        if (ItemApi.getItemInfo(text) != null) {
            itemIcon.stack = ItemApi.createItemStack(text) ?: return
        } else if (ItemApi.getItemIdFromName(text, true) != null) {
            val id = ItemApi.getItemIdFromName(text, true)!!
            itemIcon.stack = ItemApi.createItemStack(id) ?: return
        } else {
            itemIcon.stack = ItemStack(Items.sign)
        }
    }


    private fun loadFiltersFromDataFile(): List<FilteredItem> {
        val blacklistFilePath = ConfigManager.modDirectoryPath.resolve("itemBlacklist.json")
        val profileData = DataManager.loadDataFromFile(blacklistFilePath)
        val jsonFilters = profileData.getAsJsonArray("filters") ?: return (emptyList<FilteredItem>().toMutableList())
        return Gson().fromJson(jsonFilters, Array<FilteredItem>::class.java).toMutableList()
    }

    private fun saveFilters() {
        val blacklistFilePath = ConfigManager.modDirectoryPath.resolve("itemBlacklist.json")
        val gson = GsonBuilder().setPrettyPrinting().create()
        val newData = JsonObject()
        val jsonFilters = gson.toJsonTree(filters).asJsonArray // Convert filters to JsonArray
        newData.add("filters", jsonFilters) // Add filters JsonArray to the JsonObject
        DataManager.saveDataToFile(blacklistFilePath, newData)
    }
}