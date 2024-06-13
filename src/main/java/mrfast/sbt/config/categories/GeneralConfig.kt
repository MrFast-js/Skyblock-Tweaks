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
        description = "As your health decreases, your screen will start to get §cred",
        category = "General",
        subcategory = "Low Health"
    )
    var lowHealthTint = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Clean Action Bar",
        description = "Hides Health, Mana, and other attributes from the action bar",
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
        parentName = "Clean Action Bar"
    )
    var hideHealthFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Mana",
        description = "Hides mana from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Overflow Mana",
        description = "Hides overflow mana from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideOverflowManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Rift Time",
        description = "Hides rift time from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideRiftTimeFromBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Defense",
        description = "Hides defense from action bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
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
        name = "Show Overflow Mana",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarShowOverflow = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Number",
        description = "Movable health display",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var healthNumber = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Show Max Health",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Number"
    )
    var showMaxHealth = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Speed Number",
        description = "Movable Speed display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var speedNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Effective Health Number",
        description = "Movable Effective Health display",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var effectiveHealthNumber = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Show Max Effective Health",
        description = "",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Effective Health Number"
    )
    var showMaxEffectiveHealth = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Number",
        description = "Movable mana",
        category = "General",
        subcategory = "Stat Displays"
    )
    var manaNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Overflow Mana Number",
        description = "Movable overflow mana display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var overflowManaNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Defense Number",
        description = "Movable defense display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var defenseNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Damage Reduction Percentage",
        description = "Movable damage reduction percentage display",
        category = "General",
        subcategory = "Stat Displays"
    )
    var damageReductionPercentage = false

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
        description = "Hide the hunger bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHungerBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health Hearts",
        description = "Hide the Vanilla health bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHealthHearts = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Air Bubbles",
        description = "Hide the air bubble icons above hotbar when underwater",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Area"
    )
    var hideAirBubbles = false

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

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show item pickup log",
        description = "Shows items gained and lost, aswell as sack info",
        category = "General",
        subcategory = "Item Pickup Log",
        isParent = true
    )
    var itemPickupLog = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Material IDs",
        description = "",
        category = "General",
        subcategory = "Item Pickup Log",
        parentName = "Show item pickup log"
    )
    var itemPickupLogItemIds = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Material Prices",
        description = "",
        category = "General",
        subcategory = "Item Pickup Log",
        parentName = "Show item pickup log"
    )
    var itemPickupLogItemPrices = true
}