package mrfast.sbt.config.categories

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.config.Config
import mrfast.sbt.guis.ConfigGui
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import mrfast.sbt.guis.GuiItemFilterPopup
import mrfast.sbt.guis.GuiItemFilterPopup.*
import mrfast.sbt.features.auctionHouse.AuctionFlipper
import org.lwjgl.input.Keyboard
import java.awt.Color

object AuctionHouseConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auction Flipper",
        description = "Scans the Hypixel API to display auctions based on the lowest BINs and 3-day price averages.\n§c§lRequires you to be in the hub if you don't have a booster cookie! §4This feature can make mistakes.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        isParent = true
    )
    var auctionFlipper = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Include BIN Flips",
        description = "Allows for BIN auctions to be shown. §cThis is risky you need to know what your doing.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_binFlips = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Include Auction Flips",
        description = "Allows for normal auctions to be shown.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_AucFlips = true

//    @ConfigProperty(
//        type = ConfigType.DROPDOWN,
//        name = "Item Value Method",
//        description = "Choose how the flipper values its found items, this can be a combination of custom settings or a preset",
//        category = "§1§rAuction House",
//        subcategory = "Auction Flipper",
//        dropdownOptions = ["Lowest BIN", "Average BIN", "Smart"],
//        parentName = "Auction Flipper"
//    )
//    var itemValueMethod = "Smart"

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Notification Sound",
        description = "Toggle the sound played when a flip is being shown.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_notificationSound = true

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Profit Margin",
        description = "The minimum amount of profit for an auction to be shown to you.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_profitMargin = 200000

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Minimum Volume",
        description = "The minimum amount of sales per day for an auction to be shown to you.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_minimumVolume = 1

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Minimum Flip Percent",
        description = "The minimum percent of profit from an auction to be shown to you. §7Default: §e5§7%",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_minimumPercent = 5

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Minimum Time Remaining (Minutes)",
        description = "When showing auctions, will make sure its ending sooner than this time. §c5 Minutes Recommended.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_minimumTime = 5

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Notifications Limit",
        description = "The max amount of flips to be show to you, this will prevent lag.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_maxNotifications = 50

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Make Purse Max Amount",
        description = "Make the amount of money you can spend on an auction equal to your purse.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var AF_usePurseLimit = false

    @ConfigProperty(
        type = ConfigType.KEYBIND,
        name = "Open Best Flip Keybind",
        description = "Opens up the bid menu for the item with the highest profit.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper"
    )
    var autoAuctionFlipOpenKeybind: Int? = Keyboard.KEY_F

//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Easy Auction Buying",
//        description = "By spam clicking you will auto buy/bid the item from that is currently viewed.",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils"
//    )
//    var autoAuctionFlipEasyBuy = false

    @ConfigProperty(
        type = ConfigType.LABEL,
        name = "Auction Flipper Filters",
        description = "Filters allow for items to be removed based on criteria like pets, skins, furniture",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        isParent = true
    )
    var AF_filtersEnabled = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Pets",
        description = "Filters out pets from Auction Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_petFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Farming Tools",
        description = "Filters out farming tools such as §fEuclid's Wheat Hoe §rand §5Cocoa Chopper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_farmingToolFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Furniture & Decorations",
        description = "Filters out furniture from Auction Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_furnitureFilter = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Dyes",
        description = "Filters out dyes from Auction Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_dyeFilter = false

    @ConfigProperty(
        type = ConfigType.BUTTON,
        name = "Item ID Blacklist",
        description = "Filters out any item that match a specified ID",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters",
        placeholder = "§eEdit Blacklist"
    )
    var AF_blackList = Runnable {
        val popup = GuiItemFilterPopup(
            "AH Flipper Item Blacklist",
            "itemBlacklist.json",
            { AuctionFlipper.filters },
            { newFilters -> AuctionFlipper.filters = newFilters.toMutableList() },
            AuctionFlipper.defaultFilterList
        )

        popup.runOnClose {
            GuiUtil.open(ConfigGui())
        }
        GuiUtil.open(popup)
    }

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Losing Auctions",
        description = "Highlights auctions that you dont have the top bid on.",
        category = "§1§rAuction House",
        subcategory = "Overlays",
        isParent = true
    )
    var highlightLosingAuctions = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        description = "",
        category = "§1§rAuction House",
        subcategory = "Overlays",
        parentName = "Highlight Losing Auctions"
    )
    var highlightLosingAuctionsColor = Color.RED

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Bidding Overlay",
        description = "Shows useful information when looking at all your bids.",
        category = "§1§rAuction House",
        subcategory = "Overlays"
    )
    var biddingOverlay = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auction View Overlay",
        description = "Shows useful information when viewing a specific auction such as potential profit from flipping it.",
        category = "§1§rAuction House",
        subcategory = "Overlays"
    )
    var auctionViewOverlay = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auction Selling Price Overlay",
        description = "Shows useful information when selling an item including suggested price to sell at.",
        category = "§1§rAuction House",
        subcategory = "Overlays"
    )
    var auctionSellingOverlay = true


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Custom Outbid Notifications",
        description = "Allows customization of the default hypixel outbid notifications.",
        category = "§1§rAuction House",
        subcategory = "Chat",
        isParent = true
    )
    var customOutbidNotifications = true

    @ConfigProperty(
        type = ConfigType.TEXT,
        name = "Customize Notification",
        description = "Use §a{bidder}§r, §a{item}§r, and §a{amount}\n§r in your text to insert their values.",
        category = "§1§rAuction House",
        subcategory = "Chat",
        parentName = "Custom Outbid Notifications"
    )
    var customOutbidNotificationsText = "&c&l[OUTBID] &f{item} &eby &6{amount} coins &e&lCLICK!"

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Play Sound",
        description = "Plays a sound when your outbid on an auction",
        category = "§1§rAuction House",
        subcategory = "Chat",
        parentName = "Custom Outbid Notifications"
    )
    var customOutbidNotificationsPlaySound = true

//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Prevent Spam",
//        description = "Stops the same auction from showing up multiple times if you havent bid on it since.",
//        category = "§1§rAuction House",
//        subcategory = "Chat",
//        parentName = "Custom Outbid Notifications"
//    )
//    var customOutbidNotificationsStopSpam = true
}