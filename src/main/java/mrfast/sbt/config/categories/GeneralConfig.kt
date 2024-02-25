package mrfast.sbt.config.categories

// Import the ConfigProperty annotation if it's in a different package
import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color


object GeneralConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Low Health Tint",
        description = "As your health decreases your screen will start to get §cred",
        category = "General",
        subcategory = "Low Health"
    )
    var lowHealthTint = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cleaner Action Bar",
        description = "Hides Health, Mana and other attributes from action bar",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var cleanerActionBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health",
        description = "Hides health from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Action Bar"
    )
    var hideHealthFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Mana",
        description = "Hides mana from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Action Bar"
    )
    var hideManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Overflow Mana",
        description = "Hides overflow mana from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Action Bar"
    )
    var hideOverflowManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Rift Time",
        description = "Hides rift time from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Action Bar"
    )
    var hideRiftTimeFromBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Defense",
        description = "Hides defense from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Action Bar"
    )
    var hideDefenseFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Bar",
        description = "Moveable Health Bar that adjusts depending on absorption and damage taken",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var healthBar = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Health Color",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarHealthColor = Color.RED

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Absorption Color",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarAbsorbColor = Color(0xFFAA00)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Health Bar Background",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarBarColor = Color.BLACK

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Bar",
        description = "Moveable Mana Bar that adjusts depending on abilities and overflow mana",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var manaBar = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mana Color",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarManaColor = Color(0x5555FF)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Overflow Color",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarOverflowColor = Color(0x55FFFF)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mana Bar Background",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarBarColor = Color.BLACK

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Display",
        description = "Movable health display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var healthDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Speed Display",
        description = "Movable Speed display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var speedDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Effective Health Display",
        description = "Movable Effective Health display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var effectiveHealthDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Display",
        description = "Movable mana",
        category = "General",
        subcategory = "Stat Displays"
    )
    var manaDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Overflow Mana Display",
        description = "Movable overflow mana display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var overflowManaDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Defense Display",
        description = "Movable defense display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var defenseDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cleaner Hotbar Area",
        description = "Hides elements like hunger bar, armor bar",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var cleanerHotbarArea = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Armor Bar",
        description = "Hide the armor icons above health bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideArmorBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Hunger Bar",
        description = "Hide the food icons above hotbar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHungerBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health Hearts",
        description = "Hide the health icons above health bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHealthHearts = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Party Display",
        description = "Shows players in your party, along with classes if party finder",
        category = "General",
        subcategory = "Party"
    )
    var partyMemberDisplay = true


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auto Party Chat",
        description = "Auto sends §a/chat p§r after joining a party §cWarning Use At Own Risk",
        category = "General",
        subcategory = "Party",
        risky = true
    )
    var autoPartyChat = false
}