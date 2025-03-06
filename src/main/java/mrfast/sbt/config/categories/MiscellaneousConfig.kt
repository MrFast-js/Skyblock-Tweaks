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
        type = ConfigType.TOGGLE,
        name = "Show Item Pricing Data",
        description = "Data provided by the Skyblock Tweaks API! Shows Lowest BIN, Average BIN, Soonest Auction Price, Average Auction Price, Active Listings, Price Matching, Sales / Day",
        category = "Miscellaneous",
        subcategory = "Items"
    )
    var showItemPricingData = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Focused Highlight Color",
        description = "Color of the highlight when focused",
        category = "Mining",
        parentName = "Fire Freeze Staff Visual",
        subcategory = "Render"
    )
    var fireFreezeVisualColor = CustomColor(Color.RED)

//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Show Estimated Value",
//            description = "Shows the Estimated Value for various items in Skyblock. Calculates using things like enchants and stars",
//            category = "Miscellaneous",
//            subcategory = "Item Price Info"
//    )
//    var showEstimatedPrice = true
}