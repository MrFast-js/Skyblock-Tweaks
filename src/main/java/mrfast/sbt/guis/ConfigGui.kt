package mrfast.sbt.guis

import gg.essential.api.utils.GuiUtil
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import gg.essential.universal.UMatrixStack
import gg.essential.vigilance.gui.settings.SelectorComponent
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.ConfigType
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig.showInspector
import mrfast.sbt.guis.components.*
import mrfast.sbt.managers.VersionManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils.drawBackgroundBlur
import mrfast.sbt.utils.GuiUtils.resetBlurAnimation
import mrfast.sbt.utils.SocketUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.client.config.GuiUtils
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.util.*


class ConfigGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    companion object {
        var listeningForKeybind = false
        var searchQuery = ""

        fun openConfigSearch(query: String) {
            searchQuery = query
            GuiUtil.open(ConfigGui())
        }
    }

    private var mainBackgroundColorState = BasicState(CustomizationConfig.mainBackgroundColor)
    private var sidebarBackgroundColorState = BasicState(CustomizationConfig.sidebarBackgroundColor)
    private var guiLineColorsState = BasicState(CustomizationConfig.guiLineColors)
    private var mainBorderColorState = BasicState(CustomizationConfig.windowBorderColor)
    private var defaultCategoryColorState = BasicState(CustomizationConfig.defaultCategoryColor)
    private var selectedCategoryColorState = BasicState(CustomizationConfig.selectedCategoryColor)
    private var hoveredCategoryColorState = BasicState(CustomizationConfig.hoveredCategoryColor)
    private var featureBackgroundColorState = BasicState(CustomizationConfig.featureBackgroundColor)
    private var headerBackgroundColorState = BasicState(CustomizationConfig.headerBackgroundColor)
    private var featureBorderColorState = BasicState(CustomizationConfig.featureBorderColor)

    private var updateSymbol = BasicState(UIText())
    private var showUpdateButton = VersionManager.neededUpdate.versionName.isNotEmpty()
    private var selectedCategory = "General"
    private var selectedCategoryComponent: UIComponent? = null
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var animationFinished = false


    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (CustomizationConfig.backgroundBlur) drawBackgroundBlur()
        else {
            resetBlurAnimation()
        }

        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        // Dont draw tooltips on opening animation
        if (!animationFinished) return

        for (element in tooltipElements.keys) {
            if (element.isHovered()) {
                GuiUtils.drawHoveringText(
                    tooltipElements[element]?.toMutableList()
                        ?: mutableListOf(),
                    mouseX,
                    mouseY,
                    window.getWidth().toInt(),
                    window.getHeight().toInt(),
                    -1,
                    Minecraft.getMinecraft().fontRendererObj
                )
            }
        }
    }

    override fun afterInitialization() {
        super.afterInitialization()
        resetBlurAnimation()
        animationFinished = false
        Utils.setTimeout({
            animationFinished = true
        }, 500)
    }

    private var dynamicColorUpdateTimer = Timer()

    override fun onScreenClose() {
        super.onScreenClose()
        SkyblockTweaks.config.saveConfig()
        updateBlinkyTimer.cancel()
        dynamicColorUpdateTimer.cancel()
    }

    private val mainBorderRadius = 6f;

    init {

        // Create a background panel
        val background = OutlinedRoundedRectangle(mainBorderColorState.get().colorState.constraint, 2f, mainBorderRadius).constrain {
            color = mainBackgroundColorState.get().colorState.constraint
            width = 4.pixels
            height = 4.pixels
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf window

        background.animate {
            setWidthAnimation(Animations.IN_OUT_EXP, 0.25f, MinConstraint(70.percent, 600.pixels))
            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, MinConstraint(70.percent, 400.pixels), 0.2f)
        }

        dynamicColorUpdateTimer.cancel()
        updateGuiColors()
        updateThemeColors()

        if (showInspector) {
            Inspector(background) childOf window
        }
        // Use 70% width, max 600px

        val header = UIRoundedRectangle(mainBorderRadius - 1).constrain {
            width = 100.percent - 4.pixels
            height = min(30.pixels, 10.percent)
            x = 2.pixels()
            y = 2.pixels()
            color = headerBackgroundColorState.get().colorState.constraint
        } childOf background effect ScissorEffect()

        if (CustomizationConfig.developerMode) {
            val statusColor = if (SocketUtils.socketConnected) Color(85, 255, 85) else Color(255, 85, 85)

            val socketStatusButton = OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
                color = mainBackgroundColorState.get().colorState.constraint
                width = 16.pixels
                height = 16.pixels
                x = 8.pixels
                y = CenterConstraint()
            } childOf header

            val socketStatusNew = UIText("∞", false).constrain {
                x = CenterConstraint() + 1.pixels
                y = CenterConstraint()
                color = statusColor.constraint
                textScale = 1.6.pixels
            } childOf socketStatusButton


            val lore = mutableSetOf(
                if (SocketUtils.socketConnected) "§a∞ Connected to SBT Socket!" else "§c✕ Disconnected from SBT Socket!"
            )

            if (!SocketUtils.socketConnected) {
                socketStatusButton.onMouseClick {
                    SocketUtils.setupSocket()
                    ChatUtils.sendClientMessage("§eRe-attempting socket connection...", prefix = true)
                    Utils.mc.displayGuiScreen(null)
                }
                lore.add("§eClick to re-attempt connection")
            }

            socketStatusButton.addTooltip(lore)
        }

        val editGuiLocationsButton = OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
            width = 16.pixels
            height = 16.pixels
            x = if (CustomizationConfig.developerMode) SiblingConstraintFixed(8f, false) else 8.pixels
            y = CenterConstraint()
            color = mainBackgroundColorState.get().colorState.constraint
        } childOf header
        val editGuiLocationsSymbol = UIText("✎").constrain {
            x = CenterConstraint();y = CenterConstraint();color = Color(0xFFFF55).constraint
        } childOf editGuiLocationsButton
        editGuiLocationsSymbol.setTextScale(2.pixels)
        editGuiLocationsButton.onMouseClick { GuiUtil.open(GuiEditor()) }
        editGuiLocationsButton.addTooltip(setOf("§eEdit Gui Locations"))

        // Add some text to the panel
        val modTitle = UIText("§eSkyblock §9Tweaks", false).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf header
        modTitle.setTextScale(2.5.pixels)

        val modVersion = UIText("§7${SkyblockTweaks.MOD_VERSION}").constrain {
            x = SiblingConstraintFixed(4f)
            y = SiblingConstraintFixed(4f) - 10f.pixels()
        } childOf header

        modVersion.setTextScale(0.75.pixels)

        val searchBar = OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
            color = mainBackgroundColorState.get().colorState.constraint
            width = MinConstraint(12.percent, 100.pixels)
            height = 12.pixels
            x = PixelConstraint(10f, true)
            y = CenterConstraint()
        } childOf header

        val searchBarInput = UITextInput("Search").constrain {
            width = 100.percent
            height = 8.pixels
            x = CenterConstraint() + 1.pixels
            y = CenterConstraint()
        } childOf searchBar

        searchBarInput.setColor(Color.GRAY)
        searchBarInput.grabWindowFocus()

        header.onMouseClick {
            if (!searchBarInput.isActive()) {
                searchBarInput.grabWindowFocus()
            }
        }

        if (showUpdateButton) {
            val updateButton = OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
                color = mainBackgroundColorState.get().colorState.constraint
                width = 16.pixels
                height = 16.pixels
                x = SiblingConstraintFixed(15f, true)
                y = CenterConstraint()
            } childOf header

            val updateSymbolNew = UIText("⬆").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf updateButton
            updateSymbolNew.setTextScale(2.pixels)

            animateUpdateButton()
            updateSymbol.set(updateSymbolNew)

            updateButton.onMouseClick {
                // do update command
                VersionManager.checkIfNeedUpdate()
                Utils.mc.currentScreen = null
            }
            updateButton.addTooltip(setOf("§aUpdate §e${VersionManager.neededUpdate.versionName}§a is available! Click to download"))
        }

        val categoryListBackground = UIRoundedRectangle(mainBorderRadius - 1).constrain {
            x = 2.pixels
            y = 34.pixels
            width = 21.percent - 4.pixels
            height = 100.percent - 32.pixels - 4.pixels
            color = sidebarBackgroundColorState.get().colorState.constraint
        } childOf background effect ScissorEffect()

        val categoryList = ScrollComponent("").constrain {
            x = 0.pixels
            y = 8.pixels
            width = 100.percent
            height = 100.percent
        } childOf categoryListBackground

        val scrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = PixelConstraint(0f, alignOpposite = true)
            color = Color(200, 200, 200, 200).constraint
        } childOf categoryListBackground

        categoryList.setVerticalScrollBarComponent(scrollbar, true)

        val featureListBackground = UIContainer().constrain {
            x = 21.percent + 1.pixels
            y = 40.pixels
            width = 79.percent - 3.pixels
            height = 100.percent - 40.pixels
        } childOf background effect ScissorEffect()
        val featureList = ScrollComponent("No features by that name found :(").constrain {
            x = 21.percent + 2.pixels
            y = 32.pixels
            width = 79.percent - 12.pixels
            height = 100.percent - 34.pixels
        } childOf background

        val featureScrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = PixelConstraint(0f, alignOpposite = true)
            color = Color(200, 200, 200, 200).constraint
        } childOf featureListBackground

        featureList.setVerticalScrollBarComponent(featureScrollbar, true)

        if (searchQuery.isNotEmpty()) {
            Utils.setTimeout({
                searchBarInput.setText(searchQuery)
                updateSelectedFeatures(featureList)
            }, 200)
        }

        searchBarInput.onKeyType { _, keycode ->
            if (keycode == Keyboard.KEY_ESCAPE) {
                Utils.mc.displayGuiScreen(null)
                return@onKeyType
            }
            searchQuery = searchBarInput.getText()
            updateSelectedFeatures(featureList)
        }

        for ((count, category) in ConfigManager.categories.values.withIndex()) {
            val actualY = if (count == 0) 10.pixels else SiblingConstraintFixed(3f)

            // Stop developer tab from showing if not in developer mode
            if (category.name == "§eDeveloper" && !CustomizationConfig.developerMode) continue

            val categoryComponent = UIText(category.name).constrain {
                x = CenterConstraint()
                y = actualY
                height = 8.pixels
                color = defaultCategoryColorState.get().colorState.constraint
            } childOf categoryList

            if (selectedCategory == category.name) {
                updateSelectedCategoryColor(categoryComponent, category.name)
                updateSelectedFeatures(featureList)
            }
            categoryComponent.setTextScale(1.6.pixels)

            categoryComponent.onMouseEnter {
                // Don't do hover colors if already colored
                if (selectedCategory != category.name) {
                    if (!categoryComponent.getColor().equals(hoveredCategoryColorState.get().colorState.constraint)) {
                        categoryComponent.animate {
                            setColorAnimation(Animations.OUT_EXP, 0.2f, hoveredCategoryColorState.get().colorState.constraint)
                        }
                    }
                }
            }
            categoryComponent.onMouseLeave {
                // Don't do hover colors if already colored
                if (selectedCategory != category.name) {
                    if (!categoryComponent.getColor().equals(defaultCategoryColorState.get().colorState.constraint)) {
                        categoryComponent.animate {
                            setColorAnimation(Animations.OUT_EXP, 0.2f, defaultCategoryColorState.get().colorState.constraint)
                        }
                    }
                }
            }

            categoryComponent.onMouseClick {
                updateSelectedCategoryColor(categoryComponent, category.name)
                updateSelectedFeatures(featureList)
                searchQuery = ""
                SkyblockTweaks.config.saveConfig()
            }
        }
    }

    private var updateBlinkyTimer = Timer()

    private fun animateUpdateButton() {
        val colors = listOf(Color(85, 255, 85), Color(16, 100, 16))
        var currentColorIndex = 0
        updateBlinkyTimer = Timer()
        updateBlinkyTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // blinky arrow
                updateSymbol.get().animate {
                    setColorAnimation(Animations.OUT_EXP, 0.3f, colors[currentColorIndex].constraint)
                }

                // Toggle to the next color index
                currentColorIndex = (currentColorIndex + 1) % colors.size
            }
        }, 0, 350)
    }

    private fun updateGuiColors() {
        dynamicColorUpdateTimer = Timer()
        dynamicColorUpdateTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                mainBackgroundColorState.set(CustomizationConfig.mainBackgroundColor)
                sidebarBackgroundColorState.set(CustomizationConfig.sidebarBackgroundColor)
                guiLineColorsState.set(CustomizationConfig.guiLineColors)
                mainBorderColorState.set(CustomizationConfig.windowBorderColor)
                defaultCategoryColorState.set(CustomizationConfig.defaultCategoryColor)
                selectedCategoryColorState.set(CustomizationConfig.selectedCategoryColor)
                hoveredCategoryColorState.set(CustomizationConfig.hoveredCategoryColor)
                featureBackgroundColorState.set(CustomizationConfig.featureBackgroundColor)
                headerBackgroundColorState.set(CustomizationConfig.headerBackgroundColor)
                featureBorderColorState.set(CustomizationConfig.featureBorderColor)
            }
        }, 0, 100)
    }


    private fun updateSelectedCategoryColor(new: UIComponent, name: String) {
        selectedCategoryComponent?.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, defaultCategoryColorState.get().colorState.constraint)
        }
        new.animate {
            setColorAnimation(Animations.OUT_EXP, 0.2f, selectedCategoryColorState.get().colorState.constraint)
        }
        selectedCategoryComponent = new
        selectedCategory = name
    }

    private fun updateSelectedFeatures(list: ScrollComponent) {
        list.clearChildren()

        var drawnCategories = 0
        for (category in ConfigManager.categories) {
            // If searching something, ignore selected category
            if (category.key != selectedCategory && searchQuery.isEmpty()) continue

            for (subcategory in category.value.subcategories.values) {
                val actualY = if (drawnCategories == 0) 12.pixels else SiblingConstraintFixed(6f)
                var drawnFeatures = 0

                val subcategoryComponent = UIContainer().constrain {
                    height = ChildBasedSizeConstraint() + 20.pixels
                    width = 100.percent
                    x = 0.pixels
                    y = actualY
                }

                val subcategoryTitle = UIText(subcategory.name).constrain {
                    x = CenterConstraint()
                    y = 0.pixels
                    textScale = 1.8.pixels
                } childOf subcategoryComponent

                val comparator = compareByDescending<String> { it }
                val sortedFeatures = subcategory.features.toSortedMap(comparator)

                for (feature in sortedFeatures.values) {
                    val isChild = feature.parentName.isNotEmpty()
                    if (isChild) continue

                    val featureContainer = createFeatureElement(feature, subcategory, subcategoryComponent, list)

                    if (featureContainer != null) {
                        feature.featureContainer = featureContainer
                        drawnFeatures++
                    }
                }
                // Don't draw subcategory title if no features
                if (drawnFeatures != 0) {
                    subcategoryComponent childOf list
                }
                drawnCategories++
            }
        }
        // Populate suboptions
        for (category in ConfigManager.categories) {
            for (subcategory in category.value.subcategories.values) {
                for (feature in subcategory.features.values) {
                    if (feature.parentName.isNotEmpty()) {
                        val parent = subcategory.features.values.find {
                            it.name == feature.parentName
                        }
                        if (parent != null) {
                            val featureOption =
                                createFeatureOptionElement(feature, subcategory, parent)
                            if (featureOption != null) {
                                parent.optionElements[feature.name] = featureOption
                                featureOption.hide(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createFeatureElement(
        feature: ConfigManager.Feature,
        subcategory: ConfigManager.Subcategory,
        subcategoryComponent: UIComponent,
        featureList: ScrollComponent
    ): UIContainer? {
        // Check if name, description or subcategory contain the search

        if (!(containsIgnoreCase(feature.name, searchQuery) ||
                    containsIgnoreCase(feature.description, searchQuery) ||
                    containsIgnoreCase(subcategory.name, searchQuery) ||
                    containsIgnoreCase(feature.parentName, searchQuery))
        ) {
            var hasChildFittingSearch = false
            for (optionElement in feature.optionElements) {
                if (containsIgnoreCase(optionElement.key, searchQuery)) {
                    hasChildFittingSearch = true
                }
            }
            if (!hasChildFittingSearch) {
                return null
            }
        }

        val featureContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraintFixed(6f)
            width = 99.percent
            height = ChildBasedSizeConstraint(2f)
        } childOf subcategoryComponent effect ScissorEffect()

        val featureBackground = OutlinedRoundedRectangle(featureBorderColorState.get().colorState.constraint, 1f, 6f).constrain {
            x = CenterConstraint()
            y = SiblingConstraintFixed(6f)
            width = if (feature.parentName.isEmpty()) 100.percent else 90.percent
            height = ChildBasedSizeConstraint(2f)
            color = featureBackgroundColorState.get().colorState.constraint
        } childOf featureContainer

        val secondContainer = UIContainer().constrain {
            x = PixelConstraint(0f)
            y = SiblingConstraintFixed(6f)
            width = 100.percent
            height = ChildBasedSizeConstraint(2f)
        } childOf featureBackground

        val featureTitle = UIText(feature.name).constrain {
            x = 3.pixels
            y = 3.pixels
            textScale = 1.5.pixels
        } childOf secondContainer

        if (feature.description != "") {
            val featureDescription = UIWrappedText(feature.description).constrain {
                x = 3.pixels
                y = SiblingConstraintFixed(2f)
                width = 80.percent - 2.pixels
                color = Color.GRAY.constraint
            } childOf secondContainer
        }

        populateFeature(feature, secondContainer)

        return featureContainer
    }

    private fun createFeatureOptionElement(
        feature: ConfigManager.Feature,
        subcategory: ConfigManager.Subcategory,
        parent: ConfigManager.Feature?
    ): UIContainer? {
        // Check if name, description or subcategory contain the search
        val parentComponent = parent?.featureContainer ?: return null

        if (!(containsIgnoreCase(feature.name, searchQuery) ||
                    containsIgnoreCase(feature.description, searchQuery) ||
                    containsIgnoreCase(subcategory.name, searchQuery) ||
                    containsIgnoreCase(feature.parentName, searchQuery) ||
                    containsIgnoreCase(parent.description, searchQuery)
                    )
        ) {
            var hasChildFittingSearch = false
            for (optionElement in parent.optionElements) {
                if (containsIgnoreCase(optionElement.key, searchQuery)) {
                    hasChildFittingSearch = true
                }
            }
            if (!hasChildFittingSearch) {
                return null
            }
        }

        val child1 = parentComponent.children.getOrNull(0)
        val parentFeatureBackground = child1 ?: return null
        val featureContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraintFixed(6f)
            width = 100.percent
            height = ChildBasedSizeConstraint(2f)
            color = Color.RED.constraint
        } childOf parentFeatureBackground

        val optionName = UIText(feature.name).constrain {
            x = 2.pixels
            y = 2.pixels
            textScale = 1.5.pixels
        } childOf featureContainer

        if (feature.description.isNotEmpty()) {
            val optionDescription = UIWrappedText(feature.description).constrain {
                x = 2.pixels
                y = SiblingConstraintFixed(2f)
                width = 80.percent - 2.pixels
                color = Color.GRAY.constraint
                textScale = 1.pixels
            } childOf featureContainer
        } else {
            optionName.setY(CenterConstraint())
        }

        populateFeature(feature, featureContainer)

        return featureContainer
    }

    private fun containsIgnoreCase(source: String, target: String): Boolean {
        return source.lowercase(Locale.getDefault()).contains(target.lowercase(Locale.getDefault()))
    }

    var floatingColorPicker: ColorPickerComponent? = null
    private fun populateFeature(
        feature: ConfigManager.Feature,
        featureComponent: UIComponent
    ) {
        val ignoredHeights = mutableListOf<UIComponent>()
        try {
            if (feature.type == ConfigType.TOGGLE) {
                val toggleSwitch = ToggleSwitchComponent(feature.value as Boolean).constrain {
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent
                toggleSwitch.onMouseClick {
                    feature.field.set(SkyblockTweaks.config, toggleSwitch.activated)
                }
                ignoredHeights.add(toggleSwitch)
            }
            if (feature.type == ConfigType.LABEL) {
                val invisibleBox = UIBlock(Color(0, 0, 0, 0)).constrain {
                    width = 20.pixels
                    height = 18.pixels
                    y = CenterConstraint()
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent
                ignoredHeights.add(invisibleBox)
            }
            if (feature.type == ConfigType.NUMBER) {
                val numberInput = NumberInputComponent(feature.value as Int).constrain {
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent
                numberInput.onKeyType { typedChar, keyCode ->
                    feature.field.set(SkyblockTweaks.config, numberInput.intValue)
                }
                ignoredHeights.add(numberInput)
            }
            if (feature.type == ConfigType.TEXT) {
                val textInput = TextInputComponent(feature.value as String).constrain {
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent

                textInput.onKeyType { typedChar, keyCode ->
                    feature.field.set(SkyblockTweaks.config, textInput.text)
                }
                ignoredHeights.add(textInput)

            }
            if (feature.type == ConfigType.COLOR) {
                val customColor = if (feature.value is Color) CustomColor(feature.value as Color) else (feature.value as CustomColor)

                val colorDisplay = UIBlock(customColor.colorState).constrain {
                    width = 28.pixels
                    height = 16.pixels
                    y = CenterConstraint()
                    x = 10.pixels(true)
                } childOf featureComponent effect OutlineEffect(Color.YELLOW, 1f)

                val colorPicker = ColorPickerComponent(customColor,colorDisplay).constrain {
                    x = 10.pixels(true)
                } childOf featureComponent

                colorPicker.hide(true)

                var hidden = true;
                colorDisplay.onMouseClick {
                    hidden = !hidden
                    if (hidden) {
                        floatingColorPicker = null
                        colorPicker.setFloating(false)
                        colorPicker.hide(true)
                    } else {
                        clearPopup()
                        colorPicker.setY(SiblingConstraint(5f))
                        floatingColorPicker = colorPicker
                        colorPicker.setFloating(true)
                        colorPicker.unhide(false)
                    }
                }

                val unhovered = Color(200, 200, 200)
                val hovered = Color(255, 255, 255)

                val resetImg = UIImage.ofResource("/assets/skyblocktweaks/gui/reset.png").constrain {
                    width = 10.pixels
                    height = 11.pixels
                    y = CenterConstraint()
                    x = SiblingConstraintFixed(3f, true)
                    color = unhovered.constraint
                } childOf featureComponent

                resetImg.onMouseEnterRunnable {
                    resetImg.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, hovered.constraint)
                    }
                }
                resetImg.onMouseLeaveRunnable {
                    resetImg.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, unhovered.constraint)
                    }
                }

                resetImg.onMouseClick {
                    val defaultValue = ConfigManager.defaultMap[feature.field.name] ?: Color.GRAY

                    feature.field.set(SkyblockTweaks.config, defaultValue)
                    if(defaultValue is Color) {
                        colorDisplay.setColor(defaultValue)
                        colorPicker.setPickerColor(defaultValue)
                        customColor.colorState.set(defaultValue)
                    }
                    if(defaultValue is CustomColor) {
                        customColor.colorState.set(defaultValue.initialColor)
                        colorDisplay.setColor(defaultValue.initialColor)
                        colorPicker.setPickerColor(defaultValue.initialColor)
                    }
                }
                colorPicker.onValueChange { value: Any? ->
                    if(colorPicker.chroma) {
                        colorDisplay.setColor(mrfast.sbt.utils.GuiUtils.rainbowColor.constraint)
                    } else {
                        colorDisplay.setColor((value as CustomColor).get())
                    }
                    feature.field.set(SkyblockTweaks.config, (value as CustomColor))
                }

                ignoredHeights.addAll(mutableListOf(colorDisplay, resetImg, colorPicker))
            }
            if (feature.type == ConfigType.DROPDOWN) {
                var selected = feature.dropdownOptions.indexOf(feature.value)
                if (selected == -1) selected = 0
                val selector = SelectorComponent(selected, feature.dropdownOptions.toList()).constrain {
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent

                selector.onValueChange { value: Any? ->
                    feature.field.set(SkyblockTweaks.config, feature.dropdownOptions[value as Int])

                    if (feature.field.name == "selectedTheme") {
                        updateThemeColors()
                    }
                }
                ignoredHeights.add(selector)
            }
            if (feature.type == ConfigType.KEYBIND) {
                val button = UIBlock(Color.DARK_GRAY).constrain {
                    width = 70.pixels
                    height = 18.pixels
                    y = CenterConstraint()
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent effect OutlineEffect(CustomizationConfig.onSwitchColor.colorState.get(), 1f)

                button.onMouseEnterRunnable {
                    button.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, featureBackgroundColorState.get().colorState.constraint)
                    }
                }
                button.onMouseLeaveRunnable {
                    button.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, Color.DARK_GRAY.constraint)
                    }
                }
                ignoredHeights.add(button)

                val resetImg = UIImage.ofResource("/assets/skyblocktweaks/gui/reset.png").constrain {
                    width = 10.pixels
                    height = 11.pixels
                    y = CenterConstraint()
                    x = SiblingConstraintFixed(3f, true)
                } childOf featureComponent
                ignoredHeights.add(resetImg)

                val keycode = feature.value as Int
                var currentKey = "NONE"
                if (keycode != -1) {
                    currentKey = Keyboard.getKeyName(keycode)
                }
                val buttonText = UIText(currentKey).constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                } childOf button

                button.onMouseClickConsumer {
                    if (listeningForKeybind) return@onMouseClickConsumer
                    listeningForKeybind = true
                    // Set listening style, similar to minecrafts keybind system
                    buttonText.setText("§r> §e" + buttonText.getText() + "§r <")

                    Thread {
                        var keyPressed = false
                        while (!keyPressed && listeningForKeybind) {
                            for (i in 0 until Keyboard.KEYBOARD_SIZE) {
                                if (Keyboard.isKeyDown(i)) {
                                    Utils.setTimeout({
                                        listeningForKeybind = false
                                    }, 100)

                                    val newKeyName = Keyboard.getKeyName(i)

                                    // Reset if ESCAPE is pressed
                                    if (i == 1) {
                                        buttonText.setText("NONE")
                                        listeningForKeybind = false
                                        break
                                    }
                                    buttonText.setText(newKeyName)
                                    keyPressed = true
                                    feature.field.set(SkyblockTweaks.config, i)

                                    break
                                }
                            }

                            // Add a small delay to avoid excessive CPU usage
                            try {
                                Thread.sleep(100)
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    }.start()
                }

                resetImg.onMouseClick {
                    val defaultValue = ConfigManager.defaultMap[feature.field.name]
                    var newCurrentKey: String? = "NONE"
                    if (defaultValue != -1) newCurrentKey = Keyboard.getKeyName(defaultValue as Int)

                    listeningForKeybind = false
                    buttonText.setText(newCurrentKey!!)
                    feature.field.set(SkyblockTweaks.config, defaultValue)
                }
            }
            if (feature.type == ConfigType.BUTTON) {
                val button = UIBlock(Color.DARK_GRAY).constrain {
                    width = 70.pixels
                    height = 18.pixels
                    y = CenterConstraint()
                    x = 10.pixels(alignOpposite = true)
                } childOf featureComponent effect OutlineEffect(CustomizationConfig.onSwitchColor.colorState.get(), 1f)

                button.onMouseEnterRunnable {
                    button.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, headerBackgroundColorState.get().colorState.constraint)
                    }
                }
                button.onMouseLeaveRunnable {
                    button.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, Color.DARK_GRAY.constraint)
                    }
                }

                val buttonText = UIText(feature.placeholder).constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                } childOf button

                button.onMouseClick {
                    (feature.value as Runnable).run()
                }
                ignoredHeights.add(button)
            }

            if (feature.isParent) {
                val unhovered = Color(200, 200, 200)
                val hovered = Color(255, 255, 255)

                val settingsGear = UIImage.ofResourceCached("/assets/skyblocktweaks/gui/gear.png").constrain {
                    x = SiblingConstraintFixed(5f, true)
                    y = CenterConstraint()
                    height = 16.pixels
                    width = 16.pixels
                    color = unhovered.constraint
                } childOf featureComponent

                settingsGear.onMouseEnterRunnable {
                    settingsGear.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, hovered.constraint)
                    }
                }
                settingsGear.onMouseLeaveRunnable {
                    settingsGear.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, unhovered.constraint)
                    }
                }
                var orignalHeight = 0.pixels

                Utils.setTimeout({
                    orignalHeight = feature.featureContainer.getHeight().pixels
                }, 500)

                clearPopup()

                settingsGear.onMouseClick {
                    feature.optionsHidden = !feature.optionsHidden

                    clearPopup()

                    val parentFeatureBackground = feature.featureContainer.children.getOrNull(0)
                    if (!feature.optionsHidden) {
                        parentFeatureBackground?.setHeight(orignalHeight)
                        parentFeatureBackground?.animate {
                            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, ChildBasedSizeConstraint(2f), 0f)
                        }

                        feature.optionElements.values.forEach {
                            it.unhide(true)
                        }
                    } else {
                        parentFeatureBackground?.setHeight(feature.featureContainer.getHeight().pixels)
                        parentFeatureBackground?.animate {
                            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, orignalHeight, 0f)
                        }
                    }
                }
                ignoredHeights.add(settingsGear)
            }
        } catch (e: Exception) {
            println("FEATURE ${feature.name} had a problem! value: ${feature.value}")
            e.printStackTrace()
        }

        // Stop the setting options from effecting total height
        featureComponent.setHeight(LPosChildSizeConstraint(ignoredHeights) + 5.pixels)
    }

    private fun clearPopup() {
        floatingColorPicker?.setFloating(false)
        floatingColorPicker?.hide(true)
    }

    private fun UIComponent.addTooltip(set: Set<String>) {
        tooltipElements[this] = set
    }

    private fun updateThemeColors() {
        val theme = CustomizationConfig.selectedTheme
        // TODO: Switch to classes
        if (theme == "Gray") {
            CustomizationConfig.sidebarBackgroundColor.set(0x1c1c1c)
            CustomizationConfig.selectedCategoryColor.set(0xffffff)

            CustomizationConfig.mainBackgroundColor.set(0x161616)
            CustomizationConfig.windowBorderColor.set(Color.GRAY)
            CustomizationConfig.hoveredCategoryColor.set(0xcdcccc)

            CustomizationConfig.headerBackgroundColor.set(0x222222)
            CustomizationConfig.guiLineColors.set(0x828282)

            CustomizationConfig.featureBorderColor.set(0x808080)
            CustomizationConfig.featureBackgroundColor.set(0x222222)

            CustomizationConfig.onSwitchColor.set(0x00ff00)
            CustomizationConfig.defaultCategoryColor.set(0xb4b4b4)
        }
        if (theme.startsWith("Dark + ")) {
            CustomizationConfig.sidebarBackgroundColor.set(0x0f0f0f)
            CustomizationConfig.mainBackgroundColor.set(0x090909)
            CustomizationConfig.windowBorderColor.set(Color.GRAY)
            CustomizationConfig.headerBackgroundColor.set(0x0f0f0f)
            CustomizationConfig.featureBackgroundColor.set(0x1c1c1c)
            CustomizationConfig.defaultCategoryColor.set(0xb2b2b2)
        }
        if (theme == "Dark + Cyan") {
            CustomizationConfig.selectedCategoryColor.set(0x00ffff)
            CustomizationConfig.windowBorderColor.set(0x00ffff)
            CustomizationConfig.hoveredCategoryColor.set(0xcdf3f4)

            CustomizationConfig.guiLineColors.set(0x2ba7b8)
            CustomizationConfig.featureBorderColor.set(0x2ba7b8)
            CustomizationConfig.onSwitchColor.set(0x00ff96)
            CustomizationConfig.defaultCategoryColor.set(0x5f5f5f)
        }
        if (theme == "Dark + Pink") {
            CustomizationConfig.selectedCategoryColor.set(0xf700ff)
            CustomizationConfig.windowBorderColor.set(0xff00fb)
            CustomizationConfig.hoveredCategoryColor.set(0x9539cb)

            CustomizationConfig.guiLineColors.set(0x5d0065)
            CustomizationConfig.featureBorderColor.set(0x5d0065)
            CustomizationConfig.onSwitchColor.set(0xdb35b8)
            CustomizationConfig.defaultCategoryColor.set(0x5f5f5f)
        }
        if (theme == "Dark + Orange") {
            CustomizationConfig.selectedCategoryColor.set(0xffb300)
            CustomizationConfig.windowBorderColor.set(0xff7500)
            CustomizationConfig.hoveredCategoryColor.set(0xd5a98b)

            CustomizationConfig.guiLineColors.set(0xb83c2b)
            CustomizationConfig.featureBorderColor.set(0xe1651e)
            CustomizationConfig.onSwitchColor.set(0xff4400)
        }
    }
}