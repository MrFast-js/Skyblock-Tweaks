package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import org.lwjgl.input.Keyboard
import java.awt.Color


object AuctionHouseConfig : Config() {
//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Highlight Auction Flips",
//        description = "Highlights auctions that have a certain amount of profit or more.",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        isParent = true
//    )
//    var highlightAuctionProfit = false
//
//    @ConfigProperty(
//        type = ConfigType.NUMBER,
//        name = "Minimum Profit Margin",
//        description = "Highlights auctions that have this margin",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        parentName = "Highlight Auction Flips"
//    )
//    var highlightAuctionProfitMargin = 100000

//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Highlight Auctions Status",
//        description = "Highlights auctions in the \"View Bids\" menu based off of if its outbid, ended, sold to someone else.",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        isParent = true
//    )
//    var highlightAuctionStatus = true
//
//    @ConfigProperty(
//        type = ConfigType.COLOR,
//        name = "Winning Auctions",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        parentName = "Highlight Auctions Status"
//    )
//    var winningAuctionColor = Color(0x669bbc)
//
//    @ConfigProperty(
//        type = ConfigType.COLOR,
//        name = "Collect Auction",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        parentName = "Highlight Auctions Status"
//    )
//    var collectAuctionColor = Color(0x003049)
//
//    @ConfigProperty(
//        type = ConfigType.COLOR,
//        name = "Outbid Auctions",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        parentName = "Highlight Auctions Status"
//    )
//    var outbidAuctionColor = Color(0xe9c46a)
//
//    @ConfigProperty(
//        type = ConfigType.COLOR,
//        name = "Lost Auctions",
//        category = "§1§rAuction House",
//        subcategory = "Auction Utils",
//        parentName = "Highlight Auctions Status"
//    )
//    var lostAuctionColor = Color(0xF48361)

//    @ConfigProperty(
//        type = ConfigType.LABEL,
//        name = "Item Value Calculation",
//        description = "Choose between full slot highlight and border highlight",
//        category = "§1§rAuction House",
//        subcategory = "Item Value Calculation",
//        isParent = true
//    )
//    var itemValueCalculations = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auction Flipper",
        description = "Scans the Hypixel API to display auctions based on the lowest BINs and 3-day price averages.\n§c§lRequires you to be in the hub if you don't have a booster cookie! §4This mod can make mistakes.",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        isParent = true
    )
    var auctionFlipper = false

//    @ConfigProperty(
//        type = ConfigType.KEYBIND,
//        name = "Keybind to toggle",
//        description = "Keybind used to toggle the flipper on/off",
//        category = "§1§rAuction House",
//        subcategory = "Auction Flipper",
//        parentName = "Auction Flipper Active"
//    )
//    var aucFlipperKeybind = -1

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
//        type = ConfigType.TOGGLE,
//        name = "Use Item Price Estimation",
//        description = "Adds value to the auctions based off of enchants, stars, drill parts, hot potato books, etc. §cThis may over-value items!",
//        category = "§1§rAuction House",
//        subcategory = "Auction Flipper"
//    )
//    var AF_priceEstimation = false

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

//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Auto Open",
//        description = "Opens up the bid menu for the item with the highest profit. \n§cThis is slower than holding down key",
//        category = "§1§rAuction House",
//        subcategory = "Auction Flipper"
//    )
//    var autoAuctionFlipOpen = false

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
        description = "Filters out pets from Auto Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_petFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Skins",
        description = "Filters out minion skins, armor skins, and pet skins from Auto Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_skinFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Furniture",
        description = "Filters out furniture from Auto Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_furnitureFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Dyes",
        description = "Filters out dyes from Auto Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_dyeFilter = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Filter Out Runes",
        description = "Filters out runes from Auto Flipper",
        category = "§1§rAuction House",
        subcategory = "Auction Flipper",
        parentName = "Auction Flipper Filters"
    )
    var AF_runeFilter = false

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
}