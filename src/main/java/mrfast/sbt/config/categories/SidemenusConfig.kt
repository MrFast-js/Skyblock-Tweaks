package mrfast.sbt.config.categories

import mrfast.sbt.config.*


object SidemenusConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Helpful Auction Guis",
        description = "Shows the extra information about your own and others auctions.",
        category = "Overlays",
        subcategory = "§1§rAuction house"
    )
    var auctionGuis = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Custom Auction Selling Menu",
        description = "Shows custom buttons, and inputs when selling an auction to create a better user experience.",
        category = "Overlays",
        subcategory = "§1§rAuction house"
    )
    var customCreateAuctionGui = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Minion Overlay",
        description = "Shows the extra information inside the minion gui.",
        category = "Miscellaneous",
        subcategory = "Minion"
    )
    var minionOverlay = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Collections Leaderboard Overlay",
        description = "Shows a leaderboard for the collection types",
        category = "Overlays",
        subcategory = "Miscellaneous"
    )
    var collectionsLeaderboard = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "SBT Trade Gui",
        description = "Shows extra information inside the trade gui, including Estimated Values",
        category = "Overlays",
        subcategory = "Miscellaneous"
    )
    var tradeOverlay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Granda Wolf Pet Combo Timer",
        description = "Shows time until your combo expires on the Grandma Wolf Pet",
        category = "Overlays",
        subcategory = "Pets"
    )
    var grandmaWolfTimer = false
}