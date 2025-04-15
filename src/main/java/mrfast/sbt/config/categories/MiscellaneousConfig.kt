package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object MiscellaneousConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cake Bag Sorting Helper",
        description = "Provides a easy tool to help sort your new year cake bag in order.§b Low years -> High years",
        category = "Miscellaneous",
        subcategory = "New Years Cakes"
    )
    var cakeBagSortingHelper = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Missing Cakes",
        description = "Highlight cakes that your missing in the Auction House. §CMust open cake bag first!",
        category = "Miscellaneous",
        subcategory = "New Years Cakes"
    )
    var highlightMissingNewYearCakes = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Minion Overlay",
        description = "Gives extra information inside the minion gui such as coins per hour!",
        category = "Miscellaneous",
        subcategory = "Minions",
        isParent = true
    )
    var minionOverlay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Last Collected Above Minion",
        description = "Gives extra information inside the minion gui such as coins per hour!",
        category = "Miscellaneous",
        subcategory = "Minions",
        parentName = "Minion Overlay"
    )
    var lastCollectedAboveMinion = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Quiver Overlay",
        description = "Shows the arrows in currently your quiver.",
        category = "Miscellaneous",
        subcategory = "Quiver Overlay",
        isParent = true
    )
    var quiverOverlay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Only show when holding bow",
        description = "Stops showing the quiver overlay if you no longer hold a bow.s",
        category = "Miscellaneous",
        subcategory = "Quiver Overlay",
        parentName = "Quiver Overlay"
    )
    var quiverOverlayOnlyBow = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Arrow Type",
        description = "Shows the type of arrow selected in the quiver display.",
        category = "Miscellaneous",
        subcategory = "Quiver Overlay",
        parentName = "Quiver Overlay"
    )
    var quiverOverlayType = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Fire Freeze Staff Visual",
        description = "Shows a radius & timer in the world for when the §5Fire Freeze Staff's§r ability will freeze",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var fireFreezeVisual = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Focused Highlight Color",
        description = "Color of the highlight when focused",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Fire Freeze Staff Visual"
    )
    var fireFreezeVisualColor = CustomColor(Color.RED)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Item Pricing Data",
        description = "Data provided by the Skyblock Tweaks API! Customize the data shown in options.",
        category = "Miscellaneous",
        subcategory = "Items",
        isParent = true
    )
    var showItemPricingData = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Active Auction & BIN Volume",
        description = "Shows as §3Active: §a123 BIN §7| §b456 AUC",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showActiveAuctionStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Sales / Day for Auctions and BINs",
        description = "Shows as §3Sales/day: §a123 BIN §7| §b456 AUC",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showSalesStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Average and Lowest BIN",
        description = "Shows as §3BIN: §7AVG §e123,456 §8| §7LOW §e123,456",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showBinPricingStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Avg. Auction & Ending Soon Price",
        description = "Shows as §3AUC: §7AVG §e123,456 §8| §7SOON §e123,456",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showAuctionPricingStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Price Paid for items",
        description = "Shows as §3Price Paid: §d§l123,456",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showPricePaidStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Bazaar Buy and Sell Price",
        description = "Shows as §3Bazaar: §a1,234 Buy §7| §b2,345 Sell",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showBazaarStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Price Matching",
        description = "Shows as §a§lMATCHED PRICE: §712,345,678 §8100%§r, \n§rFinds the cheapest closest matching item on BIN",
        category = "Miscellaneous",
        subcategory = "Items",
        parentName = "Show Item Pricing Data"
    )
    var showPriceMatchingStat = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hotspot Circle Highlight",
        description = "Highlights the area of the hotspot, aswell as when the bobber is in the radius",
        category = "Miscellaneous",
        subcategory = "Fishing",
        isParent = true
    )
    var hotspotCircleHighlight = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Bobber Out Color",
        description = "",
        category = "Miscellaneous",
        subcategory = "Fishing",
        parentName = "Hotspot Circle Highlight"
    )
    var hotspotBobberOutColor = CustomColor(Color.RED)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Bobber In Color",
        description = "",
        category = "Miscellaneous",
        subcategory = "Fishing",
        parentName = "Hotspot Circle Highlight"
    )
    var hotspotBobberInColor = CustomColor(Color.GREEN)
}