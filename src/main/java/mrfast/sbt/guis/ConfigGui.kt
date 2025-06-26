package mrfast.sbt.guis

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.PixelConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import gg.essential.elementa.state.constraint
import gg.essential.universal.UMatrixStack
import gg.essential.vigilance.gui.settings.SelectorComponent
import kotlinx.coroutines.*
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.config.categories.DeveloperConfig.showInspector
import mrfast.sbt.guis.components.*
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.ConfigType
import mrfast.sbt.managers.GuiManager
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
import java.awt.Desktop
import java.net.URI
import java.util.*


class ConfigGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2, drawDefaultBackground = false) {
    companion object {
        var listeningForKeybind = false
        var searchQuery = ""

        fun openConfigSearch(query: String) {
            searchQuery = query
            GuiManager.displayScreen(ConfigGui())
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

    private var updateSymbol = BasicState(CustomUIText())
    private var showUpdateButton = VersionManager.neededUpdate.versionName.isNotEmpty()
    private var selectedCategory = "General"
    private var selectedCategoryComponent: UIComponent? = null
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var openingAnimation = false

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (CustomizationConfig.backgroundBlur) drawBackgroundBlur()
        else {
            resetBlurAnimation()
        }
        drawDefaultBackground()

        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        // Dont draw tooltips on opening animation
        if (!openingAnimation) return

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
        openingAnimation = false
        Utils.setTimeout({
            openingAnimation = true
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
        // Backwards compatability for old color system, update to new default theme
        if(CustomizationConfig.onSwitchColor == CustomColor(Color.GREEN) && CustomizationConfig.selectedTheme == "Dark + Cyan") {
            updateThemeColors()
        }

        // Create a background panel
        val background =
            OutlinedRoundedRectangle(mainBorderColorState.get().colorState.constraint, 2f, mainBorderRadius).constrain {
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

            val socketStatusButton =
                OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
                    color = mainBackgroundColorState.get().colorState.constraint
                    width = 20.pixels
                    height = 20.pixels
                    x = 8.pixels
                    y = CenterConstraint()
                } childOf header

            val socketStatusNew = CustomUIText("∞", false, scale = 1.6f).constrain {
                x = CenterConstraint() + 1.pixels
                y = CenterConstraint()
                color = statusColor.constraint
            } childOf socketStatusButton


            val lore = mutableSetOf(
                if (SocketUtils.socketConnected) "§a∞ Connected to SBT Socket!" else "§c✕ Disconnected from SBT Socket!"
            )

            if (!SocketUtils.socketConnected) {
                socketStatusButton.onMouseClick {
                    SocketUtils.setupSocket()
                    ChatUtils.sendClientMessage("§eRe-attempting socket connection...", prefix = true)
                    mrfast.sbt.utils.GuiUtils.closeGui()
                }
                lore.add("§eClick to re-attempt connection")
            }

            socketStatusButton.addTooltip(lore)
        }

        val editGuiLocationsButton =
            OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
                width = 20.pixels
                height = 20.pixels
                x = if(!CustomizationConfig.developerMode) 8.pixels else SiblingConstraintFixed(8f, false)
                y = CenterConstraint()
                color = mainBackgroundColorState.get().colorState.constraint
            } childOf header
        val editGuiLocationsSymbol = CustomUIText("✎", scale = 2f).constrain {
            x = CenterConstraint();y = CenterConstraint();color = Color(0xFFFF55).constraint
        } childOf editGuiLocationsButton
        editGuiLocationsButton.onMouseClick { GuiManager.displayScreen(GuiEditor()) }
        editGuiLocationsButton.addTooltip(setOf("§eEdit Gui Locations"))

        // Add some text to the panel
        val modTitle = CustomUIText("§eSkyblock §9Tweaks", false, scale = 2.5f).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf header

        val modVersion = CustomUIText("§7${SkyblockTweaks.MOD_VERSION}", scale = 0.75f).constrain {
            x = SiblingConstraintFixed(1f)
            y = SiblingConstraintFixed(4f) - 10f.pixels()
        } childOf header

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
            val updateButton =
                OutlinedRoundedRectangle(guiLineColorsState.get().colorState.constraint, 1f, 3f).constrain {
                    color = mainBackgroundColorState.get().colorState.constraint
                    width = 16.pixels
                    height = 16.pixels
                    x = SiblingConstraintFixed(15f, true)
                    y = CenterConstraint()
                } childOf header

            val updateSymbolNew = CustomUIText("⬆", scale = 2f).constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf updateButton

            animateUpdateButton()
            updateSymbol.set(updateSymbolNew)

            updateButton.onMouseClick {
                // do update command
                VersionManager.checkIfNeedUpdate()
                mrfast.sbt.utils.GuiUtils.closeGui()
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

        val discordButton =
            OutlinedRoundedRectangle(Color(0x3E477F).constraint, 1f, 3f).constrain {
                width = 90.percent
                height = 30.pixels
                x = CenterConstraint()
                y = 15.pixels(true)
                color = Color(0x5D6AC0).constraint
            } childOf categoryList

        val discordButtonSymbol = UIImage.ofResource("/assets/skyblocktweaks/gui/discord.png").constrain {
            width = (132 / 1.6).pixels
            height = (25 / 1.6).pixels
            x = CenterConstraint();y = CenterConstraint()
        } childOf discordButton

        discordButton.onMouseClick { Desktop.getDesktop().browse(URI("https://discord.gg/XKaB6t2ejh")) }
        discordButton.addTooltip(setOf("§3Click to join our Discord!"))

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

        featureList.onMouseScroll {
            clearPopup()
        }

        val featureScrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = PixelConstraint(0f, alignOpposite = true)
            color = Color(200, 200, 200, 200).constraint
        } childOf featureListBackground

        featureList.setVerticalScrollBarComponent(featureScrollbar, true)

        searchBarInput.onKeyType { _, keycode ->
            if (keycode == Keyboard.KEY_ESCAPE) {
                mrfast.sbt.utils.GuiUtils.closeGui()
                return@onKeyType
            }
            searchQuery = searchBarInput.getText()
            updateSelectedFeatures(featureList)
        }

        for ((count, category) in ConfigManager.categories.values.withIndex()) {
            val actualY = if (count == 0) 10.pixels else SiblingConstraintFixed(3f)

            // Stop developer tab from showing if not in developer mode
            if (category.name == "§eDeveloper" && !CustomizationConfig.developerMode) continue
            if (category.name == "§3Hidden" && !DeveloperConfig.showHiddenConfig) continue

            val categoryComponent = CustomUIText(category.name, scale = 1.6f).constrain {
                x = CenterConstraint()
                y = actualY
                height = 8.pixels
                color = defaultCategoryColorState.get().colorState.constraint
            } childOf categoryList

            if (selectedCategory == category.name) {
                updateSelectedCategoryColor(categoryComponent, category.name)
                updateSelectedFeatures(featureList)
            }

            categoryComponent.onMouseEnter {
                // Don't do hover colors if already colored
                if (selectedCategory != category.name) {
                    if (!categoryComponent.getColor().equals(hoveredCategoryColorState.get().colorState.constraint)) {
                        categoryComponent.animate {
                            setColorAnimation(
                                Animations.OUT_EXP,
                                0.2f,
                                hoveredCategoryColorState.get().colorState.constraint
                            )
                        }
                    }
                }
            }
            categoryComponent.onMouseLeave {
                // Don't do hover colors if already colored
                if (selectedCategory != category.name) {
                    if (!categoryComponent.getColor().equals(defaultCategoryColorState.get().colorState.constraint)) {
                        categoryComponent.animate {
                            setColorAnimation(
                                Animations.OUT_EXP,
                                0.2f,
                                defaultCategoryColorState.get().colorState.constraint
                            )
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

        if (searchQuery.isNotEmpty()) {
            // Preload all features so they are available for search
            updateSelectedFeatures(featureList, true)

            Utils.setTimeout({
                // Run again in order to actually filter the results this time
                searchBarInput.setText(searchQuery)
                updateSelectedFeatures(featureList)
            }, 200)
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

    private fun updateSelectedFeatures(list: ScrollComponent, showAll: Boolean = false) {
        list.clearChildren()

        var drawnCategories = 0
        // Loop through all categories
        for (category in ConfigManager.categories) {
            // If searching something, ignore selected category
            if (category.key != selectedCategory && searchQuery.isEmpty()) continue

            // Loop through all subcategories
            for (subcategory in category.value.subcategories.values) {
                val actualY = if (drawnCategories == 0) 12.pixels else SiblingConstraintFixed(6f)
                var drawnFeatures = 0

                val subcategoryComponent = UIContainer().constrain {
                    height = ChildBasedSizeConstraint() + 20.pixels
                    width = 100.percent
                    x = 0.pixels
                    y = actualY
                }

                val subcategoryTitle = CustomUIText(subcategory.name, scale = 1.8f).constrain {
                    x = CenterConstraint()
                    y = 0.pixels
                } childOf subcategoryComponent

                val comparator = compareByDescending<String> { it }
                val sortedFeatures = subcategory.features.toSortedMap(comparator)

                // Loop through all features in subcategory
                for (feature in sortedFeatures.values) {
                    // Filter out feature options
                    if (feature.parentName.isNotEmpty()) continue

                    if (!showAll && !shouldFeatureAppear(feature)) continue

                    // Create the feature element, containing the title, description and config type
                    createFeatureElement(feature, subcategoryComponent)

                    drawnFeatures++
                }
                // Don't draw subcategory title if no features
                if (drawnFeatures != 0) {
                    subcategoryComponent childOf list
                }
                drawnCategories++
            }
        }

        // After all features have been loaded, go back through and populate the parent features with their options

        // Loop through all categories
        for (category in ConfigManager.categories) {
            // Loop through all subcategories
            for (subcategory in category.value.subcategories.values) {
                // Loop through all features in subcategory
                for (feature in subcategory.features.values) {
                    if (!showAll && feature.parentName.isEmpty()) continue

                    // Ensure that the features option has a real parent
                    if (feature.parentFeature == null) {
                        if (feature.parentName.isNotEmpty()) {
                            val parentFeature = subcategory.features.values.find {
                                it.name == feature.parentName
                            }

                            feature.parentFeature = parentFeature
                        }
                    }

                    if (!shouldFeatureAppear(feature)) continue

                    val featureOption = createFeatureOptionElement(feature)

                    if (featureOption != null) {
                        // Add feature to parent's reference so it can be hidden / unhidden in future
                        feature.parentFeature!!.optionElements[feature.name] = featureOption

                        // Hide sub options so original height is not affected
                        featureOption.featureAndOptionsComponent.hide(true)
                    }
                }
            }
        }
    }

    private fun shouldFeatureAppear(feature: ConfigManager.Feature): Boolean {
        // If feature directly contains the search query, show it
        val mainFeatureShow =
            feature.name.contains(searchQuery, ignoreCase = true) || feature.subcategory.name.contains(
                searchQuery,
                ignoreCase = true
            ) || feature.description.contains(
                searchQuery,
                ignoreCase = true
            )

        if (mainFeatureShow) {
            return true
        }

        // if feature is a suboption, check if parent feature contains search query
        if (feature.parentFeature?.name?.contains(
                searchQuery,
                ignoreCase = true
            ) == true || feature.parentFeature?.description?.contains(searchQuery, ignoreCase = true) == true
        ) {
            return true
        }

        // If feature is a parent feature, check if any of its suboptions contain the search query
        for (optionElement in feature.optionElements) {
            if (shouldFeatureAppear(optionElement.value)) {
                return true
            }
        }

        return false
    }

    // Used to create main feature, which contains the title, description and config type
    private fun createFeatureElement(
        feature: ConfigManager.Feature,
        subcategoryComponent: UIComponent
    ): ConfigManager.Feature {
        // This is the main ui element, everything else is a child of this, and when options are present, they are cut off by this element when not expanded
        val featureAndOptionsContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraintFixed(6f)
            width = 99.percent
            height = ChildBasedSizeConstraint(2f)
        } childOf subcategoryComponent effect ScissorEffect()

        // Create the background for the feature and options container
        val featureAndOptionsBackground =
            OutlinedRoundedRectangle(featureBorderColorState.get().colorState.constraint, 1f, 6f).constrain {
                x = CenterConstraint()
                y = SiblingConstraintFixed(6f)
                width = if (feature.parentName.isEmpty()) 100.percent else 90.percent
                height = ChildBasedSizeConstraint(2f)
                color = featureBackgroundColorState.get().colorState.constraint
            } childOf featureAndOptionsContainer

        // The feature container, which contains the title, description
        val featureContainer = UIContainer().constrain {
            x = PixelConstraint(0f)
            y = SiblingConstraintFixed(6f)
            width = 100.percent
            height = ChildBasedSizeConstraint(2f)
        } childOf featureAndOptionsBackground

        val featureTitle = CustomUIText(feature.name, scale = 1.5f).constrain {
            x = 3.pixels
            y = 3.pixels
        } childOf featureContainer

        if (feature.description != "") {
            val featureDescription = CustomUIWrappedText(feature.description).constrain {
                x = 3.pixels
                y = SiblingConstraintFixed(2f)
                width = 80.percent - 2.pixels
                color = Color.GRAY.constraint
            } childOf featureContainer
        }

        // Populate the feature with its config type, ie toggle, text input, etc.
        populateFeature(feature, featureContainer)

        feature.featureAndOptionsComponent = featureAndOptionsContainer

        return feature
    }

    // Create a feature option element, which is a child of the parent feature, and contains the title, description and config type
    private fun createFeatureOptionElement(
        feature: ConfigManager.Feature
    ): ConfigManager.Feature? {
        // Check if name, description or subcategory contain the search
        if (feature.parentFeature == null) {
            return null
        }

        val parentComponent = feature.parentFeature!!.featureAndOptionsComponent

        val parentFeatureBackground = parentComponent.children.getOrNull(0) ?: return null

        val featureContainer = UIContainer().constrain {
            x = CenterConstraint()
            y = SiblingConstraintFixed(6f)
            width = 100.percent
            height = ChildBasedSizeConstraint(2f)
            color = Color.RED.constraint
        } childOf parentFeatureBackground

        val optionName = CustomUIText(feature.name, scale = 1.5f).constrain {
            x = 2.pixels
            y = 2.pixels
        } childOf featureContainer

        if (feature.description.isNotEmpty()) {
            val optionDescription = CustomUIWrappedText(feature.description).constrain {
                x = 2.pixels
                y = SiblingConstraintFixed(2f)
                width = 80.percent - 2.pixels
                color = Color.GRAY.constraint
            } childOf featureContainer
        } else {
            optionName.setY(CenterConstraint())
        }

        populateFeature(feature, featureContainer)

        feature.featureAndOptionsComponent = featureContainer

        return feature
    }

    private var floatingColorPicker: ColorPickerComponent? = null
    private var keyListerJob: Job? = null
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

                    // Reopen the config gui if developer mode is toggled
                    if(feature.name=="§cDeveloper Mode") {
                        mrfast.sbt.utils.GuiUtils.closeGui()
                        GuiManager.displayScreen(ConfigGui())
                    }
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

                if(customColor.chroma) {
                    colorDisplay.setColor(mrfast.sbt.utils.GuiUtils.rainbowColor.constraint)
                }

                val colorPicker = ColorPickerComponent(customColor, colorDisplay).constrain {
                    x = 10.pixels(true)
                } childOf featureComponent

                colorPicker.hide(true)

                colorDisplay.onMouseClick {
                    if(floatingColorPicker != null) {
                        clearPopup()
                    } else {
                        colorPicker.setY(SiblingConstraintFixed(5f))

                        colorPicker.setFloating(true)
                        colorPicker.unhide(false)

                        floatingColorPicker = colorPicker
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
                    if (defaultValue is Color) {
                        colorDisplay.setColor(defaultValue)
                        customColor.chroma = false
                        colorPicker.chroma = false
                        colorPicker.customColor.chroma = false
                        colorPicker.setPickerColor(defaultValue)
                        customColor.colorState.set(defaultValue)
                    }
                    if (defaultValue is CustomColor) {
                        customColor.colorState.set(defaultValue.initialColor)
                        customColor.chroma = false
                        colorPicker.chroma = false
                        colorPicker.customColor.chroma = false
                        colorDisplay.setColor(defaultValue.initialColor)
                        colorPicker.setPickerColor(defaultValue.initialColor)
                    }
                }

                colorPicker.onValueChange { value: Any? ->
                    feature.field.set(SkyblockTweaks.config, value)
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
                        setColorAnimation(
                            Animations.OUT_EXP,
                            0.5f,
                            featureBackgroundColorState.get().colorState.constraint
                        )
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
                val buttonText = CustomUIText(currentKey).constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                } childOf button

                button.onMouseClickConsumer {
                    if (listeningForKeybind) return@onMouseClickConsumer
                    listeningForKeybind = true
                    // Set listening style, similar to minecrafts keybind system
                    buttonText.setText("§r> §e" + buttonText.getText() + "§r <")
                    keyListerJob?.cancel()
                    keyListerJob = CoroutineScope(Dispatchers.Default).launch {
                        listeningForKeybind = true

                        while (listeningForKeybind) {
                            for (i in 0 until Keyboard.KEYBOARD_SIZE) {
                                if (Keyboard.isKeyDown(i)) {
                                    val newKeyName = Keyboard.getKeyName(i)

                                    if (i == Keyboard.KEY_ESCAPE) {
                                        buttonText.setText("NONE")
                                        delay(200)
                                        listeningForKeybind = false
                                        break
                                    }

                                    buttonText.setText(newKeyName)
                                    feature.field.set(SkyblockTweaks.config, i)

                                    listeningForKeybind = false
                                    break
                                }
                            }
                        }
                    }
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
                        setColorAnimation(
                            Animations.OUT_EXP,
                            0.5f,
                            headerBackgroundColorState.get().colorState.constraint
                        )
                    }
                }
                button.onMouseLeaveRunnable {
                    button.animate {
                        setColorAnimation(Animations.OUT_EXP, 0.5f, Color.DARK_GRAY.constraint)
                    }
                }

                val buttonText = CustomUIText(feature.placeholder).constrain {
                    x = CenterConstraint()
                    y = CenterConstraint()
                } childOf button

                button.onMouseClick {
                    (feature.value as Runnable).run()
                }

                ignoredHeights.add(button)
            }

            // Give parents a settings gear if they have options, allowing for expanding and collapsing of options
            if (feature.isParent) {
                val unhovered = Color(200, 200, 200)
                val hovered = Color(255, 255, 255)

                val settingsGear = RotatingUIImage.ofResourceCached("/assets/skyblocktweaks/gui/gear.png").constrain {
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

                // Calculate the height of the feature, waits 500ms because the components must be rendered first
                var smallSizeHeight = 0.pixels

                Utils.setTimeout({
                    smallSizeHeight = feature.featureAndOptionsComponent.getHeight().pixels
                }, 500)

                // Hides the color picker if it is open
                clearPopup()

                // On click, toggle the options hidden state
                settingsGear.onMouseClick {
                    feature.optionsHidden = !feature.optionsHidden

                    // Rotate the settings gear
                    settingsGear.setTargetAngle(if (feature.optionsHidden) 0f else 90f)

                    // Hides the color picker if it is open
                    clearPopup()

                    val featureBackground = feature.featureAndOptionsComponent.children.getOrNull(0)

                    if (!feature.optionsHidden) {
                        // If the feature is being expanded, set the height to the small size height, then animate to the full height
                        featureBackground?.setHeight(smallSizeHeight)
                        featureBackground?.animate {
                            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, ChildBasedSizeConstraint(2f), 0f)
                        }

                        // Unhide all the options in the feature
                        feature.optionElements.values.forEach {
                            it.featureAndOptionsComponent.unhide(true)
                        }
                    } else {
                        // If the feature is being collapsed, set the height to the full height, then animate to the small size height
                        featureBackground?.setHeight(feature.featureAndOptionsComponent.getHeight().pixels)
                        featureBackground?.animate {
                            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, smallSizeHeight, 0f)
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
        floatingColorPicker = null
    }

    private fun UIComponent.addTooltip(set: Set<String>) {
        tooltipElements[this] = set
    }

    private fun updateThemeColors() {
        val theme = CustomizationConfig.selectedTheme
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