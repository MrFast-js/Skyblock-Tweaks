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
        name = "Show equipment",
        description = "",
        category = "§1§rDungeons",
        subcategory = "Miscellaneous",
        parentName = "Party Finder Join Info"
    )
    var partyfinderJoinInfo_showEquipment = false

//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Highlight Trash",
//            description = "Draws a red box around items that just fill up your inventory.",
//            category = "§1§rDungeons",
//            subcategory = "Miscellaneous",
//            isParent = true
//    )
//    var highlightTrash = true
//
//    @ConfigProperty(
//            type = ConfigType.BUTTON,
//            name = "§eEdit Trash",
//            description = "The trash list will be updated once you save the file. \nTrash is an item whose Skyblock ID contains any of the entries.",
//            category = "§1§rDungeons",
//            subcategory = "Miscellaneous",
//            placeholder = "§cEdit Trash",
//            parentName = "Highlight Trash"
//    )
//    var editTrash = Runnable {
//
//    }
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