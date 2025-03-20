package mrfast.sbt.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object TradeManager {
    var tradeHistory = JsonObject()
    private var inTradeMenu = false

    @SubscribeEvent
    fun onProfileSwap(event: ProfileLoadEvent) {
        tradeHistory = DataManager.getProfileDataDefault("tradeHistory", JsonObject()) as JsonObject
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.clean().startsWith("Trade completed with")) {
            interpretLastTradeMenu()
            inTradeMenu = false
        }
    }

    private var lastTradeMenu: Container? = null
    private var tradingWith = ""

    @SubscribeEvent
    fun onContainerDraw(event: GuiContainerBackgroundDrawnEvent) {
        val tradeSlot = event.gui?.inventorySlots?.getSlot(4) ?: return

        if (tradeSlot.stack?.displayName?.clean() != "⇦ Your stuff") return
        inTradeMenu = true
        lastTradeMenu = event.gui!!.inventorySlots
        tradingWith = event.gui!!.chestName().split("You")[1].trim()
    }

    private val yourSlots = mutableListOf(0, 1, 2, 3,
                                            9, 10, 11, 12,
                                            18, 19, 20, 21,
                                            27, 28, 29, 30)

    private val theirSlots = mutableListOf(5, 6, 7, 8,
                                            14, 15, 16, 17,
                                            23, 24, 25, 26,
                                            32, 33, 34, 35)

    private fun interpretLastTradeMenu() {
        ChatUtils.sendClientMessage("Trade completed with $tradingWith")

        if (lastTradeMenu == null) return

        ChatUtils.sendClientMessage("Step 1 ")

        val trade = JsonObject()

        val yourItems = JsonArray()
        var yourCoins = 0L
        yourSlots.forEach { slot ->
            val stack = lastTradeMenu!!.getSlot(slot).stack
            if (stack != null) {
                if (stack.displayName.clean().endsWith("coins")) {
                    val name = stack.displayName.clean()
                    val coins = name
                        .replace("k", "000")
                        .replace("M", "000000")
                        .replace("B", "000000000")
                        .replace(" coins", "")
                        .trim()
                        .toLong()
                    yourCoins += coins
                } else {
                    val json = createItemJson(stack)
                    yourItems.add(json)
                }
            }
        }
        trade.add("yourItems", yourItems)
        trade.addProperty("yourCoins", yourCoins)

        ChatUtils.sendClientMessage("Step 2 "+yourCoins)

        val theirItems = JsonArray()
        var theirCoins = 0L
        theirSlots.forEach { slot ->
            val stack = lastTradeMenu!!.getSlot(slot).stack
            if (stack != null) {
                if (stack.displayName.clean().endsWith("coins")) {
                    val name = stack.displayName.clean()
                    val coins = name
                        .replace("k", "000")
                        .replace("M", "000000")
                        .replace("B", "000000000")
                        .replace(" coins", "")
                        .trim()
                        .toLong()
                    theirCoins += coins
                } else {
                    val json = createItemJson(stack)
                    theirItems.add(json)
                }
            }
        }
        trade.add("theirItems", theirItems)
        trade.addProperty("theirCoins", theirCoins)

        ChatUtils.sendClientMessage("Step 3 "+theirCoins)

        trade.addProperty("timestamp", System.currentTimeMillis())
        trade.addProperty("username", tradingWith)

        ChatUtils.sendClientMessage("Step 4 "+ tradingWith)

        val date = Utils.getFormattedDate()
        if (!tradeHistory.has(date)) tradeHistory.add(date, JsonArray())

        ChatUtils.sendClientMessage("Step 5 "+ tradeHistory.entrySet().size)
        tradeHistory[date].asJsonArray.add(trade)
        ChatUtils.sendClientMessage("Step 6 "+ tradeHistory.entrySet().size)

        DataManager.saveProfileData("tradeHistory", tradeHistory)
    }


    private fun createItemJson(stack: ItemStack): JsonObject {
        val json = JsonObject()
        json.addProperty("count", stack.stackSize)
        json.addProperty("damage", stack.itemDamage)
        json.addProperty("nbt", stack.tagCompound.toString())
        json.addProperty("id", stack.item.registryName)

        return json
    }
}