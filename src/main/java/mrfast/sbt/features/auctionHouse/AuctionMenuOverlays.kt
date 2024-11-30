package mrfast.sbt.features.auctionHouse

import com.google.gson.JsonObject
import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.SignDrawnEvent
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
import mrfast.sbt.utils.Utils.getStringWidth
import mrfast.sbt.utils.Utils.getRegexGroups
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

@SkyblockTweaks.EventComponent
object AuctionMenuOverlays {
    var biddedAuctions = listOf<Auction>()
    var ownedAuctions = listOf<Auction>()
    var lastViewedAuction: Auction? = null
    var sellingAuction: Auction? = null

    open class Auction(var slot: Slot, var uuid: String) {
        var price = 0L
        var profit = 0L
        var suggestedListingPrice = 0
        var winning: Boolean? = null
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

            if (inventory.getStackInSlot(33) != null) {
                val bidHistory = inventory.getStackInSlot(33)
                val pastBids = mutableListOf<String>()
                val BIDDER_REGEX = """By: (.*)""".toRegex()
                for (line in bidHistory.getLore()) {
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
                var newBidPrice = 0L
                val NEW_BID_REGEX = """New bid: ([\d,]+) coins""".toRegex()

                for (line in submitBidStack.getLore(clean = true)) {
                    if (line.matches(NEW_BID_REGEX)) {
                        newBidPrice = line.getRegexGroups(NEW_BID_REGEX)!![1]!!.value.replace(",","").toLong()
                        break
                    }
                }
                auction.price = newBidPrice
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
        auction.pricingData = ItemApi.getItemPriceInfo(id) ?: return

        val stack = auction.stack ?: return
        val suggestedListingPrice = ItemUtils.getSuggestListingPrice(stack) ?: return

        auction.suggestedListingPrice = suggestedListingPrice
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
                    5f,
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
                (10 + 12 * lines.size).toFloat(),
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
            if (event !is GuiContainerBackgroundDrawnEvent) return false

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
            val pricingData = ItemApi.getItemPriceInfo(itemId) ?: return
            val resellProfit = lastViewedAuction?.profit ?: return
            val coloredSymbol = if (resellProfit > 0) "§a+" else "§c"

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
                    "§rLowest BIN: §6${if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asInt.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    5f,
                    28f,
                    "§rAverage BIN: §3${if (pricingData.has("price_avg")) pricingData.get("price_avg").asInt.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    5f,
                    43f,
                    "§rResell Profit: ${coloredSymbol}${resellProfit.formatNumber()}",
                    listOf(
                        "§e§lFlip Potential",
                        "§7Buy for §a${(lastViewedAuction!!.price).formatNumber()}",
                        "§7Sell for §d${(lastViewedAuction!!.suggestedListingPrice).formatNumber()}"
                    ),
                    null
                )
            )

            if (lastViewedAuction!!.otherBidder != null && lastViewedAuction!!.otherBidder != Utils.mc.thePlayer.name) {
                lines.add(
                    Element(
                        7f,
                        55f,
                        "§9Party Bidder",
                        listOf(
                            "§e/party ${lastViewedAuction!!.otherBidder}",
                            "§7Allows for a chance to negotiate",
                            "§7if a player wont stop bidding"
                        ),
                        {
                            Utils.mc.thePlayer.closeScreen()
                            ChatUtils.sendPlayerMessage("/party ${lastViewedAuction!!.otherBidder}")
                        },
                        drawBackground = true
                    )
                )
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
                (10 + 12 * lines.size).toFloat(),
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

            val lines = mutableListOf(
                Element(5f, 5f, itemName, itemLore, null), Element(
                    5f,
                    20f,
                    "§rLowest BIN: §6${if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asInt.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    5f,
                    30f,
                    "§rAverage BIN: §3${if (pricingData.has("price_avg")) pricingData.get("price_avg").asInt.formatNumber() else "§cUnknown"}",
                    null,
                    null
                ), Element(
                    7f,
                    43f,
                    "§rSug Price: §a${if (sellingAuction?.suggestedListingPrice != 0) sellingAuction?.suggestedListingPrice?.formatNumber() else "§cUnknown"}",
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
