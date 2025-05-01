package mrfast.sbt.guis

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIImage
import gg.essential.elementa.components.UIRoundedRectangle
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.*
import gg.essential.elementa.constraints.animation.Animations
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
import mrfast.sbt.utils.Utils.formatNumber
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
import kotlin.math.absoluteValue

/*
 * @TODO
 *   - Add a info icon in top of gui that tells you how to change profit, and to press the enter key
 */
class TradeHistoryGui : WindowScreen(ElementaVersion.V2, newGuiScale = 2) {
    private var tooltipElements: MutableMap<UIComponent, Set<String>> = mutableMapOf()
    private var stackElements: MutableMap<UIComponent, ItemStack?> = mutableMapOf()
    private var searchQuery = ""
    private var tradeHistory = TradeManager.tradeHistory

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

    private fun createBackground(): OutlinedRoundedRectangle {
        return OutlinedRoundedRectangle(
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
    }

    private fun createHeader(background: UIComponent): UIComponent {
        val header = UIRoundedRectangle(mainBorderRadius - 1).constrain {
            width = 100.percent - 4.pixels
            height = min(30.pixels, 10.percent)
            x = 2.pixels()
            y = 2.pixels()
            color = CustomizationConfig.headerBackgroundColor.get().constraint
        } childOf background effect ScissorEffect()

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

        searchBarInput.onKeyType { _, keycode ->
            if (keycode == Keyboard.KEY_ESCAPE) {
                GuiUtils.closeGui()
                return@onKeyType
            }
            searchQuery = searchBarInput.getText()
            body!!.clearChildren()
            loadTradeDates(body!!)
        }

        return header
    }

    private fun createContentArea(background: UIComponent) {
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
    }

    init {
        val background = createBackground()

        createHeader(background)

        if (DeveloperConfig.showInspector && CustomizationConfig.developerMode) Inspector(background) childOf window

        createContentArea(background)

        loadTradeDates(body!!)
    }

    private fun loadTradeDates(body: ScrollComponent) {
        stackElements.clear()
        tooltipElements.clear()
        var first = true
        tradeHistory.entrySet().reversed().forEach {
            if (!dateShouldShow(it.key)) return@forEach

            val tradesArray = it.value.asJsonArray
            if(tradesArray.size() == 0) return@forEach

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
        val hoverText = mutableListOf<String>().apply {
            add("§e§l$date Trade Summary")

            // Iterate through trade history to calculate coin changes and item counts
            tradeHistory[date].asJsonArray.forEach { trade ->
                val tradeObj = trade.asJsonObject
                currentCoins += tradeObj.get("theirCoins").asLong - tradeObj.get("yourCoins").asLong

                // Process item counts for both "yourItems" and "theirItems"
                tradeObj.get("yourItems").asJsonArray.forEach { item ->
                    val itemObj = item.asJsonObject
                    itemCounts[itemObj] = itemCounts.getOrDefault(itemObj, 0) - 1
                }
                tradeObj.get("theirItems").asJsonArray.forEach { item ->
                    val itemObj = item.asJsonObject
                    itemCounts[itemObj] = itemCounts.getOrDefault(itemObj, 0) + 1
                }
            }

            // Add coin change info to hoverText
            add("§7Coin Change: ${if (currentCoins == 0L) "§6" else if (currentCoins > 0L) "§6" else "§c"}${currentCoins.abbreviateNumber()}")

            // Process item changes
            if (itemCounts.isNotEmpty()) {
                add("§7Item Changes: ")
                itemCounts.forEach { (item, count) ->
                    val itemStack = ItemStack(Item.getByNameOrId(item.get("id").asString), item.get("count").asInt).apply {
                        tagCompound = JsonToNBT.getTagFromJson(item.get("nbt").asString)
                    }
                    val displayText = " ${if (count > 0) "§a+" else "§c-"} ${itemStack.displayName.clean()} x${itemStack.stackSize}"

                    repeat(count.absoluteValue.toInt()) { add(displayText) }
                }
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

        val unhovered = Color(200, 200, 200)
        val hovered = Color(255, 255, 255)

        val deleteTradeImg = UIImage.ofResource("/assets/skyblocktweaks/gui/delete.png").constrain {
            width = 10.pixels
            height = 11.pixels
            y = 1.pixels(true)
            x = PixelConstraint(3f, true)
            color = unhovered.constraint
        } childOf timeBlock

        deleteTradeImg.onMouseEnterRunnable {
            deleteTradeImg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, hovered.constraint)
            }
        }
        deleteTradeImg.onMouseLeaveRunnable {
            deleteTradeImg.animate {
                setColorAnimation(Animations.OUT_EXP, 0.5f, unhovered.constraint)
            }
        }
        var foundTrade = false
        var targetDate = ""

        deleteTradeImg.onMouseClick {
            tradeHistory.entrySet().forEach { it1 ->
                if(foundTrade) return@forEach

                it1.value.asJsonArray.forEach { it2 ->
                    if(foundTrade) return@forEach

                    if (it2.asJsonObject.get("timestamp").asLong == trade.get("timestamp").asLong) {
                        foundTrade = true
                        targetDate = it1.key
                        parent.removeChild(group)
                    }
                }
            }

            tradeHistory[targetDate].asJsonArray.removeAll { it.equals(trade) }
            DataManager.saveProfileData("tradeHistory", tradeHistory)
        }
        deleteTradeImg.addTooltip(
            setOf(
                "§c§lDelete Trade",
                "§7Click to delete this trade from the history.",
            )
        )

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

        createTradeSide("your", yourItemWorth, trade, timeText, time, leftBlock, youBlock, traderName)
        createTradeSide("their", theirItemWorth, trade, timeText, time, rightBlock, usernameBlock, traderName)

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

    private fun createTradeSide(side: String, // either "your" or "their"
                                rawWorth: Long,
                                trade: JsonObject,
                                timeText: CustomUIText,
                                time: String,
                                block: UIBlock,
                                usernameBlock: UIBlock,
                                traderName: String
    ): Long {
        val isYourSide = side == "your"
        var otherWorth = if (isYourSide) trade.get("theirCustomValue")?.asLong ?: rawWorth else trade.get("yourCustomValue")?.asLong ?: rawWorth

        val label = CustomUIText(if (isYourSide) "You" else traderName).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
        } childOf usernameBlock

        val items = trade.getAsJsonArray("${side}Items")
        var itemWorth = createInventoryAndGetWorth(items, block)
        itemWorth += trade.get("${side}Coins").asLong
        var sideWorth = itemWorth

        val customValueKey = "${side}CustomValue"
        if (trade.has(customValueKey) && trade.get(customValueKey).asLong != 0L) {
            sideWorth = trade.get(customValueKey).asLong
        }

        val coinString = "§6${trade.get("${side}Coins").asLong.abbreviateNumber()} Coins §8§o≈"

        val bottomContainer = UIBlock(Color(0, 0, 0, 0)).constrain {
            x = CenterConstraint()
            y = 100.percent - 12.pixels
            width = 0.pixels
            height = 12.pixels
        } childOf block

        val coinText = CustomUIText(coinString).constrain {
            x = 0.pixels
            y = 100.percent - 12.pixels
        } childOf bottomContainer

        updateTimeText(timeText, time, if (isYourSide) otherWorth else sideWorth, if (isYourSide) sideWorth else otherWorth)

        val input = TextInputComponent(
            sideWorth.abbreviateNumber(),
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
            sideWorth = handleInput(input.text)
            if (sideWorth == 0L) sideWorth = itemWorth

            input.setTextValue(sideWorth.abbreviateNumber())
            input.setWidth((input.text.getStringWidth() + 3).pixels)

            // Update otherWorth from opposing custom value if exists
            val opposingKey = if (isYourSide) "theirCustomValue" else "yourCustomValue"
            if (trade.has(opposingKey) && trade.get(opposingKey).asLong != 0L) {
                otherWorth = trade.get(opposingKey).asLong
            }

            updateTimeText(timeText, time, if (isYourSide) otherWorth else sideWorth, if (isYourSide) sideWorth else otherWorth)

            trade.addProperty(customValueKey, sideWorth)
            DataManager.saveProfileData("tradeHistory", tradeHistory)
            input.loseFocus()
            input.releaseWindowFocus()
            bottomContainer.setWidth(PixelConstraint(coinText.getWidth() + input.getWidth()))
            coinText.scale2 = if (coinText.getWidth() + input.getWidth() > 85) 0.9f else 1f
        }

        input.addTooltip(
            setOf(
                "§6§l${sideWorth.formatNumber()} Coins",
                "§e§lClick to edit estimated value",
                "§7Press §a§lENTER §7to save",
                "§7Press §c§lESCAPE §7to cancel",
                "§7Delete §c§lall §7to reset",
            )
        )

        input.setWidth((input.text.getStringWidth() + 5).pixels)
        bottomContainer.setWidth(PixelConstraint(coinText.getWidth() + input.getWidth()))
        coinText.scale2 = if (coinText.getWidth() + input.getWidth() > 85) 0.9f else 1f

        return sideWorth
    }

    // Create a 4x4 grid of items
    private fun createInventoryAndGetWorth(items: JsonArray, block: UIComponent?): Long {
        var totalWorth = 0L

        items.forEachIndexed { index, jsonElement ->
            val obj = jsonElement.asJsonObject
            val count = obj["count"].asInt
            val itemId = obj["id"].asString
            val damage = obj["damage"].asInt
            val nbt = obj["nbt"].asString

            val item = ItemStack(Item.getByNameOrId(itemId), count).apply {
                tagCompound = JsonToNBT.getTagFromJson(nbt)
                itemDamage = damage
            }

            val lore = item.getLore().apply { add(0, item.displayName) }.toSet()

            val suggestedListing = ItemUtils.getSuggestListingPrice(item)
            val price = suggestedListing?.get("price")?.asDouble ?: 0.0
            var itemValue = price.toLong()

            if (price == 0.0 && item.getSkyblockId() != null) itemValue = (ItemUtils.getItemBasePrice(item.getSkyblockId()!!) * count).toLong()

            totalWorth += itemValue

            if (block != null) {
                (ItemComponent(item, 20f).constrain {
                    x = (index % 4 * 22 + 8).pixels
                    y = (index / 4 * 22 + 22).pixels
                    width = 20.pixels
                    height = 20.pixels
                } childOf block).addTooltip(lore, item)
            }
        }

        return totalWorth
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
        val match = Regex("""([0-9]*\.?[0-9]+)[^a-zA-Z]*([kmbt]?)""").find(lowercase)

        if (match != null) {
            val (numberStr, suffix) = match.destructured
            val number = numberStr.toDoubleOrNull() ?: return 0L

            return when (suffix) {
                "k" -> (number * 1_000).toLong()
                "m" -> (number * 1_000_000).toLong()
                "b" -> (number * 1_000_000_000).toLong()
                "t" -> (number * 1_000_000_000_000).toLong()
                else -> number.toLong()
            }
        }

        return 0L
    }
}