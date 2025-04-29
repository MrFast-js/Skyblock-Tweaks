package mrfast.sbt.features.auctionHouse

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.SignDrawnEvent
import mrfast.sbt.guis.GuiItemFilterPopup
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.DataManager
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.Element
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getItemUUID
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.cleanRanks
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.getInventory
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.getStringWidth
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.max

@SkyblockTweaks.EventComponent
object AuctionMenuOverlays {
    private var biddedAuctions = listOf<Auction>()
    private var ownedAuctions = listOf<Auction>()
    private var lastViewedAuction: Auction? = null
    private var sellingAuction: Auction? = null

    open class Auction(var slot: Slot, var uuid: String) {
        var price = 0L
        var profit = 0L
        var suggestedListingPrice = 0L
        var winning: Boolean? = null
        var shouldSellBIN = true
        var seller: String? = null
        var otherBidder: String? = null
        var pricingData: JsonObject? = null
        var stack: ItemStack? = null
        var ended = false
    }

    @SubscribeEvent
    fun onContainerDrawn(event: GuiContainerBackgroundDrawnEvent) {
        if (event.gui !is GuiChest) return
        val containerName = (event.gui as GuiContainer).chestName()

        if (containerName == "Your Bids") {
            val newBiddedAuctions = mutableListOf<Auction>()

            val inventory = (event.gui as GuiChest).getInventory()
            for (i in 0..inventory.sizeInventory) {
                val stack = inventory.getStackInSlot(i) ?: continue
                val uuid = stack.getItemUUID() ?: (stack.displayName + i)
                val slot = (event.gui as GuiChest).inventorySlots.getSlot(i)
                val auction = Auction(slot, uuid)
                auction.stack = stack

                setAuctionInfoFromLore(auction)

                if (auction.winning == null) continue

                auction.stack = stack

                // Highlight losing auctions
                if (AuctionHouseConfig.highlightLosingAuctions && auction.winning == false) {
                    GuiUtils.highlightSlot(slot, AuctionHouseConfig.highlightLosingAuctionsColor.colorState.get())
                }

                setAuctionPricingData(auction)

                auction.profit = auction.suggestedListingPrice - auction.price

                newBiddedAuctions.add(auction)
            }
            biddedAuctions = newBiddedAuctions
        }

        if (containerName.contains("Auction View")) {
            val inventory = (event.gui as GuiChest).getInventory()
            val stack = inventory.getStackInSlot(13) ?: return
            val uuid = stack.getItemUUID() ?: (stack.displayName + 13)
            val slot = (event.gui as GuiChest).inventorySlots.getSlot(13)
            val auction = Auction(slot, uuid)
            auction.stack = stack

            setAuctionInfoFromLore(auction)
            setAuctionPricingData(auction)

            // Increase price based off of next bid
            val bidHistoryItem = inventory.getStackInSlot(33)
            if (bidHistoryItem != null) {
                val pastBids = mutableListOf<String>()
                val BIDDER_REGEX = """By: (.*)""".toRegex()
                for (line in bidHistoryItem.getLore()) {
                    if (line.clean().matches(BIDDER_REGEX)) {
                        pastBids.add(line.clean().getRegexGroups(BIDDER_REGEX)!![1]!!.value.cleanRanks())
                    }
                }
                if (pastBids.size > 0) {
                    auction.otherBidder = pastBids[0]
                    if (pastBids.size > 1 && auction.otherBidder == Utils.mc.thePlayer.name) {
                        auction.otherBidder = pastBids[1]
                    }
                }
            }

            if (auction.winning == false) {
                val submitBidStack = inventory.getStackInSlot(29) ?: return
                var newBidPrice = -1L
                val NEW_BID_REGEX = """New bid: ([\d,]+) coins""".toRegex()

                for (line in submitBidStack.getLore(clean = true)) {
                    if (line.matches(NEW_BID_REGEX)) {
                        newBidPrice = line.getRegexGroups(NEW_BID_REGEX)!![1]!!.value.replace(",", "").toLong()
                        break
                    }
                }
                // Only update price if a new bid is found
                if (newBidPrice != -1L) auction.price = newBidPrice
            }

            auction.profit = auction.suggestedListingPrice - auction.price

            lastViewedAuction = auction
        }

        if (containerName == "Create BIN Auction" || containerName == "Create Auction") {
            val inventory = (event.gui as GuiChest).getInventory()
            val stack = inventory.getStackInSlot(13) ?: return
            val uuid = stack.getItemUUID() ?: (stack.displayName + 13)
            val slot = (event.gui as GuiChest).inventorySlots.getSlot(13)
            val auction = Auction(slot, uuid)
            auction.stack = stack

            setAuctionPricingData(auction)

            sellingAuction = auction
        }


        if (containerName == "Manage Auctions") {
            val newOwnedAuctions = mutableListOf<Auction>()

            val inventory = (event.gui as GuiChest).getInventory()
            for (i in 0..inventory.sizeInventory) {
                val stack = inventory.getStackInSlot(i) ?: continue
                val uuid = stack.getItemUUID() ?: (stack.displayName + i)
                val slot = (event.gui as GuiChest).inventorySlots.getSlot(i)

                val auction = Auction(slot, uuid)
                newOwnedAuctions.add(auction)
            }

            ownedAuctions = newOwnedAuctions
        }
    }

    private fun setAuctionPricingData(auction: Auction) {
        val id = auction.stack?.getSkyblockId() ?: return
        auction.pricingData = ItemApi.getItemInfo(id) ?: return

        val stack = auction.stack ?: return
        val suggestedListingPrice = ItemUtils.getSuggestListingPrice(stack) ?: return

        auction.suggestedListingPrice = suggestedListingPrice.get("price").asLong
        auction.shouldSellBIN = suggestedListingPrice.get("bin")?.asBoolean == true
        auction.profit = auction.suggestedListingPrice - auction.price
    }

    private fun setAuctionInfoFromLore(auction: Auction) {
        for (line in auction.stack!!.getLore()) {
            if (line.clean().matches("""Bidder: (.*)""".toRegex())) {
                auction.winning = line.clean().contains(Utils.mc.thePlayer.name)
            }
            if (line.clean().matches("""Status: Ended!""".toRegex())) {
                auction.ended = true
            }
            if (line.clean().matches("""(?:Starting bid|Top bid|Buy it now): (.*) coins""".toRegex())) {
                auction.price = line.clean().replace("\\D+".toRegex(), "").toLong()
            }
            if (line.clean().matches("""Seller: (.*)""".toRegex())) {
                auction.seller = line.clean().getRegexGroups("""Seller: (.*)""".toRegex())!![1]!!.value.cleanRanks()
            }
        }
    }


    init {
        BidsOverlay()
        AuctionViewOverlay()
        AuctionPriceOverlay()
    }

    class BidsOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val winningAuctions = biddedAuctions.filter { it.winning == true }.size
            val endedAuctions = biddedAuctions.filter { it.ended }.size

            var totalFlipPotential = 0L;
            val flipList = mutableListOf("§e§lTotal Flip Profit", "§3===============")

            for (biddedAuction in biddedAuctions) {
                if (biddedAuction.winning == true) {
                    totalFlipPotential += biddedAuction.profit
                    val colorSymbol = if (biddedAuction.profit > 0) "§a+" else "§c"
                    flipList.add(biddedAuction.stack!!.displayName)
                    flipList.add(" §8${biddedAuction.price.abbreviateNumber()}§3➜§7${biddedAuction.suggestedListingPrice.abbreviateNumber()} ${colorSymbol}${biddedAuction.profit.abbreviateNumber()}")
                }
            }

            val lines = mutableListOf(
                Element(
                    5f, 5f, "§a${winningAuctions}§r Winning Auctions", null, null
                ),
                Element(
                    5f, 15f, "§c${biddedAuctions.size - winningAuctions}§r Losing Auctions", null, null
                ),
                Element(
                    5f, 30f, "§rEnded Auctions: §6${endedAuctions}", null, null
                ),
                Element(
                    7f,
                    43f,
                    "§rFlip Profit: §6${totalFlipPotential.formatNumber()}",
                    flipList,
                    null,
                    drawBackground = true
                ),
            )

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 150
                )

            val chestEvent = (event as GuiContainerBackgroundDrawnEvent)
            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                GuiUtils. getLowestY(lines),
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, (x + chestEvent.guiLeft + 180).toInt(), (y + chestEvent.guiTop).toInt())
            }
            GlStateManager.translate(0f, 0f, -52f)
        }

        override fun isActive(event: Event): Boolean {
            if (event !is GuiContainerBackgroundDrawnEvent || event.gui == null) return false

            return (event.gui as GuiContainer).chestName() == "Your Bids" && AuctionHouseConfig.biddingOverlay
        }
    }

    class AuctionViewOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            if (event !is GuiContainerBackgroundDrawnEvent) return

            val viewedItem = (event.gui as GuiChest).getInventory().getStackInSlot(13) ?: return
            val itemId = viewedItem.getSkyblockId() ?: return
            val pricingData = ItemApi.getItemInfo(itemId) ?: return
            val resellProfit = lastViewedAuction?.profit ?: return
            val coloredSymbol = if (resellProfit > 0) "§a+" else "§c"

            // Pricing and count for Live Auction listings and their prices
            val activeAucNum = if (pricingData.has("activeAuc")) pricingData.get("activeAuc").asInt else -1
            val activeAuc = if (activeAucNum != -1) activeAucNum.formatNumber() else "§cUnknown"
            val activeAucPrices =
                if (pricingData.has("activeAucPrices")) pricingData.get("activeAucPrices").asJsonArray else JsonArray()

            // Pricing and count for Live Bin Listings and their prices
            val activeBinNum = if (pricingData.has("activeBin")) pricingData.get("activeBin").asInt else -1
            val activeBin = if (activeBinNum != -1) activeBinNum.formatNumber() else "§cUnknown"
            val activeBinPrices =
                if (pricingData.has("activeBinPrices")) pricingData.get("activeBinPrices").asJsonArray else JsonArray()

            // Pricing and count for recently ended auction listings and their prices
            val soldAucNum = if (pricingData.has("aucSold")) pricingData.get("aucSold").asInt else -1
            val soldAuc = if (soldAucNum != -1) soldAucNum.formatNumber() else "§cUnknown"
            val soldAucPrices =
                if (pricingData.has("aucSoldPrices")) pricingData.get("aucSoldPrices").asJsonArray else JsonArray()

            // Pricing and count for recently ended Bin Listings and their prices
            val soldBinNum = if (pricingData.has("binSold")) pricingData.get("binSold").asInt else -1
            val soldBin = if (soldBinNum != -1) soldBinNum.formatNumber() else "§cUnknown"
            val soldBinPrices =
                if (pricingData.has("binSoldPrices")) pricingData.get("binSoldPrices").asJsonArray else JsonArray()

            val recentlySoldHover = mutableListOf(
                "§b§lSales / Day",
                "§9$soldAuc Sold Auctions"
            ) + getPrices(soldAucPrices) + listOf("§3$soldBin Sold BINs") + getPrices(soldBinPrices)

            val activeListingsHover = mutableListOf(
                "§b§lActive Listings",
                "§9$activeAuc Auctions"
            ) + getPrices(activeAucPrices) + listOf("§3$activeBin BINs") + getPrices(activeBinPrices)

            val flipPotential = mutableListOf(
                "§e§lFlip Potential",
                "§7Buy for §a${(lastViewedAuction!!.price).formatNumber()}",
                "§7Sell for §d${(lastViewedAuction!!.suggestedListingPrice).formatNumber()}"
            )
            if (lastViewedAuction?.shouldSellBIN == false) flipPotential.add("§c§lSELL AS AUCTION")

            val lines = mutableListOf(
                Element(
                    5f,
                    5f,
                    "§rItem Price: §d${lastViewedAuction!!.price.formatNumber()}",
                    null,
                    null
                ),
                Element(
                    5f,
                    18f,
                    "§rLowest BIN: §6${if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asLong.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    5f,
                    28f,
                    "§rAverage BIN: §3${if (pricingData.has("avgLowestBin")) pricingData.get("avgLowestBin").asLong.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    5f,
                    41f,
                    "§b§l${max(activeAucNum + activeBinNum, 0).formatNumber()} Active Listings",
                    activeListingsHover,
                    null
                ),
                Element(
                    5f,
                    51f,
                    "§b§l${max(soldAucNum + soldBinNum, 0).formatNumber()} Sales / Day",
                    recentlySoldHover,
                    null
                ),
                Element(
                    5f,
                    64f,
                    "§rResell Profit: ${coloredSymbol}${resellProfit.formatNumber()}",
                    flipPotential,
                    null
                )
            )

            val id = lastViewedAuction?.stack?.getSkyblockId() ?: return
            val rarity = ItemApi.getItemInfo(id)?.get("rarity")?.let {
                if (it.isJsonNull) "UNKNOWN" else it.asString
            } ?: "UNKNOWN"

            if (rarity == "COMMON") {
                lines.add(
                    Element(
                        5f,
                        GuiUtils.getLowestY(lines) + 2f,
                        "§c§lONLY 5x NPC VALUE!",
                        listOf(
                            "§7This item is a common rarity item",
                            "§7and can only be sold for 5x NPC price.",
                            "§7You can still make profit by flipping it",
                            "§7on Auction but it will be harder."
                        ),
                        null
                    )
                )
            }

            val iconY = GuiUtils.getLowestY(lines) + 4f
            var icons = 0

            // Add special button to party the other bidder if they are being annoying
            if (lastViewedAuction!!.otherBidder != null && lastViewedAuction!!.otherBidder != Utils.mc.thePlayer.name) {
                lines.add(
                    GuiUtils.IconElement(
                        "§9✚",
                        7f + icons*20f,
                        iconY,
                        listOf(
                            "§9Party Bidder",
                            "§e/party ${lastViewedAuction!!.otherBidder}",
                            "§7Allows for a chance to negotiate",
                            "§7if a player wont stop bidding"
                        ),
                        {
                            Utils.mc.thePlayer.closeScreen()
                            ChatUtils.sendPlayerMessage("/party ${lastViewedAuction!!.otherBidder}")
                        },
                        backgroundColor = Color(85, 85, 255),
                        width = 10
                    )
                )
                icons++
            }

            lines.add(
                GuiUtils.IconElement(
                    "§6⧫",
                    7f + icons*20f,
                    iconY,
                    listOf(
                        "§6Sellers AH",
                        "§e/ah ${lastViewedAuction!!.seller}",
                        "§7Check if more profitable auctions",
                        "§7are being sold by the same seller"
                    ),
                    {
                        Utils.mc.thePlayer.closeScreen()
                        ChatUtils.sendPlayerMessage("/ah ${lastViewedAuction!!.seller}")
                    },
                    backgroundColor = Color(255, 170, 0),
                    width = 10
                )
            )
            icons++


            lines.add(
                GuiUtils.IconElement(
                    "§c⚠",
                    7f + icons*20f,
                    iconY,
                    listOf(
                        "§cBlacklist Item",
                        "§7Blacklists ${lastViewedAuction!!.stack?.getSkyblockId()} from",
                        "§7appearing in Auction Flipper."
                    ),
                    {
                        val blacklistFilePath = ConfigManager.modDirectoryPath.resolve("data/itemBlacklist.json")
                        val filters = AuctionFlipper.filters

                        // Close the menu
                        GuiUtils.closeGui()

                        if(filters.any { it.textInput == lastViewedAuction!!.stack?.getSkyblockId() }) {
                            ChatUtils.sendClientMessage("§cItem is already blacklisted. §7/sbt blacklist", prefix = true)
                            return@IconElement
                        }

                        filters.add(
                            GuiItemFilterPopup.FilteredItem(
                                lastViewedAuction!!.stack?.getSkyblockId()!!,
                                GuiItemFilterPopup.FilterType.EQUALS,
                                GuiItemFilterPopup.InputType.ITEM_ID
                            )
                        )
                        val gson = GsonBuilder().setPrettyPrinting().create()
                        val newData = JsonObject()
                        val jsonFilters = gson.toJsonTree(filters).asJsonArray // Convert filters to JsonArray
                        newData.add("filters", jsonFilters) // Add filters JsonArray to the JsonObject
                        DataManager.saveDataToFile(blacklistFilePath, newData)

                        ChatUtils.sendClientMessage("§cAdded ${lastViewedAuction!!.stack?.getSkyblockId()} to blacklist.", prefix = true)
                        ChatUtils.sendClientMessage("§7You can remove this item in /sbt blacklist", prefix = true)
                    },
                    backgroundColor = Color(255, 85, 85),
                    width = 10
                )
            )
            icons++

            if(AuctionNotifications.capturedAuctionID()) {
                lines.add(
                    GuiUtils.IconElement(
                        "§c✖",
                        7f + icons * 20f,
                        iconY,
                        listOf(
                            "§cIgnore Auction",
                            "§7Stops this auction from",
                            "§7giving notifications.",
                        ),
                        {
                            // Close the menu
                            GuiUtils.closeGui()
                            AuctionNotifications.ignoreCurrentAuction()
                            ChatUtils.sendClientMessage(
                                "§cIgnored Auction, you will no longer get notifications",
                                prefix = true
                            )
                        },
                        backgroundColor = Color(205, 55, 55),
                        width = 10
                    )
                )
                icons++
            }

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 150
                )

            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                GuiUtils.getLowestY(lines) + 2f,
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, (x + event.guiLeft + 180).toInt(), (y + event.guiTop).toInt())
            }
            GlStateManager.translate(0f, 0f, -52f)
        }

        override fun isActive(event: Event): Boolean {
            if (event !is GuiContainerBackgroundDrawnEvent) return false
            return (event.gui as GuiContainer).chestName()
                .contains("Auction View") && AuctionHouseConfig.auctionViewOverlay
        }
    }

    fun getPrices(pricesArray: JsonArray): List<String> {
        val out = mutableListOf<String>()
        for (price in pricesArray) {
            if (!price.isJsonNull) {
                out.add("§8 - §6${price.asLong.formatNumber()}")
            }
        }
        return out
    }

    class AuctionPriceOverlay : OverlayManager.Overlay() {
        init {
            this.x = 50.0
            this.y = 65.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.SIGN)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val pricingData = sellingAuction?.pricingData ?: return
            var itemLore = sellingAuction?.stack?.getLore() ?: return
            itemLore = itemLore.subList(2, itemLore.size)

            val itemName = itemLore[0]

            // Pricing and count for Live Auction listings and their prices
            val activeAucNum = if (pricingData.has("activeAuc")) pricingData.get("activeAuc").asInt else -1
            val activeAuc = if (activeAucNum != -1) activeAucNum.formatNumber() else "§cUnknown"
            val activeAucPrices =
                if (pricingData.has("activeAucPrices")) pricingData.get("activeAucPrices").asJsonArray else JsonArray()

            // Pricing and count for Live Bin Listings and their prices
            val activeBinNum = if (pricingData.has("activeBin")) pricingData.get("activeBin").asInt else -1
            val activeBin = if (activeBinNum != -1) activeBinNum.formatNumber() else "§cUnknown"
            val activeBinPrices =
                if (pricingData.has("activeBinPrices")) pricingData.get("activeBinPrices").asJsonArray else JsonArray()

            // Pricing and count for recently ended auction listings and their prices
            val soldAucNum = if (pricingData.has("aucSold")) pricingData.get("aucSold").asInt else -1
            val soldAuc = if (soldAucNum != -1) soldAucNum.formatNumber() else "§cUnknown"
            val soldAucPrices =
                if (pricingData.has("aucSoldPrices")) pricingData.get("aucSoldPrices").asJsonArray else JsonArray()

            // Pricing and count for recently ended Bin Listings and their prices
            val soldBinNum = if (pricingData.has("binSold")) pricingData.get("binSold").asInt else -1
            val soldBin = if (soldBinNum != -1) soldBinNum.formatNumber() else "§cUnknown"
            val soldBinPrices =
                if (pricingData.has("binSoldPrices")) pricingData.get("binSoldPrices").asJsonArray else JsonArray()

            val recentlySoldHover = mutableListOf(
                "§b§lRecently Sold Listings",
                "§9$soldAuc Sold Auctions"
            ) + getPrices(soldAucPrices) + listOf("§3$soldBin Sold BINs") + getPrices(soldBinPrices)

            val activeListingsHover = mutableListOf(
                "§b§lActive Listings",
                "§9$activeAuc Auctions"
            ) + getPrices(activeAucPrices) + listOf("§3$activeBin BINs") + getPrices(activeBinPrices)

            val lines = mutableListOf(
                Element(5f, 5f, itemName, itemLore, null),
                Element(
                    5f,
                    20f,
                    "§rLowest BIN: §6${if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asLong.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ),
                Element(
                    5f,
                    30f,
                    "§rAverage BIN: §3${if (pricingData.has("avgLowestBin")) pricingData.get("avgLowestBin").asLong.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ),
                Element(
                    5f,
                    43f,
                    "§b§l${max(activeAucNum + activeBinNum, 0).formatNumber()} Active Listings",
                    activeListingsHover,
                    null
                ),
                Element(
                    5f,
                    53f,
                    "§b§l${max(soldAucNum + soldBinNum, 0).formatNumber()} Recently Sold",
                    recentlySoldHover,
                    null
                ),
                Element(
                    7f,
                    66f,
                    "§rSug Price: §a${if (sellingAuction?.suggestedListingPrice != 0L) sellingAuction?.suggestedListingPrice?.formatNumber() else "§cUnknown"}",
                    listOf(
                        "§e§lSuggested Listing Price",
                        "§7Click to set §6${sellingAuction?.suggestedListingPrice?.formatNumber()} §7as price",
                        "§cItem Upgrades not included!"
                    ),
                    Runnable {
                        val sign = (event as SignDrawnEvent).sign
                        // Input price to sign
                        sign.signText[0] = ChatComponentText(sellingAuction?.suggestedListingPrice.toString())
                    },
                    drawBackground = true
                )
            )

            if (lastViewedAuction?.shouldSellBIN == false) {
                lines.add(
                    Element(
                        7f,
                        80f,
                        "§c§lSELL AS AN AUCTION!!",
                        listOf(
                            "§cThis item has a higher value as an auction"
                        ),
                        null
                    )
                )
            }

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 150
                )
            val sr = ScaledResolution(Utils.mc)

            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                (10 + 12 * lines.size).toFloat(),
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                if (segment.text.length > 30) {
                    segment.text = segment.text.substring(0, 30) + "..."
                }
                segment.draw(mouseX, mouseY, x.toInt() + (sr.scaledWidth / 2), y.toInt())
            }
        }

        override fun isActive(event: Event): Boolean {
            if (!AuctionHouseConfig.auctionSellingOverlay || event !is SignDrawnEvent) return false
            if (event.sign.signText[3].unformattedText == "starting bid") {
                return true
            }
            return false
        }
    }
}
