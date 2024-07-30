package mrfast.sbt.features.auctionHouse

import com.google.gson.JsonObject
import com.mojang.realmsclient.gui.ChatFormatting
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.customevents.SocketMessageEvent
import mrfast.sbt.managers.PurseManager
import mrfast.sbt.utils.*
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.toFormattedTime
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse


/*
This is an auction flipper that scans the Hypixel Auction API based off of current lowest bins, and 3 day price averages.
There is no storing of pricing data on SBT's server end, thus making it vulnerable to multi-day market manipulations
 */

@SkyblockTweaks.EventComponent
object AuctionFlipper {
    var auctionsNotified = 0

    private var sentStartingText = false
    private var sentBestAuction = false
    private var lastBestAuctionKeybindState = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (AuctionHouseConfig.auctionFlipper) {
            if (!sentStartingText) {
                sentStartingText = true;
                val notificationText =
                    ChatComponentText("§eSB§9T§6 >> §aThe Auction Flipper is now active! §7Allow for a full minute for flips to appear.")

                Utils.setTimeout({
                    Utils.playSound("random.orb", 0.1)
                    ChatUtils.sendClientMessage("", false)
                    ChatUtils.sendClientMessage(notificationText, false)
                    ChatUtils.sendClientMessage("", false)
                }, 500)
            }

            try {
                val currentKeyState =
                    Mouse.isButtonDown(AuctionHouseConfig.autoAuctionFlipOpenKeybind!!) || Keyboard.isKeyDown(
                        AuctionHouseConfig.autoAuctionFlipOpenKeybind!!
                    )
                if (Utils.mc.currentScreen == null) {
                    if (currentKeyState) {
                        if (sentAuctionFlips.isNotEmpty() && !sentBestAuction) {
                            sentBestAuction = true
                            val bestAuction = sentAuctionFlips[0]
                            ChatUtils.sendPlayerMessage("/viewauction " + bestAuction.auctionUUID)
                            sentAuctionFlips.remove(bestAuction)

                            Utils.setTimeout({ sentBestAuction = false }, 1000)
                        }
                    }
                    if (lastBestAuctionKeybindState != currentKeyState && currentKeyState) {
                        ChatUtils.sendClientMessage(ChatFormatting.RED.toString() + "Best flip not found! Keep holding to open next.")
                    }
                    lastBestAuctionKeybindState = currentKeyState
                }
            } catch (_: Exception) {
            }
        } else {
            sentStartingText = false;
        }
    }

    class AuctionFlip(
        var bin: Boolean? = null,
        var endTime: Long? = null,
        var auctioneer: String? = null,
        var auctionUUID: String? = null
    ) {
        var price: Long = 0
        var profit: Long? = null
        var sellFor: Long? = null
        var bidderUUID: String? = null
        var volume: Int? = null
        var itemID: String? = null
        var itemStack: ItemStack? = null

        override fun toString(): String {
            return "AuctionFlip(" +
                    "bin=$bin, " +
                    "endTime=$endTime, " +
                    "auctioneer=$auctioneer, " +
                    "auctionUUID=$auctionUUID, " +
                    "price=$price, " +
                    "profit=$profit, " +
                    "volume=$volume, " +
                    "itemID=$itemID)"
        }

        fun sendToChat() {
            val auctionType = if (bin == true) "§7BIN" else "§7AUC"
            var timeRemaining = ""
            if (endTime != null && bin == false) {
                timeRemaining = (endTime!! - System.currentTimeMillis()).toFormattedTime()
            }
            // Example: buying item for 20m, selling for 35m
            val profitPercent = (((sellFor!!.toFloat() / price.toFloat()) - 1) * 100).toInt()

            val notification =
                "§eSB§9T§6 >> $auctionType ${itemStack?.displayName} §a${price.abbreviateNumber()}§3➜§a${(sellFor!!).abbreviateNumber()} §2(+${profit!!.abbreviateNumber()} §4$profitPercent%§2) §e${timeRemaining}"

            val chatComponent = ChatComponentText(notification)
            chatComponent.chatStyle.chatClickEvent =
                ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction $auctionUUID")
            chatComponent.chatStyle.chatHoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§a/viewauction $auctionUUID"))

            if (AuctionHouseConfig.AF_notificationSound && !cooldown) {
                cooldown = true
                Utils.playSound("note.pling", 0.5)
                // Use cooldown to prevent sounds from multiplying and breaking your headphones
                Utils.setTimeout({
                    cooldown = false
                }, 150)
            }
            ChatUtils.sendClientMessage(chatComponent, prefix = false)
            auctionsNotified++
        }
    }

    var cooldown = false

    // Wait for notification from webserver socket that auction house api updated
    @SubscribeEvent
    fun onSocketMessage(event: SocketMessageEvent) {
        if (!AuctionHouseConfig.auctionFlipper || Utils.mc.theWorld == null || !LocationUtils.inSkyblock) return

        if (event.type == "event" && event.message == "AuctionUpdate") {
            scanAuctionHouse()
        }
    }

    var maxPagesToScan = 30

    // Scans each page of auction house for potential flips
    // Each Page = 1000 Auctions
    private fun scanAuctionHouse() {
        auctionsNotified = 0
        checkedAuctions = 0
        sentAuctionFlips.clear()

        ChatUtils.sendClientMessage("", false)
        ChatUtils.sendClientMessage("§eSB§9T§6 >> §7Starting Auction House Scan..")
        ChatUtils.sendClientMessage("", false)

        runBlocking {
            for (page in 0..maxPagesToScan) {
                launch(Dispatchers.IO) {
                    val auctionHousePageJson = NetworkUtils.apiRequestAndParse(
                        "https://api.hypixel.net/skyblock/auctions?page=${page}",
                        useProxy = false,
                        caching = false
                    )
                    if (auctionHousePageJson.get("success").asBoolean) {
                        handleAuctionPage(auctionHousePageJson)
                    }
                }
            }

            delay(5000)
            ChatUtils.sendClientMessage("", false)
            ChatUtils.sendClientMessage("§eSB§9T§6 >> §7Scanned §9${checkedAuctions.formatNumber()}§7 auctions! §3${auctionsNotified.formatNumber()}§7 matched your filter.")
            ChatUtils.sendClientMessage("", false)
        }
    }

    //
    private fun handleAuctionPage(page: JsonObject) {
        val auctions = page.getAsJsonArray("auctions")
        for (auction in auctions) {
            handleAuction(auction as JsonObject)
        }
    }

    var checkedAuctions = 0
    private fun handleAuction(auction: JsonObject) {
        checkedAuctions++

        val bin = auction.get("bin").asBoolean
        val endTime = auction.get("end").asLong
        val auctioneer = auction.get("auctioneer").asString
        val auctionUUID = auction.get("uuid").asString
        val auctionFlip = AuctionFlip(bin, endTime, auctioneer, auctionUUID)

        if (auction.get("bids").asJsonArray.size() > 0) {
            auctionFlip.bidderUUID = auction.get("bids").asJsonArray.last().asJsonObject.get("bidder").asString
        }

        // Get Auction Price
        auctionFlip.price = auction.get("starting_bid").asLong
        // Use highest bid if its an auction
        if (auctionFlip.bin == false && auction.get("highest_bid_amount").asLong != 0L) {
            auctionFlip.price = auction.get("highest_bid_amount").asLong
        }

        val itemBytes = auction.get("item_bytes").asString
        val itemStack = ItemUtils.decodeBase64Item(itemBytes) ?: return

        val itemID = itemStack.getSkyblockId() ?: return

        auctionFlip.itemID = itemID
        auctionFlip.itemStack = itemStack

        val pricingData = ItemApi.getItemPriceInfo(itemID)

        if (pricingData != null) {
            calcAuctionProfit(auctionFlip, pricingData)
        }
    }

    // Calculate how much profit an auction will yeild
    // Will find a 'base price' for the item, found by using average bin ideally, with the lowest bin as a backup
    private fun calcAuctionProfit(auctionFlip: AuctionFlip, pricingData: JsonObject) {
        val suggestedListingPrice = ItemUtils.getSuggestListingPrice(auctionFlip.itemStack!!)!!

        if (auctionFlip.price < suggestedListingPrice) {
            // Already take out 8% accounting for your upcoming bid
            val nextBidPrice = (auctionFlip.price * 1.08).toLong()
            auctionFlip.profit = suggestedListingPrice - nextBidPrice
            if (pricingData.has("sales_avg")) {
                auctionFlip.volume = pricingData.get("sales_avg").asInt
            }

            auctionFlip.sellFor = suggestedListingPrice.toLong()

            filterOutAuction(auctionFlip)
        }
    }

    val sentAuctionFlips = mutableListOf<AuctionFlip>()

    // Filters for various things
    // Example options: No Runes, No Pets, No Furniture, no pet skins, no armor skins
    private fun filterOutAuction(auctionFlip: AuctionFlip) {
        // Filter out auctions that your already the top bid on
        if (auctionFlip.bidderUUID?.equals(Utils.mc.thePlayer.uniqueID.toString().replace("-", "")) == true) {
            return
        }
        // Filter out runes
        if (AuctionHouseConfig.AF_runeFilter && auctionFlip.itemID?.contains("_RUNE") == true) {
            return
        }
        // Filter out furniture items
        if (AuctionHouseConfig.AF_furnitureFilter && auctionFlip.itemStack?.getLore().toString()
                .contains("Furniture")
        ) {
            return
        }
        // Filter out decoration items
        if (AuctionHouseConfig.AF_furnitureFilter && auctionFlip.itemStack?.getLore().toString()
                .contains("Decoration")
        ) {
            return
        }
        // Filter out pets
        if (AuctionHouseConfig.AF_petFilter && auctionFlip.itemStack?.displayName?.clean()
                ?.startsWith("[Lvl") == true
        ) {
            return
        }
        // Filter out Dyes
        if (AuctionHouseConfig.AF_dyeFilter && auctionFlip.itemStack?.displayName?.clean()?.contains("Dye") == true) {
            return
        }
        // Filter out Pet Skins & Armor Skins
        if (AuctionHouseConfig.AF_skinFilter && (auctionFlip.itemID?.contains("SKIN") == true || auctionFlip.itemStack!!.displayName.clean()
                .contains("Skin"))
        ) {
            return
        }
        // Block BIN auctions if enabled
        if (/*!AuctionHouseConfig.AF_binFlips &&*/ auctionFlip.bin == true) {
            return
        }
        // Block auctions if enabled
//        if (!AuctionHouseConfig.AF_AucFlips && auctionFlip.bin == false) {
//            return
//        }

        /*
        Coin filters: Purse limit, price, profit, percentage
         */
        // Filter out auctions that are going to take too long to finish, Example: Auctions over an hour or already ended
        if (auctionFlip.endTime != null) {
            val msTillEnd = auctionFlip.endTime!! - System.currentTimeMillis();
            if (auctionFlip.bin == false && msTillEnd > AuctionHouseConfig.AF_minimumTime * 1000 * 60 || msTillEnd < 0) {
                return
            }
        }

        // Filter out auctions that don't make it past the profit requirement
        if (auctionFlip.profit != null) {
            if (auctionFlip.profit!! < AuctionHouseConfig.AF_profitMargin) {
                return
            }
        }
        // Filter out auctions that dont meet volume requirement
        if (auctionFlip.volume != null) {
            if (auctionFlip.volume!! < AuctionHouseConfig.AF_minimumVolume) {
                return
            }
        }
        // Filter out based on if the percentage of profit, example buying for 100k and selling for 300k is 200% profit
        if (auctionFlip.sellFor != null) {
            if (auctionFlip.sellFor!!.toFloat() / auctionFlip.price.toFloat() <= AuctionHouseConfig.AF_minimumPercent / 100) {
                return
            }
        }
        // Filter out auctions if it's too expensive
        if (AuctionHouseConfig.AF_usePurseLimit && auctionFlip.price > PurseManager.coinsInPurse) {
            return
        }
        // Filter out notifications that go past the limit
        if (auctionsNotified > AuctionHouseConfig.AF_maxNotifications) {
            return
        }

        sentAuctionFlips.add(auctionFlip)
        sentAuctionFlips.sortBy { auctionFlip.profit }

        auctionFlip.sendToChat()
    }
}