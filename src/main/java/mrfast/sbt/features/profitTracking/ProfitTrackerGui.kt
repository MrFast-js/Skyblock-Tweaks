package mrfast.sbt.features.profitTracking

import gg.essential.api.utils.GuiUtil
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UKeyboard
import gg.essential.universal.UMatrixStack
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.features.profitTracking.ProfitTracker.blacklistItems
import mrfast.sbt.features.profitTracking.ProfitTracker.paused
import mrfast.sbt.features.profitTracking.ProfitTracker.pausedDuration
import mrfast.sbt.features.profitTracking.ProfitTracker.purseGainLoss
import mrfast.sbt.features.profitTracking.ProfitTracker.selectedFilterMode
import mrfast.sbt.features.profitTracking.ProfitTracker.sessionStartedAt
import mrfast.sbt.features.profitTracking.ProfitTracker.started
import mrfast.sbt.features.profitTracking.ProfitTracker.whitelistItems
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.Button
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.toFormattedTime
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.GuiTextField
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import kotlin.math.floor
import kotlin.math.min


class ProfitTrackerGui : WindowScreen(ElementaVersion.V2) {
    private val fontRenderer = Utils.mc.fontRendererObj
    private var guiTop = 0F
    private var guiLeft = 0F
    private val boxTexture = ResourceLocation("skyblocktweaks", "gui/box.png")
    private val startStopButton = Button(40F, 12F, 142, 66)
    private val pauseButton = Button(40F, 12F, 185, 66)

    private var mouseX = 0.0
    private var mouseY = 0.0

    private var itemPickerPopupX = 0.0
    private var itemPickerPopupY = 0.0
    private var searchField: GuiTextField? = null

    init {
        Keyboard.enableRepeatEvents(true)
        searchField = GuiTextField(
            0,
            fontRenderer,
            0,
            0,
            75,
            14
        )
        setupTextInput()
    }

    private fun setupTextInput() {
        val sf = searchField ?: return
        sf.maxStringLength = 15
        sf.enableBackgroundDrawing = true
        sf.setTextColor(0xFFFFFF)
    }

    private val newItemButtons = mutableMapOf<String, Button>()

    override fun onKeyPressed(keyCode: Int, typedChar: Char, modifiers: UKeyboard.Modifiers?) {
        super.onKeyPressed(keyCode, typedChar, modifiers)

        startingItemPopupOffset = 0
        this.searchField?.textboxKeyTyped(typedChar, keyCode)
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        super.onMouseClicked(mouseX, mouseY, mouseButton)

        if (startStopButton.isClicked(mouseX, mouseY, guiLeft, guiTop)) {
            started = !started

            // Clicking Started runs this
            if (started) {
                sessionStartedAt = System.currentTimeMillis()
                ProfitTracker.itemsGainedDuringSession.clear()
                purseGainLoss = 0
            }
            pausedDuration = 0
            paused = false
        }
        if (pauseButton.isClicked(mouseX, mouseY, guiLeft, guiTop)) {
            paused = !paused
        }

        val itemButtonsCopy = newItemButtons.toMap()

        for (value in itemButtonsCopy.values) {
            value.isClicked(mouseX, mouseY, guiLeft, guiTop)
        }
    }

    private var lastMouseDown = false
    private var mouseClicking = false
    private var mouseDown = false

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        val res = ScaledResolution(Utils.mc)

        this.mouseX = mouseX.toDouble()
        this.mouseY = mouseY.toDouble()

        val mainBoxWidth = 256F
        val mainBoxHeight = 213F

        mouseDown = Mouse.isButtonDown(0)
        mouseClicking = lastMouseDown && !mouseDown
        lastMouseDown = mouseDown

        // Draw Title
        GlStateManager.pushMatrix()
        val titleFontScale = 2.1
        GlStateManager.scale(titleFontScale, titleFontScale, 1.0)
        GlStateManager.translate(0f, 0f, 10f)
        fontRenderer.drawString(
            "§aProfit Tracker",
            ((60f + guiLeft) / titleFontScale).toFloat(), ((10f + guiTop) / titleFontScale).toFloat(), 0xFFFFFF, true
        )
        GlStateManager.popMatrix()

        guiLeft = (res.scaledWidth - mainBoxWidth) / 2
        guiTop = (res.scaledHeight - mainBoxHeight) / 2

        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);
        GlStateManager.color(1f, 1f, 1f)
        GuiUtils.drawTexture(
            guiLeft,
            guiTop,
            mainBoxWidth,
            mainBoxHeight,
            0F,
            mainBoxWidth / 400F,
            0F,
            mainBoxHeight / 213F,
            GL11.GL_NEAREST
        )
        // Whitelist/Blacklist button
        GuiUtils.drawTexture(
            guiLeft + 27,
            guiTop + 31,
            81f,
            12f,
            257 / 400f,
            (257 + 81f) / 400F,
            126 / 213f,
            (126 + 12f) / 213F,
            GL11.GL_NEAREST
        )
        if (mouseClicking) {
            if (mouseX > guiLeft + 27 && mouseX < guiLeft + 27 + 81f && mouseY > guiTop + 31 && mouseY < guiTop + 31 + 12) {
                // Toggle whitelist/blacklist
                selectedFilterMode = if (selectedFilterMode == "Whitelist") {
                    "Blacklist"
                } else {
                    "Whitelist"
                }
                newItemButtons.clear()
            }
        }
        GlStateManager.popMatrix()

        val buttonText = if (selectedFilterMode == "Blacklist") "§cBlacklist" else "§eWhitelist"
        fontRenderer.drawString(buttonText, guiLeft + 29 + 18, guiTop + 33, 0xFFFFFF, true)

        // Start stop buttons
        drawTimerControls()
        drawFilteredItems()
        drawCollectedItems(fontRenderer)
        drawItemPickerPopup()

        val moneyAreaX = guiLeft + 145 - 4
        val moneyAreaY = guiTop + 92 - 5

        if (mouseX > moneyAreaX && mouseX < moneyAreaX + 105 && mouseY > moneyAreaY && mouseY < moneyAreaY + 16 && started) {
            // Calculate session time
            val currentTime = System.currentTimeMillis()
            val sessionTime = currentTime - sessionStartedAt - pausedDuration
            val coinsPerMs = totalWorth.toDouble() / sessionTime.toDouble()
            val coinsPerSec = coinsPerMs * 1000F
            val coinsPerMin = coinsPerSec * 60F
            val coinsPerHr = coinsPerMin * 60F
            val formatted = "§6$${coinsPerHr.abbreviateNumber()} / Hour"

            net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                listOf("§eYour currently earning", formatted),
                mouseX,
                mouseY,
                Utils.mc.displayWidth,
                Utils.mc.displayHeight,
                -1,
                Utils.mc.fontRendererObj
            )
        }
    }

    private fun drawTimerControls() {
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);

        GlStateManager.color(1f, 1f, 1f)
        GuiUtils.drawTexture(
            guiLeft + startStopButton.x,
            guiTop + startStopButton.y,
            startStopButton.width,
            startStopButton.height,
            257 / 400f,
            (257 + 40f) / 400F,
            139 / 213f,
            (139 + 12f) / 213F,
            GL11.GL_NEAREST
        )
        GuiUtils.drawTexture(
            guiLeft + pauseButton.x,
            guiTop + pauseButton.y,
            pauseButton.width,
            pauseButton.height,
            257 / 400f,
            (257 + 40f) / 400F,
            139 / 213f,
            (139 + 12f) / 213F,
            GL11.GL_NEAREST
        )

        GlStateManager.popMatrix()
        val buttonText = if (started) "§cReset" else "§aStart"
        fontRenderer.drawString(buttonText, guiLeft + 142 + 7, guiTop + 66 + 2, 0xFFFFFF, true)

        fontRenderer.drawString("Pause", guiLeft + 185 + 5, guiTop + 66 + 2, 0xFFFFFF, true)


        // Calculate session time
        val currentTime = if (started) {
            System.currentTimeMillis()
        } else {
            sessionStartedAt
        }
        val sessionTime = currentTime - sessionStartedAt - pausedDuration

        // Draw timer
        fontRenderer.drawString(
            "§bSession: ${sessionTime.toFormattedTime()}",
            guiLeft + 145,
            guiTop + 51,
            0xFFFFFF,
            true
        )

        fontRenderer.drawString("§6$${totalWorth.formatNumber()}", guiLeft + 145, guiTop + 92, 0xFFFFFF, true)
    }

    private var totalWorth = 0L
    private fun drawCollectedItems(fontRenderer: FontRenderer) {
        val items = ProfitTracker.itemsGainedDuringSession
        val totalItemWorth = mutableMapOf<String, Pair<Long, Int>>()

        // Populate totalItemWorth map
        for (entry in items) {
            val itemId = entry.key
            val itemCount = entry.value
            val itemValue = ItemApi.getItemPriceInfo(itemId)?.asJsonObject?.get("basePrice")?.asFloat ?: 0F
            val multiValue = (itemValue * itemCount).toLong()

            totalItemWorth[itemId] = Pair(multiValue, itemCount)
        }
        totalItemWorth["SKYBLOCK_COIN"] = Pair(purseGainLoss.toLong(), 1)
        val sortedItems = totalItemWorth.entries.sortedByDescending { it.value.first }

//        sort then loop from high -> low
        var tempWorth = 0L
        val maxWidth = 100 - 12

        for ((i, entry) in sortedItems.withIndex()) {
            if (i >= 8) continue
            val itemId = entry.key
            val itemWorth = entry.value.first
            val itemCount = entry.value.second
            val itemStack = ItemApi.createItemStack(itemId) ?: continue
            tempWorth += itemWorth

            val countText = if (itemCount > 1) "§8x${itemCount.abbreviateNumber()} " else ""
            var displayName = itemStack.displayName

            if (itemStack.displayName.clean() == "Enchanted Book") {
                displayName = itemStack.getLore()[0]
            }

            val itemNameShort = if (displayName.clean().length > 17) displayName.substring(
                0,
                14
            ) + "§8..." else displayName

            val renderText = "$countText$itemNameShort §r- $${itemWorth.abbreviateNumber()}"

            val textWidth = fontRenderer.getStringWidth(renderText.clean())
            val scaleFactor = min(1.0, maxWidth / textWidth.toDouble())

            GuiUtils.renderItemStackOnScreen(itemStack, guiLeft + 144, guiTop + 107 + (11 * i), 12f, 12f)

            GlStateManager.pushMatrix()
            GlStateManager.scale(scaleFactor, scaleFactor, 1.0)

            fontRenderer.drawString(
                renderText,
                ((guiLeft + 144 + 14).toDouble() / scaleFactor).toFloat(), // Adjusted X position
                ((guiTop + 107 + (11 * i) + 4).toDouble() / scaleFactor).toFloat(), // Adjusted Y position
                0xFFFFFF,
                true
            )

            GlStateManager.popMatrix()
        }
        totalWorth = tempWorth
    }

    private fun drawFilteredItems() {
        val blocksPerRow = 5f
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);
        GlStateManager.color(1f, 1f, 1f)
        val selectedList = if (selectedFilterMode == "Whitelist") whitelistItems else blacklistItems

        for ((index, itemId) in selectedList.withIndex()) {
            val x = ((index % blocksPerRow) * 20)
            val y = floor(index.toFloat() / blocksPerRow) * 20f
            if (index > 34) continue
            drawItemButton(x, y, itemId)
        }
        GlStateManager.popMatrix()
    }

    private fun removeItem(itemId: String) {
        val selectedList = if (selectedFilterMode == "Whitelist") whitelistItems else blacklistItems
        selectedList.remove(itemId)
        newItemButtons.clear()
        // Refresh buttons
        GuiUtil.open(null)
        GuiUtil.open(ProfitTrackerGui())
    }

    private fun addItem(itemId: String) {
        val selectedList = if (selectedFilterMode == "Whitelist") whitelistItems else blacklistItems
        if (selectedList.contains(itemId)) return
        selectedList.add(selectedList.size - 1, itemId)
        newItemButtons.clear()
        // Refresh buttons
        GuiUtil.open(null)
        GuiUtil.open(ProfitTrackerGui())
    }

    private fun openPickerPopup() {
        itemPickerPopupX = mouseX
        val minY = Utils.mc.displayHeight / 2 - 128F
        itemPickerPopupY = min(minY.toDouble(), mouseY)
    }

    private fun drawItemButton(x: Float, y: Float, itemId: String) {
        val itemButtonWidth = 18f
        val itemButtonHeight = 18f
        val addItem = itemId == "ADD_ITEM"

        if (!newItemButtons.contains(itemId)) {
            newItemButtons[itemId] = Button(18f, 18f, (x + 18).toInt(), (y + 49).toInt())
            newItemButtons[itemId]?.onClicked = Runnable {
                if (addItem) {
                    openPickerPopup()
                    return@Runnable
                }
                removeItem(itemId)
            }
        }

        GlStateManager.color(1f, 1f, 1f)
        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);

        if (addItem) {
            GuiUtils.drawTexture(
                guiLeft + 18 + x,
                guiTop + 49 + y,
                itemButtonWidth,
                itemButtonHeight,
                257 / 400f,
                (257 + itemButtonWidth) / 400F,
                152 / 213f,
                (152 + itemButtonHeight) / 213F,
                GL11.GL_NEAREST
            )
        } else {
            val item = ItemApi.createItemStack(itemId)

            GuiUtils.drawTexture(
                guiLeft + 18 + x,
                guiTop + 49 + y,
                itemButtonWidth,
                itemButtonHeight,
                276 / 400f,
                (276 + itemButtonWidth) / 400F,
                152 / 213f,
                (152 + itemButtonHeight) / 213F,
                GL11.GL_NEAREST
            )
            GuiUtils.renderItemStackOnScreen(item, guiLeft + 18 + x + 1, guiTop + 49 + y + 1, 16f, 16f)

            if (mouseX > guiLeft + 18 + x && mouseX < guiLeft + 18 + x + itemButtonWidth &&
                mouseY > guiTop + 49 + y && mouseY < guiTop + 49 + y + itemButtonHeight
            ) {
                if (item != null) {
                    GlStateManager.pushMatrix()
                    GlStateManager.color(1f, 1f, 1f, 0.4f)

                    GuiUtils.renderItemStackOnScreen(
                        ItemStack(Item.getItemFromBlock(Blocks.barrier)),
                        guiLeft + 18 + x + 1,
                        guiTop + 49 + y + 1,
                        16f,
                        16f
                    )
                    GlStateManager.color(1f, 1f, 1f, 1f)

                    var hoverText = item.displayName
                    if (item.displayName.clean() == "Enchanted Book") {
                        hoverText = item.getLore()[0]
                    }

                    net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                        listOf(hoverText),
                        mouseX.toInt(),
                        mouseY.toInt(),
                        Utils.mc.displayWidth,
                        Utils.mc.displayHeight,
                        -1,
                        Utils.mc.fontRendererObj
                    )
                    GlStateManager.popMatrix()
                }
            }
        }
        GlStateManager.popMatrix()
    }

    private var startingItemPopupOffset = 0

    override fun onMouseScrolled(delta: Double) {
        super.onMouseScrolled(delta)

        if (delta > 0) {
            if (startingItemPopupOffset >= 5) {
                startingItemPopupOffset -= 5
            }
        } else {
            startingItemPopupOffset += 5
        }
    }

    private fun drawItemPickerPopup() {
        if (itemPickerPopupX != 0.0) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 100f)
            Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);
            GuiUtils.drawTexture(
                itemPickerPopupX.toFloat(),
                itemPickerPopupY.toFloat(),
                105F,
                125F,
                257 / 400f,
                (257 + 105) / 400F,
                0 / 213f,
                (0 + 125F) / 213F,
                GL11.GL_NEAREST
            )
            GlStateManager.popMatrix()

            val startingItemsX = itemPickerPopupX + 10
            val startingItemsY = itemPickerPopupY + 29

            // Close popup on lost focus
            if (mouseClicking) {
                if (mouseX < itemPickerPopupX || mouseX > itemPickerPopupX + 105 || mouseY < itemPickerPopupY || mouseY > itemPickerPopupY + 125) {
                    itemPickerPopupX = 0.0
                    itemPickerPopupY = 0.0
                }
            }

            var rawIndex = -1
            var drawn = 0
            val maxItemsToShow = 24 // Maximum number of items to show in the grid
            for ((itemId, skyblockItem) in ItemApi.getSkyblockItems().entrySet()) {
                val stack = ItemApi.createItemStack(itemId) ?: continue
                if (stack.displayName.endsWith(")")) continue

                val search = searchField?.text?.toLowerCase() as CharSequence
                val matchesSearch =
                    stack.displayName.clean().toLowerCase().contains(search) || itemId.toLowerCase()
                        .contains(search) || itemId.toLowerCase().replace("_", " ").contains(search)
                if (!matchesSearch) continue

                rawIndex++
                if (rawIndex < startingItemPopupOffset || rawIndex > startingItemPopupOffset + maxItemsToShow) continue
                drawn++
                val relativeIndex = rawIndex - startingItemPopupOffset // Adjusted index considering offset
                val x = startingItemsX + ((relativeIndex % 5) * 18) - 2
                val y = startingItemsY + (floor(relativeIndex.toFloat() / 5) * 18f) - 2

                GlStateManager.pushMatrix()
                GlStateManager.translate(0f, 0f, 105f)
                GuiUtils.renderItemStackOnScreen(
                    stack,
                    x.toFloat(),
                    y.toFloat(),
                    16f,
                    16f
                )
                val itemName = skyblockItem.asJsonObject.get("displayname").asString
                val itemLore = skyblockItem.asJsonObject.getAsJsonArray("lore")
                val textToShow = mutableListOf(itemName)
                itemLore?.forEach { loreItem ->
                    textToShow.add(loreItem.asString)
                }

                if (mouseX > x.toFloat() && mouseX < x + 16 && mouseY > y.toFloat() && mouseY < y + 16) {
                    net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                        textToShow,
                        mouseX.toInt(),
                        mouseY.toInt(),
                        Utils.mc.displayWidth,
                        Utils.mc.displayHeight,
                        -1,
                        Utils.mc.fontRendererObj
                    )
                }

                if (mouseClicking) {
                    if (mouseX > x.toFloat() && mouseX < x + 16 && mouseY > y.toFloat() && mouseY < y + 16) {
                        addItem(itemId)
                    }
                }
                GlStateManager.popMatrix()
            }
            if (drawn == 0 && startingItemPopupOffset > 0) {
                startingItemPopupOffset -= 5
            }

            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 110f)
            searchField?.visible = true
            searchField?.setCanLoseFocus(false)
            searchField?.isFocused = true
            searchField?.enableBackgroundDrawing = false
            searchField?.xPosition = startingItemsX.toInt() - 1
            searchField?.yPosition = itemPickerPopupY.toInt() + 9
            searchField?.drawTextBox()
            GlStateManager.popMatrix()
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()

        DataManager.saveData("GPTwhitelist", whitelistItems)
        DataManager.saveData("GPTblacklist", blacklistItems)
    }
}