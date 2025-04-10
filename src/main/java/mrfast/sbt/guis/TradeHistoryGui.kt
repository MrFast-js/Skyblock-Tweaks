package mrfast.sbt.guis

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.elementa.ElementaVersion
import gg.essential.elementa.UIComponent
import gg.essential.elementa.WindowScreen
import gg.essential.elementa.components.ScrollComponent
import gg.essential.elementa.components.UIBlock
import gg.essential.elementa.components.UIRoundedRectangle
import mrfast.sbt.guis.components.CustomUIText
import gg.essential.elementa.components.input.UITextInput
import gg.essential.elementa.components.inspector.Inspector
import gg.essential.elementa.constraints.CenterConstraint
import gg.essential.elementa.constraints.MinConstraint
import gg.essential.elementa.constraints.PixelConstraint
import gg.essential.elementa.dsl.*
import gg.essential.elementa.effects.ScissorEffect
import gg.essential.elementa.state.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.guis.components.ItemComponent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.guis.components.SiblingConstraintFixed
import mrfast.sbt.managers.TradeManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
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
    var body: ScrollComponent? = null

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

        CustomUIText("§7Trade Log History", true).constrain {
            x = CenterConstraint()
            y = 6.pixels
            textScale = 2.pixels
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

            val date = CustomUIText("§7${it.key}").constrain {
                x = CenterConstraint()
                y = 0.pixels
                textScale = 1.4.pixels
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
        CustomUIText("§e$time").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.pixels
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

        CustomUIText("You").constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.pixels
        } childOf youBlock

        val yourItems = trade.getAsJsonArray("yourItems")
        for ((index, jsonElement) in yourItems.withIndex()) {
            val newX = index % 4
            val newY = index / 4
            val item = ItemStack(
                Item.getByNameOrId(jsonElement.asJsonObject.get("id").asString),
                jsonElement.asJsonObject.get("count").asInt
            )
            val nbt = jsonElement.asJsonObject.get("nbt").asString
            item.tagCompound = JsonToNBT.getTagFromJson(nbt)
            item.itemDamage = jsonElement.asJsonObject.get("damage").asInt

            val itemIcon = ItemComponent(item, 20f).constrain {
                x = (newX * 22).pixels + 8.pixels
                y = (newY * 22).pixels + 22.pixels
                height = 20.pixels
                width = 20.pixels
            } childOf leftBlock

            val lore = item.getLore()
            lore.add(0, item.displayName)

            itemIcon.addTooltip(lore.toSet(), item)
        }

        CustomUIText("§6${trade.get("yourCoins").asLong.abbreviateNumber()} Coins").constrain {
            x = CenterConstraint()
            y = 100.percent - 12.pixels
            textScale = 1.pixels
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

        CustomUIText(traderName).constrain {
            x = CenterConstraint()
            y = CenterConstraint()
            textScale = 1.pixels
        } childOf usernameBlock

        val theirItems = trade.getAsJsonArray("theirItems")
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

            val itemIcon = ItemComponent(item, 20f).constrain {
                x = (newX * 22).pixels + 8.pixels
                y = (newY * 22).pixels + 22.pixels
                height = 20.pixels
                width = 20.pixels
            } childOf rightBlock

            val lore = item.getLore()
            lore.add(0, item.displayName)

            itemIcon.addTooltip(lore.toSet(), item)
        }

        CustomUIText("§6${trade.get("theirCoins").asLong.abbreviateNumber()} Coins").constrain {
            x = CenterConstraint()
            y = 100.percent - 12.pixels
            textScale = 1.pixels
        } childOf rightBlock
    }
}