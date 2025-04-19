package mrfast.sbt.guis

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.guis.components.*
import mrfast.sbt.managers.DataManager
import mrfast.sbt.managers.TradeManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getStringWidth
import mrfast.sbt.utils.Utils.toDateTimestamp
import net.minecraft.client.Minecraft
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import org.lwjgl.input.Keyboard
import java.awt.Color

/*
 * @TODO
 *   - Add a info icon in top of gui that tells you how to change profit, and to press the enter key
 */
class TradeHistoryGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var stackElements: MutableMap<UIComponent, ItemStack?> = mutableMapOf()
    private var searchQuery = ""
    private var tradeHistory = TradeManager.tradeHistory

    override fun onScreenClose() {
        super.onScreenClose()
    }

    private fun UIComponent.addTooltip(set: Set<String>, stack: ItemStack? = null) {
        tooltipElements[this] = set
        stackElements[this] = stack
    }

    override fun onDrawScreen(matrixStack: UMatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float) {
        if (CustomizationConfig.backgroundBlur) GuiUtils.drawBackgroundBlur()

        super.onDrawScreen(matrixStack, mouseX, mouseY, partialTicks)
        for (element in tooltipElements.keys) {
            if (element.isHovered()) {
                val tooltip = tooltipElements[element]?.toMutableList() ?: mutableListOf()
                val item = stackElements[element] ?: ItemStack(Items.apple)

                // Post the event and modify the tooltip
                val event = ItemTooltipEvent(item, Utils.mc.thePlayer, tooltip, false)
                MinecraftForge.EVENT_BUS.post(event)

                // Draw the tooltip after it has been modified
                net.minecraftforge.fml.client.config.GuiUtils.drawHoveringText(
                    event.toolTip, // Use the modified tooltip
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

    private val mainBorderRadius = 6f
    private var body: ScrollComponent? = null

    init {
        // Create a background panel
        val background = OutlinedRoundedRectangle(
            CustomizationConfig.windowBorderColor.get().constraint,
            2f,
            mainBorderRadius
        ).constrain {
            color = CustomizationConfig.mainBackgroundColor.colorState.constraint
            width = MinConstraint(70.percent, 600.pixels)
            height = MinConstraint(70.percent, 400.pixels)
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf window

        val header = UIRoundedRectangle(mainBorderRadius - 1).constrain {
            width = 100.percent - 4.pixels
            height = min(30.pixels, 10.percent)
            x = 2.pixels()
            y = 2.pixels()
            color = CustomizationConfig.headerBackgroundColor.get().constraint
        } childOf background effect ScissorEffect()

        if (DeveloperConfig.showInspector && CustomizationConfig.developerMode) Inspector(background) childOf window

        CustomUIText("§7Trade Log History", true, scale = 2f).constrain {
            x = CenterConstraint()
            y = 6.pixels
        } childOf header

        val searchBar = OutlinedRoundedRectangle(CustomizationConfig.guiLineColors.get().constraint, 1f, 3f).constrain {
            color = CustomizationConfig.mainBackgroundColor.get().constraint
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

        val block = UIRoundedRectangle(4f).constrain {
            color = CustomizationConfig.headerBackgroundColor.colorState.constraint
            x = CenterConstraint()
            y = SiblingConstraintFixed(4f)
            width = 100.percent - 16.pixels
            height = 90.percent
        } childOf background

        body = ScrollComponent().constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 100.percent
        } childOf block

        this.window.onMouseScroll {
            it.stopImmediatePropagation()
            body!!.mouseScroll(it.delta)
        }

        val scrollbar = UIRoundedRectangle(3f).constrain {
            width = 5.pixels
            height = 100.percent
            x = PixelConstraint(0f, alignOpposite = true)
            color = Color(200, 200, 200, 200).constraint
        } childOf block

        body!!.setVerticalScrollBarComponent(scrollbar, true)

        loadTradeDates(body!!)

        searchBarInput.onKeyType { _, keycode ->
            if (keycode == Keyboard.KEY_ESCAPE) {
                GuiUtils.closeGui()
                return@onKeyType
            }
            searchQuery = searchBarInput.getText()
            body!!.clearChildren()
            loadTradeDates(body!!)
        }
    }

    private fun loadTradeDates(body: ScrollComponent) {
        stackElements.clear()
        tooltipElements.clear()
        var first = true
        tradeHistory.entrySet().reversed().forEach {
            if (!dateShouldShow(it.key)) return@forEach

            val tradesArray = it.value.asJsonArray
            val group = UIBlock(Color(0, 0, 0, 0)).constrain {
                x = CenterConstraint()
                y = if (first) 2.pixels else SiblingConstraintFixed(4f)
                width = 100.percent
                height = 165.pixels
            } childOf body

            if (first) first = false

            val date = CustomUIText("§7${it.key}", scale = 1.4f).constrain {
                x = CenterConstraint()
                y = 0.pixels
            } childOf group

            getCombinedChanges(date, it.key)

            val tradeScrollContainer =
                ScrollComponent("No Trades", horizontalScrollEnabled = true, verticalScrollEnabled = false).constrain {
                    x = 0.pixels
                    y = SiblingConstraintFixed(2f)
                    width = 100.percent
                    height = 100.percent - 26.pixels
                } childOf group

            tradeScrollContainer.onMouseScroll {
                if (!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                    it.stopImmediatePropagation()
                    body.scrollTo(verticalOffset = (body.verticalOffset + it.delta * 20).toFloat())
                }
            }

            val scrollbar = UIRoundedRectangle(3f).constrain {
                width = 100.percent
                height = 5.pixels
                y = 0.pixels(alignOpposite = true)
                color = Color(200, 200, 200, 200).constraint
            } childOf group

            tradeScrollContainer.setHorizontalScrollBarComponent(scrollbar, true)

            tradesArray.reversed().forEach { it2 ->
                createTradeContainer(tradeScrollContainer, it2.asJsonObject, it.key)
            }
        }
    }

    private fun getCombinedChanges(dateComponent: UIComponent, date: String) {
        var currentCoins = 0L
        val itemCounts = mutableMapOf<JsonObject, Long>()

        tradeHistory[date].asJsonArray.forEach {
            val trade = it.asJsonObject
            currentCoins -= trade.get("yourCoins").asLong
            currentCoins += trade.get("theirCoins").asLong

            trade.get("yourItems").asJsonArray.forEach {
                val item = it.asJsonObject
                itemCounts[item] = itemCounts.getOrDefault(item, 0) - 1
            }

            trade.get("theirItems").asJsonArray.forEach {
                val item = it.asJsonObject
                itemCounts[item] = itemCounts.getOrDefault(item, 0) + 1
            }
        }

        val gainedItems = JsonArray()
        val lostItems = JsonArray()

        itemCounts.forEach { (item, count) ->
            when {
                count > 0 -> repeat(count.toInt()) { gainedItems.add(item) }
                count < 0 -> repeat(-count.toInt()) { lostItems.add(item) }
            }
        }

        val hoverText = mutableListOf<String>()
        hoverText.add("§e§l$date Trade Summary")
        hoverText.add("§7Coin Change: ${if (currentCoins == 0L) "§6" else if (currentCoins > 0L) "§6" else "§c"}${currentCoins.abbreviateNumber()}")
        if (gainedItems.size() + lostItems.size() != 0) {
            hoverText.add("§7Item Changes: ")
            gainedItems.forEach {
                val item = ItemStack(
                    Item.getByNameOrId(it.asJsonObject.get("id").asString),
                    it.asJsonObject.get("count").asInt
                )
                val nbt = it.asJsonObject.get("nbt").asString
                item.tagCompound = JsonToNBT.getTagFromJson(nbt)
                hoverText.add(" §a+ ${item.displayName.clean()} x${item.stackSize}")
            }
            lostItems.forEach {
                val item = ItemStack(
                    Item.getByNameOrId(it.asJsonObject.get("id").asString),
                    it.asJsonObject.get("count").asInt
                )
                val nbt = it.asJsonObject.get("nbt").asString
                item.tagCompound = JsonToNBT.getTagFromJson(nbt)
                hoverText.add(" §c- ${item.displayName.clean()} x${item.stackSize}")
            }
        }

        dateComponent.addTooltip(hoverText.toSet())
    }

    private fun dateShouldShow(date: String): Boolean {
        if (searchQuery.isEmpty() || date.contains(searchQuery, ignoreCase = true)) return true

        tradeHistory[date].asJsonArray.forEach {
            val trade = it.asJsonObject
            if (matchesSearch(trade, date)) return true
        }
        return false
    }

    private fun matchesSearch(trade: JsonObject, date: String): Boolean {
        if (searchQuery.isEmpty() || date.contains(searchQuery, ignoreCase = true)) return true

        if (trade.get("username").asString.contains(searchQuery, ignoreCase = true)) return true

        val yourItems = trade.getAsJsonArray("yourItems")
        yourItems.forEach {
            val item = ItemStack(
                Item.getByNameOrId(it.asJsonObject.get("id").asString),
                it.asJsonObject.get("count").asInt
            )
            val nbt = it.asJsonObject.get("nbt").asString
            item.tagCompound = JsonToNBT.getTagFromJson(nbt)

            if (item.displayName.clean().contains(searchQuery, ignoreCase = true)) return true
            if (item.getLore(true).joinToString(" ").contains(searchQuery, ignoreCase = true)) return true
        }

        val theirItems = trade.getAsJsonArray("theirItems")
        theirItems.forEach {
            val item = ItemStack(
                Item.getByNameOrId(it.asJsonObject.get("id").asString),
                it.asJsonObject.get("count").asInt
            )
            val nbt = it.asJsonObject.get("nbt").asString
            item.tagCompound = JsonToNBT.getTagFromJson(nbt)

            if (item.displayName.clean().contains(searchQuery, ignoreCase = true)) return true
            if (item.getLore(true).joinToString(" ").contains(searchQuery)) return true
        }

        // hide by default
        return false
    }

    private fun createTradeContainer(parent: UIComponent, trade: JsonObject, date: String) {
        if (!matchesSearch(trade, date)) return

        val group = OutlinedRoundedRectangle(CustomizationConfig.guiLineColors.get().constraint, 2f, 3f).constrain {
            color = CustomizationConfig.mainBackgroundColor.get().constraint
            width = 202.pixels
            height = 100.percent
            x = SiblingConstraintFixed(4f)
            y = 0.pixels
        } childOf parent

        val timeBlock = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 15.pixels
        } childOf group

        val time = (trade.get("timestamp").asLong).toDateTimestamp(true)
        val timeText = CustomUIText("§e$time").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf timeBlock

        val horizontalLine1 = UIBlock(CustomizationConfig.guiLineColors.get().constraint).constrain {
            x = 2.pixels
            y = 15.pixels
            width = 100.percent - 2.pixels
            height = 1.pixels
        } childOf group

        val horizontalLine2 = UIBlock(CustomizationConfig.guiLineColors.get().constraint).constrain {
            x = 2.pixels
            y = 35.pixels
            width = 100.percent - 2.pixels
            height = 1.pixels
        } childOf group

        val verticalLine = UIBlock(CustomizationConfig.guiLineColors.get().constraint).constrain {
            x = 100.pixels
            y = 37.pixels
            width = 1.pixels
            height = 100.percent - 2.pixels
        } childOf group

        val leftBlock = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 0.pixels
            y = 15.pixels
            width = 100.pixels
            height = 100.percent - 15.pixels
        } childOf group

        val youBlock = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 0.pixels
            y = 0.pixels
            width = 100.percent
            height = 20.pixels
        } childOf leftBlock

        val rightBlock = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 102.pixels
            y = 15.pixels
            width = 100.pixels
            height = 100.percent - 15.pixels
        } childOf group

        val usernameBlock = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = 0.pixels(alignOpposite = true)
            y = 0.pixels
            width = 100.pixels
            height = 20.pixels
        } childOf rightBlock

        val traderName = trade.get("username").asString
        val yourItemWorth = getItemWorth("yourItems", trade)
        val theirItemWorth = getItemWorth("theirItems", trade)

        createYourSide(
            trade,
            time,
            timeText,
            leftBlock,
            youBlock,
            theirItemWorth
        )
        createTheirSide(
            traderName,
            trade,
            time,
            timeText,
            rightBlock,
            usernameBlock,
            yourItemWorth
        )

        if (trade.has("yourCustomValue") || trade.has("theirCustomValue")) {
            var yourWorth = yourItemWorth
            if(trade.has("yourCustomValue")) yourWorth = trade.get("yourCustomValue").asLong

            var theirWorth = theirItemWorth
            if(trade.has("theirCustomValue")) theirWorth = trade.get("theirCustomValue").asLong

            updateTimeText(timeText, time, theirWorth, yourWorth)
        }
    }

    private fun getItemWorth(who: String, trade: JsonObject): Long {
        val items = trade.getAsJsonArray(who)
        var worth = createInventoryAndGetWorth(items, null)
        worth += trade.get("yourCoins").asLong
        return worth
    }

    private fun createYourSide(trade: JsonObject,
                               time: String,
                               timeText: CustomUIText,
                               leftBlock: UIComponent,
                               youBlock: UIComponent,
                               theirWorthRaw: Long) {
        var theirWorth = theirWorthRaw
        CustomUIText("You").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf youBlock

        val yourItems = trade.getAsJsonArray("yourItems")
        var yourItemWorth = createInventoryAndGetWorth(yourItems, leftBlock)
        yourItemWorth += trade.get("yourCoins").asLong
        var yourWorth = yourItemWorth

        // Check if the trade has a custom value set
        if (trade.has("yourCustomValue")) {
            if (trade.get("yourCustomValue").asLong != 0L) {
                yourWorth = trade.get("yourCustomValue").asLong
            }
        }

        val yourCoinString = "§6${trade.get("yourCoins").asLong.abbreviateNumber()} Coins §8§o≈"

        val bottomContainer = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = CenterConstraint()
            y = 100.percent - 12.pixels
            width = 0.pixels
            height = 12.pixels
        } childOf leftBlock

        val yourCoinText = CustomUIText(
            yourCoinString
        ).constrain {
            x = 0.pixels
            y = 100.percent - 12.pixels
        } childOf bottomContainer

        updateTimeText(timeText, time, theirWorth, yourWorth)

        val input = TextInputComponent(
            yourWorth.abbreviateNumber(),
            "Est. Val",
            45,
            fancy = false,
            Color(0x555555),
            6,
            dynamicWidth = true
        ).constrain {
            x = SiblingConstraintFixed(0f)
            y = 100.percent - 12.pixels
        } childOf bottomContainer

        input.onEnterPressed {
            yourWorth = handleInput(input.text)
            if (yourWorth == 0L) {
                yourWorth = yourItemWorth
            }
            input.setTextValue(yourWorth.abbreviateNumber())
            input.setWidth((input.text.getStringWidth() + 3).pixels)

            if (trade.has("theirCustomValue")) {
                if (trade.get("theirCustomValue").asLong != 0L) {
                    theirWorth = trade.get("theirCustomValue").asLong
                }
            }

            updateTimeText(timeText, time, theirWorth, yourWorth)

            trade.addProperty("yourCustomValue", yourWorth)
            DataManager.saveProfileData("tradeHistory", tradeHistory)
            input.loseFocus()
            input.releaseWindowFocus()
            bottomContainer.setWidth(PixelConstraint(yourCoinText.getWidth() + input.getWidth()))
            yourCoinText.scale2 = if (yourCoinText.getWidth() + input.getWidth() > 85) 0.9f else 1f
        }

        input.addTooltip(
            setOf(
                "§e§lClick to edit estimated value",
                "§7Press §a§lENTER §7to save",
                "§7Press §c§lESCAPE §7to cancel",
                "§7Delete §c§lall §7to reset",
            )
        )

        yourCoinText.scale2 = if (yourCoinText.getWidth() + input.getWidth() > 85) 0.9f else 1f
        input.setWidth((input.text.getStringWidth() + 5).pixels)
        bottomContainer.setWidth(PixelConstraint(yourCoinText.getWidth() + input.getWidth()))
    }

    fun createTheirSide(
        traderName: String,
        trade: JsonObject,
        time: String,
        timeText: CustomUIText,
        rightBlock: UIComponent,
        usernameBlock: UIComponent,
        yourWorthRaw: Long
    ) {
        var yourWorth = yourWorthRaw
        CustomUIText(traderName).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf usernameBlock

        val theirItems = trade.getAsJsonArray("theirItems")
        var theirItemWorth = createInventoryAndGetWorth(theirItems, rightBlock)
        theirItemWorth += trade.get("theirCoins").asLong
        var theirWorth = theirItemWorth

        // Check if the trade has a custom value set
        if (trade.has("theirCustomValue")) {
            if (trade.get("theirCustomValue").asLong != 0L) {
                theirWorth = trade.get("theirCustomValue").asLong
            }
        }

        val theirCoinString = "§6${trade.get("theirCoins").asLong.abbreviateNumber()} Coins §8§o≈"

        val bottomContainer = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = CenterConstraint()
            y = 100.percent - 12.pixels
            width = 0.pixels
            height = 12.pixels
        } childOf rightBlock

        val theirCoinText = CustomUIText(
            theirCoinString
        ).constrain {
            x = 0.pixels
            y = 100.percent - 12.pixels
        } childOf bottomContainer

        updateTimeText(timeText, time, theirWorth, yourWorth)

        val input = TextInputComponent(
            theirWorth.abbreviateNumber(),
            "Est. Val",
            45,
            fancy = false,
            Color(0x555555),
            6,
            dynamicWidth = true
        ).constrain {
            x = SiblingConstraintFixed(0f)
            y = 100.percent - 12.pixels
        } childOf bottomContainer

        input.onEnterPressed {
            theirWorth = handleInput(input.text)
            if (theirWorth == 0L) {
                theirWorth = theirItemWorth
            }

            input.setTextValue(theirWorth.abbreviateNumber())
            input.setWidth((input.text.getStringWidth() + 3).pixels)

            if (trade.has("yourCustomValue")) {
                if (trade.get("yourCustomValue").asLong != 0L) {
                    yourWorth = trade.get("yourCustomValue").asLong
                }
            }

            updateTimeText(timeText, time, theirWorth, yourWorth)
            trade.addProperty("theirCustomValue", theirWorth)
            DataManager.saveProfileData("tradeHistory", tradeHistory)
            input.loseFocus()
            input.releaseWindowFocus()
            bottomContainer.setWidth(PixelConstraint(theirCoinText.getWidth() + input.getWidth()))
            theirCoinText.scale2 = if (theirCoinText.getWidth() + input.getWidth() > 85) 0.9f else 1f
        }

        input.addTooltip(
            setOf(
                "§e§lClick to edit estimated value",
                "§7Press §a§lENTER §7to save",
                "§7Press §c§lESCAPE §7to cancel",
                "§7Delete §c§lall §7to reset",
            )
        )

        input.setWidth((input.text.getStringWidth() + 5).pixels)
        bottomContainer.setWidth(PixelConstraint(theirCoinText.getWidth() + input.getWidth()))
        theirCoinText.scale2 = if (theirCoinText.getWidth() + input.getWidth() > 85) 0.9f else 1f
    }

    fun createInventoryAndGetWorth(theirItems: JsonArray, block: UIComponent?): Long {
        var itemWorth = 0L
        for ((index, jsonElement) in theirItems.withIndex()) {
            val newX = index % 4
            val newY = index / 4
            val item = ItemStack(
                Item.getByNameOrId(jsonElement.asJsonObject.get("id").asString),
                jsonElement.asJsonObject.get("count").asInt
            )
            val nbt = jsonElement.asJsonObject.get("nbt").asString
            item.tagCompound = JsonToNBT.getTagFromJson(nbt)
            item.itemDamage = jsonElement.asJsonObject.get("damage").asInt

            val lore = item.getLore()

            val suggestedListing = ItemUtils.getSuggestListingPrice(item)
            if (suggestedListing != null) {
                val price = suggestedListing.get("price").asDouble
                if (price == 0.0) {
                    if (item.getSkyblockId() != null) {
                        val basePrice =
                            ItemUtils.getItemBasePrice(item.getSkyblockId()!!) * (jsonElement.asJsonObject.get("count").asInt)

                        itemWorth += basePrice.toLong()
                    }
                } else {
                    itemWorth += price.toLong()
                }
            }

            lore.add(0, item.displayName)

            if(block!=null) {
                val itemIcon = ItemComponent(item, 20f).constrain {
                    x = (newX * 22).pixels + 8.pixels
                    y = (newY * 22).pixels + 22.pixels
                    height = 20.pixels
                    width = 20.pixels
                } childOf block
                itemIcon.addTooltip(lore.toSet(), item)
            }
        }
        return itemWorth
    }

    private fun updateTimeText(timeText: CustomUIText, time: String, theirWorth: Long, yourWorth: Long) {
        if (theirWorth > yourWorth) {
            timeText.setText("§e$time §a+${(theirWorth - yourWorth).abbreviateNumber()}")
        } else {
            timeText.setText("§e$time §c-${(yourWorth - theirWorth).abbreviateNumber()}")
        }
    }

    private fun handleInput(inputText: String): Long {
        val lowercase = inputText.trim().lowercase()

        // Match something like "205.5k", "5.e1k", "100k", "5.k1", etc.
        val match = Regex("""([0-9]*\.?[0-9]+)[^a-zA-Z]*([km]?)""").find(lowercase)

        if (match != null) {
            val (numberStr, suffix) = match.destructured
            val number = numberStr.toDoubleOrNull() ?: return 0L

            return when (suffix) {
                "k" -> (number * 1_000).toLong()
                "m" -> (number * 1_000_000).toLong()
                else -> number.toLong()
            }
        }

        return 0L
    }
}