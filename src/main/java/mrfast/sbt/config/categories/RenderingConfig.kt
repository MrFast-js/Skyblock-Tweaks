package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
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
    var showItemEffectiveAreaColor = CustomColor(Color.RED)


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
    var zealotBruiserLocUnready = CustomColor(0x484848)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Ready Color",
        description = "Used when zealots or bruisers are out of cooldown.",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Zealot / Bruiser Spawn Locations"
    )
    var zealotBruiserLocReady = CustomColor(0x6A5ACD)


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Dragons",
        description = "Draws a outline for dragons for better visibility. Useful for §cMaster Mode 7§r and §bDragons",
        category = "Rendering",
        subcategory = "Misc",
        isParent = true
    )
    var highlightDragon = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Hitbox Color",
        category = "Rendering",
        subcategory = "Misc",
        parentName = "Show Advanced Dragon Hitbox"
    )
    var highlightDragonColor = CustomColor(0x55FF91)


    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Main Path Color",
        description = "Main color used when drawing the loaded path. \nThis will display if points are §a< 3§r blocks away.",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathTracingColor1 = CustomColor(0x55FF91)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Secondary Path Color",
        description = "Secondary color used when drawing the loaded path. \nThis will display if points are §c> 3§r blocks away.",
        category = "Rendering",
        subcategory = "Path Tracing §7(/path)"
    )
    var pathTracingColor2 = CustomColor(0x5590FF)

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
//            type = ConfigType.BUTTON,
//            name = "§eOpen Bestiary Menu",
//            description = "Opens the bestiary menu where you can select which mobs to track.",
//            category = "Render",
//            subcategory = "Bestiary",
//            placeholder = "§cOpen Bestiary"
//    )
//    val openBestiary = Runnable {
//        ChatUtils.sendPlayerMessage("/bestiary")
//    }
}