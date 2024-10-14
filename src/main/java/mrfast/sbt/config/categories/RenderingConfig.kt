package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import mrfast.sbt.utils.ChatUtils
import java.awt.Color

object RenderingConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Disable Damage Tint",
        description = "Stops the §cred§r damage tint from rendering on hurt mobs",
        category = "Rendering",
        subcategory = "Misc"
    )
    var disableDamageTint = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Item Effective Area",
        description = "Draws an area on the ground of where mobs are in range of the weapon. §cWorks for §6Hyperion§c & §6Spirit Scepter",
        category = "Rendering",
        subcategory = "Misc",
        isParent = true
    )
    var showItemEffectiveArea = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Only Show When Mobs Nearby",
        description = "Stops the area from rendering if there is no mobs present",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Show Item Effective Area"
    )
    var showItemEffectiveAreaMobsNearby = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Area Color",
        description = "",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Show Item Effective Area"
    )
    var showItemEffectiveAreaColor = Color.RED


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Zealot / Bruiser Spawn Locations",
        description = "Shows where and when zealots or bruisers will spawn in The End.",
        category = "Rendering",
        subcategory = "Misc",
        isParent = true
    )
    var zealotBruiserLocations = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Draw Timer",
        description = "Shows when the next group of zealots will spawn",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Zealot / Bruiser Spawn Locations"
    )
    var zealotBruiserLocTimer = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Draw Bounding Box",
        description = "Shows the area where a zealot can spawn",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Zealot / Bruiser Spawn Locations"
    )
    var zealotBruiserLocDrawBox = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Unready Color",
        description = "Used when zealots or bruisers is on a cooldown.",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Zealot / Bruiser Spawn Locations"
    )
    var zealotBruiserLocUnready = Color(0x484848)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Ready Color",
        description = "Used when zealots or bruisers are out of cooldown.",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Zealot / Bruiser Spawn Locations"
    )
    var zealotBruiserLocReady = Color(0x6A5ACD)


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Advanced Dragon Hitbox",
        description = "Draws a better hitbox for dragons. Useful for §cMaster Mode 7§r and §bDragons",
        category = "Rendering",
        subcategory = "Misc",
        isParent = true
    )
    var advancedDragonHitbox = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Hitbox Color",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Show Advanced Dragon Hitbox"
    )
    var advancedDragonHitboxColor = Color(0x55FF91)


    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Main Path Color",
        description = "Main color used when drawing the loaded path. \nThis will display if points are §a< 3§r blocks away.",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathTracingColor1 = Color(0x55FF91)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Secondary Path Color",
        description = "Secondary color used when drawing the loaded path. \nThis will display if points are §c> 3§r blocks away.",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathTracingColor2 = Color(0x5590FF)

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Path Render Range",
        description = "Max distance to render path points at. The lower this is the better your fps will be.",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathRenderRange = 75

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Draw Through Walls",
        description = "",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathThroughWalls = true

//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Glowing Dungeon Teammates",
//            description = "Make your teammates glow in dungeons.",
//            category = "Render",
//            subcategory = "1.9 Glow Effect"
//    )
//    var glowingDungeonPlayers = false
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Glowing Items!",
//            description = "Make items glow depending on rarity. (Requires Fast render to be off)",
//            category = "Render",
//            subcategory = "1.9 Glow Effect"
//    )
//    var glowingItems = false
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Show Advanced Dragon Hitbox",
//            description = "Draws a better hitbox for dragons. Useful for Master Mode 7 and Dragons",
//            category = "Render",
//            subcategory = "Highlights",
//            isParent = true
//    )
//    var advancedDragonHitbox = true
//
//    @ConfigProperty(
//            type = ConfigType.COLOR,
//            name = "Dragon Hitbox Color",
//            description = "",
//            category = "Render",
//            subcategory = "Highlights",
//            parentName = "Show Advanced Dragon Hitbox"
//    )
//    var advancedDragonHitboxColor = Color.green
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Show Zealot Spawn Areas & Spawn Timer",
//            description = "Draws where zealots spawn and when zealots will spawn. (this includes bruisers)",
//            category = "Render",
//            subcategory = "Zealots"
//    )
//    var showZealotSpawnAreas = true
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Small Items",
//            description = "Makes the items you hold smaller",
//            category = "Render",
//            subcategory = "Items"
//    )
//    var smallItems = false
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Highlight Selected Bestiary Mobs",
//            category = "Render",
//            description = "Highlights mobs from the §aBestiary Menu§r in the world with glowing effect.  §eCtrl+Click§r on a mob inside the §aBestiary Menu§r to start tracking it.",
//            subcategory = "Bestiary",
//            isParent = true
//    )
//    var highlightBestiaryMobs = true
//
//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Middle Click To Track",
//            category = "Render",
//            description = "If you middle click a mob in the world it will start being tracked.",
//            subcategory = "Bestiary",
//            parentName = "Highlight Selected Bestiary Mobs"
//    )
//    var highlightBestiaryMobsMidClick = true
//
//    @ConfigProperty(
//            type = ConfigType.COLOR,
//            name = "Mob Highlight Color",
//            category = "Render",
//            description = "",
//            subcategory = "Bestiary",
//            parentName = "Highlight Selected Bestiary Mobs"
//    )
//    var highlightBestiaryColor = Color.ORANGE
//
//    @ConfigProperty(
//            type = ConfigType.TEXT,
//            name = "Tracked Mobs",
//            description = "This is the list of currently tracked mobs",
//            category = "Render",
//            subcategory = "Bestiary",
//            parentName = "Highlight Selected Bestiary Mobs"
//    )
//    var trackedBestiaryMobs = ""
//
//    @ConfigProperty(
//            type = ConfigType.BUTTON,
//            name = "§eOpen Bestiary Menu",
//            description = "Opens the bestiary menu where you can select which mobs to track.",
//            category = "Render",
//            subcategory = "Bestiary",
//            placeholder = "§cOpen Bestiary"
//    )
//    val openBestiary = Runnable {
//        ChatUtils.sendPlayerMessage("/bestiary")
//        ChatUtils.sendClientMessage("test 123§d test 123")
//    }
}