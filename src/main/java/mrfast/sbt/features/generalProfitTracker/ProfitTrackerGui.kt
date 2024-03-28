package mrfast.sbt.features.generalProfitTracker

import gg.essential.api.utils.GuiUtil
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.WindowScreen
import gg.essential.universal.UMatrixStack
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.blacklistItems
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.paused
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.pausedDuration
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.selectedFilterMode
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.sessionStartedAt
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.started
import mrfast.sbt.features.generalProfitTracker.GeneralProfitTracker.whitelistItems
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.toFormattedTime
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
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

    private val newItemButtons = mutableMapOf<String, Button>()

    class Button(var width: Float, var height: Float, var x: Int, var y: Int) {
        fun isClicked(mouseX: Double, mouseY: Double, guiLeft: Float, guiTop: Float): Boolean {
            val clicked =
                (mouseX >= (x + guiLeft) && mouseX <= (x + guiLeft) + width && mouseY >= (y + guiTop) && mouseY <= (y + guiTop) + height)
            if (clicked) {
                this.onClicked?.run()
            }
            return clicked
        }

        var onClicked: Runnable? = null
    }

    override fun onMouseClicked(mouseX: Double, mouseY: Double, mouseButton: Int) {
        super.onMouseClicked(mouseX, mouseY, mouseButton)
        this.mouseX = mouseX
        this.mouseY = mouseY

        if (startStopButton.isClicked(mouseX, mouseY, guiLeft, guiTop)) {
            started = !started

            // Clicking Started runs this
            if (started) {
                sessionStartedAt = System.currentTimeMillis()
                GeneralProfitTracker.itemsGainedDuringSession.clear()
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
        val items = GeneralProfitTracker.itemsGainedDuringSession
        val totalItemWorth = mutableMapOf<String, Pair<Long, Int>>()

        // Populate totalItemWorth map
        for (entry in items) {
            val itemId = entry.key
            val itemCount = entry.value
            val itemValue = ItemApi.getItemPriceInfo(itemId)?.asJsonObject?.get("worth")?.asFloat ?: 0F
            val multiValue = (itemValue * itemCount).toLong()

            totalItemWorth[itemId] = Pair(multiValue, itemCount)
        }

        val sortedItems = totalItemWorth.entries.sortedByDescending { it.value.first }

//        sort then loop from high -> low
        var tempWorth = 0L
        val maxWidth = 100 - 12

        for ((i, entry) in sortedItems.withIndex()) {
            if (i > 8) continue
            val itemId = entry.key
            val itemWorth = entry.value.first
            val itemCount = entry.value.second
            val itemStack = ItemApi.createItemStack(itemId) ?: continue
            tempWorth += itemWorth

            val countText = if (itemCount > 1) "§8x${itemCount.abbreviateNumber()} " else ""
            val itemNameShort = if (itemStack.displayName.clean().length > 17) itemStack.displayName.substring(
                0,
                14
            ) + "§8..." else itemStack.displayName
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
            if (index > 35) continue
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
        itemPickerPopupY = mouseY


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
                println("NEED TO REMOVE $itemId")
                removeItem(itemId)
            }
        }

        GlStateManager.pushMatrix()
        Minecraft.getMinecraft().textureManager.bindTexture(boxTexture);
        GlStateManager.color(1f, 1f, 1f)

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
        }
        GlStateManager.popMatrix()
    }

    private fun drawItemPickerPopup() {
        if (itemPickerPopupX != 0.0) {
            GlStateManager.pushMatrix()
            GlStateManager.translate(0f, 0f, 200f)
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
            val startingItemsX = itemPickerPopupX + 10
            val startingItemsY = itemPickerPopupY + 29
            var index = 0

            // Close popup on lost focus
            if (mouseClicking) {
                if (mouseX < itemPickerPopupX || mouseX > itemPickerPopupX + 105 || mouseY < itemPickerPopupY || mouseY > itemPickerPopupY + 125) {
                    itemPickerPopupX = 0.0
                    itemPickerPopupY = 0.0
                }
            }

            for ((itemId, skyblockItem) in ItemApi.getSkyblockItems().entrySet()) {
                if (index > 24) continue
                val stack = ItemApi.createItemStack(itemId) ?: continue
                val x = startingItemsX + ((index % 5) * 18) - 2
                val y = startingItemsY + (floor(index.toFloat() / 5) * 18f) - 2

                GuiUtils.renderItemStackOnScreen(
                    stack,
                    x.toFloat(),
                    y.toFloat(),
                    16f,
                    16f
                )
                if (mouseClicking) {
                    if (mouseX > x.toFloat() && mouseX < x + 16 && mouseY > y.toFloat() && mouseY < y + 16) {
                        println("NEED TO ADD $itemId")
                        addItem(itemId)
                    }
                }
                index++
            }
            GlStateManager.popMatrix()
        }
    }

    override fun onScreenClose() {
        super.onScreenClose()
    }

    init {

    }
}