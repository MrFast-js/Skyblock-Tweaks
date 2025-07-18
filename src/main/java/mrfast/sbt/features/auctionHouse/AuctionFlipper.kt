package mrfast.sbt.features.auctionHouse

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Semaphore
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.customevents.SocketMessageEvent
import mrfast.sbt.guis.GuiItemFilterPopup.*
import mrfast.sbt.managers.*
import mrfast.sbt.utils.*
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.toFormattedDuration
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
    private var auctionsNotified = 0
    private var sentStartingText = false
    private var sentBestAuction = false
    private var lastBestAuctionKeybindState = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return

        if (TickManager.tickCount % 10 != 0) return

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
                if (Utils.getCurrentScreen() == null) {
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
                        ChatUtils.sendClientMessage("§cBest flip not found! Keep holding to open next.")
                    }
                    lastBestAuctionKeybindState = currentKeyState
                }
            } catch (_: Exception) {
            }
        } else {
            sentStartingText = false;
        }
    }

    private class AuctionFlip(
        var bin: Boolean? = null,
        var endTime: Long? = null,
        var auctioneer: String? = null,
        var auctionUUID: String? = null
    ) {
        var price: Long = 0
        var profit: Long? = null
        var listingFee: Long = 0
        var suggestedListingPrice: Long? = null
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
                timeRemaining = (endTime!! - System.currentTimeMillis()).toFormattedDuration()
            }
            // Example: buying item for 20m, selling for 35m
            val profitPercent = (((suggestedListingPrice!!.toFloat() / price.toFloat()) - 1) * 100).toInt()

            profit = suggestedListingPrice!! - price - listingFee

            val notification =
                "§eSB§9T§6 >> $auctionType ${itemStack?.displayName} §a${price.abbreviateNumber()}§3➜§a${(suggestedListingPrice!!).abbreviateNumber()} §2(+${profit!!.abbreviateNumber()} §4$profitPercent%§2) §e${timeRemaining}"

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

    private var cooldown = false

    // Wait for notification from webserver socket that auction house api updated
    @SubscribeEvent
    fun onSocketMessage(event: SocketMessageEvent) {
        if (!AuctionHouseConfig.auctionFlipper || !Utils.isWorldLoaded() || !LocationManager.inSkyblock) return

        if (event.type == "event" && event.message == "AuctionUpdate") {
            if(CustomizationConfig.developerMode) {
                ChatUtils.sendClientMessage("event: ${event.type} message: ${event.message}")
            }
            scanAuctionHouse()
        }
    }

    private var maxPagesToScan = 60

    // Scans each page of auction house for potential flips
    // Each Page = 1000 Auctions
    private fun scanAuctionHouse() {
        auctionsNotified = 0
        checkedAuctions = 0
        sentAuctionFlips.clear()
        filterDebugCounts.clear()
        ChatUtils.sendClientMessage("", false)
        ChatUtils.sendClientMessage("§eSB§9T§6 >> §7Starting Auction House Scan..")
        ChatUtils.sendClientMessage("", false)

        runBlocking {
            val semaphore = Semaphore(3) // Max 5 pages concurrently

            val batchSize = 3 // Adjust based on your system's capacity
            for (i in 0 until maxPagesToScan step batchSize) {
                val currentBatch = (i until minOf(i + batchSize, maxPagesToScan)).map { page ->
                    launch(Dispatchers.IO) {
                        semaphore.acquire()
                        try {
                            val auctionHousePageJson = NetworkUtils.apiRequestAndParse(
                                "https://api.hypixel.net/skyblock/auctions?page=${page}",
                                useProxy = false,
                                caching = false
                            )
                            if (auctionHousePageJson.get("success")?.asBoolean == true) {
                                handleAuctionPage(auctionHousePageJson)
                            }
                            if(auctionHousePageJson.get("totalPages") != null) {
                                maxPagesToScan = auctionHousePageJson.get("totalPages").asInt
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            semaphore.release()
                        }
                    }
                }

                currentBatch.joinAll() // Wait for this batch to finish before continuing

                // Optional: Add delay after processing each batch
                delay(500)  // Delay to reduce pressure
            }

            ChatUtils.sendClientMessage("", false)
            val text =
                ChatComponentText("§eSB§9T§6 >> §7Scanned §9${checkedAuctions.formatNumber()}§7 auctions! §3${auctionsNotified.formatNumber()}§7 matched your filter. (hover)")
            text.chatStyle.chatHoverEvent =
                HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText(getFilterSummary()))
            ChatUtils.sendClientMessage(text)
            ChatUtils.sendClientMessage("", false)
        }
    }

    private fun handleAuctionPage(page: JsonObject) {
        val auctions = page.getAsJsonArray("auctions")
        for (auction in auctions) {
            handleAuction(auction as JsonObject)
        }
    }

    private var checkedAuctions = 0
    private var itemStackCache = mutableMapOf<String, ItemStack>()
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

            // Set price to the next bid price
            auctionFlip.price = when {
                auctionFlip.price < 100_000L -> (auctionFlip.price * 1.15).toLong()
                auctionFlip.price < 1_000_000L -> (auctionFlip.price * 1.1).toLong()
                auctionFlip.price < 10_000_000L -> (auctionFlip.price * 1.05).toLong()
                auctionFlip.price < 100_000_000L -> (auctionFlip.price * 1.025).toLong()
                else -> auctionFlip.price
            }
        }

        val itemBytes = auction.get("item_bytes").asString
        val itemStack: ItemStack?

        if(itemStackCache.containsKey(itemBytes)) {
            itemStack = itemStackCache[itemBytes]
        } else {
            val stack = ItemUtils.decodeBase64Item(itemBytes)
            if (stack != null) {
                itemStack = stack
                itemStackCache[itemBytes] = stack
            } else {
                filterOutWithReason("Could Not Decode Item")
                return
            }
        }

        if (itemStack == null) {
            filterOutWithReason("Could Not Resolve Item")
            return
        }

        val itemID = itemStack.getSkyblockId()
        if (itemID == null) {
            filterOutWithReason("Could Not Find Item ID")
            return
        }

        auctionFlip.itemID = itemID
        auctionFlip.itemStack = itemStack

        val pricingData = ItemApi.getItemInfo(itemID)
        if (pricingData == null) {
            filterOutWithReason("No Item Pricing Data")
            return
        }

        calcAuctionProfit(auctionFlip, pricingData)
    }

    // Calculate how much profit an auction will yield
    // Will find a 'base price' for the item, found by using average bin ideally, with the lowest bin as a backup
    private fun calcAuctionProfit(auctionFlip: AuctionFlip, pricingData: JsonObject) {
        val suggestedListingPrice = ItemUtils.getSuggestListingPrice(auctionFlip.itemStack!!)!!
        val priceToSellFor = suggestedListingPrice.get("price").asLong

        auctionFlip.suggestedListingPrice = priceToSellFor

        // Subtract listing fee, 1% of profit if price < 10m, 2% if < 100m, else 2.5%
        auctionFlip.listingFee = when {
            priceToSellFor < 10_000_000L -> (priceToSellFor * 0.01).toLong()
            priceToSellFor < 100_000_000L -> (priceToSellFor * 0.02).toLong()
            else -> (priceToSellFor * 0.025).toLong()
        }

        auctionFlip.profit = priceToSellFor - auctionFlip.price - auctionFlip.listingFee

        if (pricingData.has("sold")) {
            auctionFlip.volume = pricingData.get("sold").asInt
        }

        if (auctionFlip.price < priceToSellFor) {
            filterAndSendFlip(auctionFlip)
        } else {
            filterOutWithReason("Price > Suggested Price")
        }
    }

    private val sentAuctionFlips = mutableListOf<AuctionFlip>()

    // Filters for various things
    // Example options: No Runes, No Pets, No Furniture, no pet skins, no armor skins
    var defaultFilterList = listOf(
        FilteredItem("BOUNCY_", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("HEAVY_", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("Aurora", FilterType.CONTAINS, InputType.DISPLAY_NAME),
        FilteredItem("_HOE", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("_RUNE", FilterType.CONTAINS, InputType.ITEM_ID),
        FilteredItem("Skin", FilterType.CONTAINS, InputType.DISPLAY_NAME)
    )
    var filters = defaultFilterList.toMutableList()

    init {
        val blacklistFilePath = ConfigManager.modDirectoryPath.resolve("data/itemBlacklist.json")

        if (blacklistFilePath.exists()) {
            val profileData = DataManager.loadDataFromFile(blacklistFilePath)
            val jsonFilters = profileData.getAsJsonArray("filters") ?: JsonArray()
            filters = Gson().fromJson(jsonFilters, Array<FilteredItem>::class.java).toMutableList()
        }
    }

    private val filterDebugCounts = mutableMapOf<String, Int>()

    private fun filterAndSendFlip(auctionFlip: AuctionFlip) {
        if(!Utils.isWorldLoaded()) return

        // Filter out auctions that your already the top bid on
        if (auctionFlip.bidderUUID?.equals(Utils.getPlayer()!!.uniqueID.toString().replace("-", "")) == true) {
            filterOutWithReason("Already top bidder")
            return
        }

        if (filters.isNotEmpty()) {
            for (filter in filters) {
                if (filter.matches(auctionFlip.itemStack!!)) {
                    filterOutWithReason("Matched A Blacklist Filter")
                    return
                }
            }
        }

        // Filter out farming tools
        if (AuctionHouseConfig.AF_farmingToolFilter) {
            if (auctionFlip.itemStack?.getSkyblockId()?.contains("_HOE") == true ||
                auctionFlip.itemStack?.getSkyblockId()?.equals("COCO_CHOPPER") == true ||
                auctionFlip.itemStack?.getSkyblockId()?.contains("_DICER") == true
            ) {
                filterOutWithReason("Farming tools Filter")
                return
            }
        }

        if (AuctionHouseConfig.AF_commonFilter) {
            ItemApi.getItemInfo(auctionFlip.itemID!!)?.let {
                if (it.has("rarity")) {
                    if(!it.get("rarity").isJsonNull) {
                        if(it.get("rarity").asString == "COMMON") {
                            filterOutWithReason("Common Item Filter")
                            return
                        }
                    }
                }
            }
        }

        // Filter out furniture items
        if (AuctionHouseConfig.AF_furnitureFilter && auctionFlip.itemStack?.getLore().toString()
                .contains("Furniture")
        ) {
            filterOutWithReason("Furniture Filter")
            return
        }

        // Filter out decoration items
        if (AuctionHouseConfig.AF_furnitureFilter && auctionFlip.itemStack?.getLore().toString()
                .contains("Decoration")
        ) {
            filterOutWithReason("Decoration Filter")
            return
        }

        // Filter out pets
        if (AuctionHouseConfig.AF_petFilter && auctionFlip.itemStack?.displayName?.clean()
                ?.startsWith("[Lvl") == true
        ) {
            filterOutWithReason("Pet Filter")
            return
        }

        // Filter out Dyes
        if (AuctionHouseConfig.AF_dyeFilter && auctionFlip.itemStack?.displayName?.clean()?.contains("Dye") == true) {
            filterOutWithReason("Dye Filter")
            return
        }

        // Block BIN auctions if enabled
        if (!AuctionHouseConfig.AF_binFlips && auctionFlip.bin == true) {
            filterOutWithReason("Is A BIN Auction")
            return
        }

        // Block regular auctions if enabled
        if (!AuctionHouseConfig.AF_AucFlips && auctionFlip.bin == false) {
            filterOutWithReason("Is A Regular Auction")
            return
        }

        // Filter based on auction duration
        if (auctionFlip.endTime != null) {
            val msTillEnd = auctionFlip.endTime!! - System.currentTimeMillis()
            if (auctionFlip.bin == false && msTillEnd > AuctionHouseConfig.AF_minimumTime * 1000 * 60 || msTillEnd < 0) {
                filterOutWithReason("Auction Duration Too Long")
                return
            }
        }

        // Filter based on profit margin
        if (auctionFlip.profit != null) {
            auctionFlip.profit = auctionFlip.suggestedListingPrice!! - auctionFlip.price - auctionFlip.listingFee
            if (auctionFlip.profit!! < AuctionHouseConfig.AF_profitMargin) {
                filterOutWithReason("Profit Margin Too Low")
                return
            }
        }

        // Filter based on profit percentage
        if (auctionFlip.suggestedListingPrice != null) {
            if (auctionFlip.suggestedListingPrice!!.toFloat() / auctionFlip.price.toFloat() <= AuctionHouseConfig.AF_minimumPercent / 100) {
                filterOutWithReason("Profit Percentage Too Low")
                return
            }
        }


        // Filter based on volume
        if (auctionFlip.volume != null) {
            if (auctionFlip.volume!! < AuctionHouseConfig.AF_minimumVolume) {
                filterOutWithReason("Volume Too Low")
                return
            }
        }

        // Filter based on purse limit
        if (AuctionHouseConfig.AF_usePurseLimit && auctionFlip.price > PurseManager.coinsInPurse) {
            filterOutWithReason("Price > Purse Limit")
            return
        }

        // Filter based on max notifications
        if (auctionsNotified > AuctionHouseConfig.AF_maxNotifications) {
            filterOutWithReason("Beyond Max Notifications")
            return
        }

        // Add the auction to the list and notify
        sentAuctionFlips.add(auctionFlip)
        sentAuctionFlips.sortedByDescending { it.profit }

        auctionFlip.sendToChat()
    }

    private fun filterOutWithReason(filterName: String) {
        filterDebugCounts[filterName] = filterDebugCounts.getOrDefault(filterName, 0) + 1
    }

    private fun getFilterSummary(): String {
        val out = mutableListOf("§b§lFilter Summary")
        filterDebugCounts.forEach { (filter, count) ->
            out.add("§6$filter: §c${count.formatNumber()} §7auctions removed")
        }
        return out.joinToString("\n")
    }
}