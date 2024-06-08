package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType


object DungeonConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Party Finder Join Info",
        description = "Shows stats of players when they join your party such as, armor, weapons, equipment, cata, secrets, and more",
        category = "§1§rDungeons",
        subcategory = "Miscellaneous",
        isParent = true
    )
    var partyfinderJoinInfo = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Player Equipment",
        description = "",
        category = "§1§rDungeons",
        subcategory = "Miscellaneous",
        parentName = "Party Finder Join Info"
    )
    var partyfinderJoinInfo_showEquipment = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Player Hotbar",
        description = "§cReplaces \"Hype ✘   Term ✘   Clay ✘\" line!",
        category = "§1§rDungeons",
        subcategory = "Miscellaneous",
        parentName = "Party Finder Join Info"
    )
    var partyfinderJoinInfo_showHotbar = false


    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Floor 2 Spawn Timer",
            description = "Renders timers showing when the §dPriest§f, §cWarrior§f, §aArcher§f, §bMage§f will spawn.",
            category = "§1§rDungeons",
            subcategory = "Spawn Timers"
    )
    var floor2SpawnTimer = true

//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Dungeon Map",
//            description = "Render a moveable dungeon map on screen",
//            category = "§1§rDungeons",
//            subcategory = "Dungeon Map",
//            isParent = true
//    )
//    var dungeonMap = false
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Render Player Names",
//            description = "Draws names above the players",
//            category = "§1§rDungeons",
//            subcategory = "Dungeon Map",
//            parentName = "Dungeon Map"
//    )
//    var dungeonMapPlayerNames = true
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Blood Door Highlight",
//            description = "Marks the player's name red if they opened the blood door",
//            category = "§1§rDungeons",
//            subcategory = "Dungeon Map",
//            parentName = "Dungeon Map"
//    )
//    var dungeonMapBloodGuy = true
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Render Player Heads",
//            description = "Adds an outline to the player heads on the dungeon map",
//            category = "§1§rDungeons",
//            subcategory = "Dungeon Map",
//            parentName = "Dungeon Map"
//    )
//    var dungeonMapHeads = true
}