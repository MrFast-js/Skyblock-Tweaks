package mrfast.sbt.config

import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.*
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.OutlineEffect
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.BasicState
import gg.essential.universal.UMatrixStack
import net.minecraft.client.Minecraft
import net.minecraftforge.fml.client.config.GuiUtils
import java.awt.Color
import java.util.*


class ConfigGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    var MainBackgroundColor = BasicState(Color(50, 50, 50))
    var CategoriesBackgroundColor = BasicState(Color(62, 62, 62))
    var HeaderBackgroundColor = BasicState(Color(75, 75, 75))
    var GuiLineColors = BasicState(Color(130, 130, 130))
    var MainBorderColor = BasicState(Color(0, 255, 255))
    var SelectedCategoryColor = BasicState(Color(0, 255, 255))
    var updateSymbol = BasicState(UIText())

    var showUpdateButton = true
    var selectedCategory = "General"
    var selectedCategoryComponent: UIComponent? = null

    var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        for (element in tooltipElements.keys) {
            if (element.isHovered()) {
                GuiUtils.drawHoveringText(tooltipElements[element]?.toMutableList()
                        ?: mutableListOf(), mouseX, mouseY, window.getWidth().toInt(), window.getHeight().toInt(), -1, Minecraft.getMinecraft().fontRendererObj)
            }
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()

        updateBlinkyTimer.cancel()
    }


    init {
        // Create a background panel
        val background = UIBlock(MainBackgroundColor).constrain {
            width = 1.pixels
            height = 4.pixels
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf window effect OutlineEffect(MainBorderColor.get(), 2f, drawInsideChildren = true, drawAfterChildren = true) effect ScissorEffect()

        background.animate {
            setWidthAnimation(Animations.IN_OUT_EXP, 0.25f, MinConstraint(70.percent, 600.pixels))
            setHeightAnimation(Animations.IN_OUT_EXP, 0.35f, MinConstraint(70.percent, 400.pixels),0.2f)
        }
        // Use 70% width, max 600px

        val header = UIBlock(HeaderBackgroundColor).constrain {
            width = 100.percent
            height = 30.pixels
            x = 0.pixels()
            y = 0.pixels()
        } childOf background effect OutlineEffect(GuiLineColors.get(), 2f, sides = setOf(OutlineEffect.Side.Bottom))

        // Add some text to the panel
        val modTitle = UIText("§rSkyblock §9Tweaks").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf header
        modTitle.setTextScale(2.5.pixels)

        val modVersion = UIText("§7v1.0.0").constrain {
            x = SiblingConstraint(4f)
            y = SiblingConstraint(4f) - 10f.pixels()
        } childOf header

        modVersion.setTextScale(0.75.pixels)

        val searchBar = UIBlock(MainBackgroundColor).constrain {
            width = MinConstraint(12.percent, 100.pixels)
            height = 12.pixels
            x = PixelConstraint(10f, true)
            y = CenterConstraint()
        } childOf header effect OutlineEffect(GuiLineColors.get(), 1f)

        val searchBarInput = UITextInput("Search").constrain {
            width = 100.percent
            height = 8.pixels
            x = CenterConstraint() + 1.pixels
            y = CenterConstraint()
        } childOf searchBar
        searchBarInput.setColor(Color.GRAY)

        searchBarInput.onMouseClick {
            searchBarInput.grabWindowFocus()
        }

        if (showUpdateButton) {
            val updateButton = UIBlock(MainBackgroundColor).constrain {
                width = 16.pixels
                height = 16.pixels
                x = SiblingConstraint(15f, true)
                y = CenterConstraint()
            } childOf header effect OutlineEffect(GuiLineColors.get(), 1f)

            val updateSymbolNew = UIText("⬆").constrain {
                x = CenterConstraint()
                y = CenterConstraint()
            } childOf updateButton
            updateSymbolNew.setTextScale(2.pixels)

            animateUpdateButton()
            updateSymbol.set(updateSymbolNew)

            updateButton.onMouseClick {
                // do update command
            }
            updateButton.addTooltip(setOf("§aUpdate §ev1.2.0§a is available! Click to download"))
        }

        val categoryListBackground = UIBlock(CategoriesBackgroundColor).constrain {
            x = 0.pixels
            y = 32.pixels
            width = 21.percent
            height = 100.percent - 32.pixels
        } childOf background effect OutlineEffect(GuiLineColors.get(), 1f, sides = setOf(OutlineEffect.Side.Right))

        val categoryList = ScrollComponent("", 10f).constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 100.percent
        } childOf categoryListBackground

        val scrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = 0.pixels
            color = Color(200, 200, 200, 200).constraint
        } childOf categoryListBackground

        categoryList.setVerticalScrollBarComponent(scrollbar, true)

        val featureList = ScrollComponent("", 10f).constrain {
            x = 21.percent + 2.pixels
            y = 32.pixels
            width = 79.percent - 2.pixels
            height = 100.percent
        } childOf background

        val featureScrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = 0.pixels
            color = Color(200, 200, 200, 200).constraint
        } childOf categoryListBackground

        featureList.setVerticalScrollBarComponent(featureScrollbar, true)

        for ((count, category) in ConfigManager.categories.values.withIndex()) {
            val actualY = if (count == 0) 10.pixels else SiblingConstraint(3f)

            val categoryComponent = UIText(category.name).constrain {
                x = CenterConstraint()
                y = actualY
                height = 8.pixels
            } childOf categoryList
            if (selectedCategory.equals(category.name)) {
                updateSelectedCategoryColor(categoryComponent, category.name)
                updateSelectedFeatures(featureList)
            }

            categoryComponent.setTextScale(1.6.pixels)
            categoryComponent.onMouseClick {
                updateSelectedCategoryColor(categoryComponent, category.name)
                updateSelectedFeatures(featureList)
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
                // blinky
                updateSymbol.get().animate {
                    setColorAnimation(Animations.OUT_EXP, 0.5f, colors[currentColorIndex].constraint)
                }

                // Toggle to the next color index
                currentColorIndex = (currentColorIndex + 1) % colors.size
            }
        }, 0, 500)
    }


    private fun updateSelectedCategoryColor(new: UIComponent, name: String) {
        selectedCategoryComponent?.setColor(Color.WHITE)
        new.setColor(SelectedCategoryColor.get())
        selectedCategoryComponent = new
        selectedCategory = name
    }

    private fun updateSelectedFeatures(list: ScrollComponent) {
        list.clearChildren()

        val category = ConfigManager.categories[selectedCategory] ?: return
        for ((drawnCategories, subcategory) in category.subcategories.values.withIndex()) {
            val actualY = if (drawnCategories == 0) 5.pixels else SiblingConstraint(8f)
            val subcategoryComponent = UIContainer().constrain {
                height = ChildBasedSizeConstraint()
                width = 100.percent
                x = 0.pixels
                y = actualY
            } childOf list

            val subcategoryName = UIText(subcategory.name).constrain {
                x = CenterConstraint()
                y = 0.pixels
                textScale = 1.8.pixels
            } childOf subcategoryComponent

            for (feature in subcategory.features.values) {
                val featureBackground = UIBlock(HeaderBackgroundColor).constrain {
                    x = CenterConstraint()
                    y = SiblingConstraint(4f)
                    width = 90.percent
                    height = ChildBasedSizeConstraint(2f)
                } childOf subcategoryComponent effect OutlineEffect(GuiLineColors.get(), 1f)

                val featureTitle = UIText(feature.name).constrain {
                    x = 2.pixels
                    y = 2.pixels
                    textScale = 1.5.pixels
                } childOf featureBackground

                val featureDescription = UIWrappedText(feature.description).constrain {
                    x = 2.pixels
                    y = SiblingConstraint(2f)
                    width = 80.percent - 2.pixels
                    color = Color.GRAY.constraint
                } childOf featureBackground
            }
        }

    }

    private fun UIComponent.addTooltip(set: Set<String>) {
        tooltipElements[this] = set
    }
}

