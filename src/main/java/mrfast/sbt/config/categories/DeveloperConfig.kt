package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType


object DeveloperConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TEXT,
            name = "Mod API Url",
            description = "§cDo not change this if you do not know what you're doing!",
            category = "§eDeveloper",
            subcategory = "Settings"
    )
    var modAPIURL = "https://app.mrfast-developer.com/"

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show mob ids",
            description = "Shows skyblock mob ids on mobs in the world using Skyblock Mob Detector",
            category = "§eDeveloper",
            subcategory = "Settings",
            isParent = true
    )
    var showMobIds = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show mob ids through walls",
        description = "",
        category = "§eDeveloper",
        subcategory = "Settings",
        parentName = "Show mob ids"
    )
    var showMobIdsThroughWalls = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show item abilities",
            description = "Shows when a skyblock item uses its ability, used to create features with listening for item abilities",
            category = "§eDeveloper",
            subcategory = "Settings"
    )
    var showItemAbilities = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show Inspector in Guis",
            category = "§eDeveloper",
            subcategory = "Settings"
    )
    var showInspector = false

//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Log Debug info for Auction Flipper",
//            category = "§eDeveloper",
//            subcategory = "Settings"
//    )
//    var debugAuctionFlipper = false
}