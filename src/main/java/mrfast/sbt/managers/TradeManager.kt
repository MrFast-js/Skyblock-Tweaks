package mrfast.sbt.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.inventory.Container
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

// @TODO add a 'hint' that after a trade prompts you once that you can view all past trades with /tradelog
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
        }
    }

    private var lastTradeMenu: Container? = null
    private var tradingWithSub = ""
    private var tradingWith = ""

    @SubscribeEvent
    fun onContainerDraw(event: SlotDrawnEvent.Post) {
        if (event.slot.slotNumber == 0) {
            inTradeMenu = false

            val tradeSlot = event.gui.inventorySlots?.getSlot(4) ?: return
            if (tradeSlot.stack?.displayName?.clean() != "⇦ Your stuff") return

            inTradeMenu = true
            lastTradeMenu = event.gui.inventorySlots
            tradingWithSub = event.gui.chestName().split("You")[1].trim()
        }
    }

    @SubscribeEvent
    fun onClientTick(event: ClientTickEvent) {
        if (TickManager.tickCount % 20 != 0 || Utils.mc.theWorld == null) return

        if (tradingWithSub.isNotEmpty()) {
            val matchingPlayer = Utils.mc.theWorld.playerEntities.find {
                it.name.contains(tradingWithSub)
            }
            if (matchingPlayer != null) {
                tradingWith = matchingPlayer.name
            }
        }
    }

    private val yourSlots = mutableListOf(
        0, 1, 2, 3,
        9, 10, 11, 12,
        18, 19, 20, 21,
        27, 28, 29, 30
    )

    private val theirSlots = mutableListOf(
        5, 6, 7, 8,
        14, 15, 16, 17,
        23, 24, 25, 26,
        32, 33, 34, 35
    )

    private fun interpretLastTradeMenu() {
        if (lastTradeMenu == null) return

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

        trade.addProperty("timestamp", System.currentTimeMillis())
        trade.addProperty("username", tradingWith)

        val date = Utils.getFormattedDate()
        if (!tradeHistory.has(date)) tradeHistory.add(date, JsonArray())

        tradeHistory[date].asJsonArray.add(trade)

        DataManager.saveProfileData("tradeHistory", tradeHistory)
        tradingWith = ""
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