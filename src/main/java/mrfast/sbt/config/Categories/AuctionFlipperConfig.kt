package mrfast.sbt.config.Categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import org.lwjgl.input.Keyboard
import java.awt.Color


object AuctionFlipperConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Highlight Auction Flips",
            description = "Highlights auctions that have a certain amount of profit or more.",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            isParent = true
    )
    var highlightAuctionProfit = false

    @ConfigProperty(
            type = ConfigType.NUMBER,
            name = "Margin",
            description = "Highlights auctions that have this margin",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            parentName = "Highlight Auction Flips"
    )
    var highlightAuctionProfitMargin = 100000

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Highlight Auctions Status",
            description = "Highlights auctions in the \"View Bids\" menu based off of if its outbid, ended, sold to someone else.",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            isParent = true
    )
    var highlightAuctionStatus = true

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Winning Auctions",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            parentName = "Highlight Auctions Status"
    )
    var winningAuctionColor = Color(0x669bbc)

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Collect Auction",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            parentName = "Highlight Auctions Status"
    )
    var collectAuctionColor = Color(0x003049)

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Outbid Auctions",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            parentName = "Highlight Auctions Status"
    )
    var outbidAuctionColor = Color(0xe9c46a)

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Lost Auctions",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils",
            parentName = "Highlight Auctions Status"
    )
    var lostAuctionColor = Color(0xF48361)

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Auction Flipper Active",
            description = "Enables or disables the flipper with its current settings.\n§cDo not put 100% trust in the mod, it can and probably will make mistakes.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings",
            isParent = true
    )
    var aucFlipperEnabled = false

    @ConfigProperty(
            type = ConfigType.KEYBIND,
            name = "Keybind to toggle",
            description = "Keybind used to toggle the flipper on/off",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings",
            parentName = "Auction Flipper Active"
    )
    var aucFlipperKeybind = -1

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Include BIN Flips",
            description = "Check BINs for flips. §cThis is risky you need to know what your doing.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var aucFlipperBins = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Include Item Upgrades",
            description = "Adds value to the auctions based off of enchants, stars, drill parts, hot potato books, etc. §cThis may over-value items!",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var aucFlipperItemUpgrades = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Include Auction Flips",
            description = "Check auctions for flips",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var aucFlipperAucs = true

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Play sound when flip found",
            description = "",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var aucFlipperSound = true

    @ConfigProperty(
            type = ConfigType.NUMBER,
            name = "Profit Margin",
            description = "The minimum amount of profit for an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipMargin = 200000

    @ConfigProperty(
            type = ConfigType.NUMBER,
            name = "Minimum Volume",
            description = "The minimum amount of sales per day for an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipMinVolume = 1

    @ConfigProperty(
            type = ConfigType.NUMBER,
            name = "Minimum Flip Percent",
            description = "The minimum percent of profit from an auction to be shown to you.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipMinPercent = 5

    @ConfigProperty(
            type = ConfigType.NUMBER,
            name = "Max Amount Of Auctions",
            description = "The max amount of flips to be show to you, this will prevent lag.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipMaxAuc = 50

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Make Purse Max Amount",
            description = "Make the amount of money you can spend on an auction equal to your purse.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipSetPurse = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Auto Open",
            description = "Opens up the bid menu for the item with the highest profit. \n§cThis is slower than holding down key",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipOpen = false

    @ConfigProperty(
            type = ConfigType.KEYBIND,
            name = "Open Best Flip Keybind",
            description = "Opens up the bid menu for the item with the highest profit.",
            category = "§1§rAuction Flipper",
            subcategory = "Flipper Settings"
    )
    var autoAuctionFlipOpenKeybind = Keyboard.KEY_F

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Easy Auction Buying",
            description = "By spam clicking you will auto buy/bid the item from that is currently viewed.",
            category = "§1§rAuction Flipper",
            subcategory = "Auction Utils"
    )
    var autoAuctionFlipEasyBuy = false

    @ConfigProperty(
            type = ConfigType.CHECKBOX,
            name = "Filter Out Pets",
            description = "Filters out pets from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionFilterOutPets = false

    @ConfigProperty(
            type = ConfigType.CHECKBOX,
            name = "Filter Out Skins",
            description = "Filters out minion skins, armor skins, and pet skins from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionFilterOutSkins = false

    @ConfigProperty(
            type = ConfigType.CHECKBOX,
            name = "Filter Out Furniture",
            description = "Filters out furniture from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionFilterOutFurniture = false

    @ConfigProperty(
            type = ConfigType.CHECKBOX,
            name = "Filter Out Dyes",
            description = "Filters out dyes from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionFilterOutDyes = false

    @ConfigProperty(
            type = ConfigType.CHECKBOX,
            name = "Filter Out Runes",
            description = "Filters out runes from Auto Flipper",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionFilterOutRunes = false

    @ConfigProperty(
            type = ConfigType.TEXT,
            name = "Blacklist",
            description = "Filters out any blacklisted items. Seperate with §a;§r.§aExample: 'bonemerang;stick'",
            category = "§1§rAuction Flipper",
            subcategory = "§1§rAuction Flipper Filter"
    )
    var autoAuctionBlacklist = "bonemerang;soldier;jungle pick;"
}