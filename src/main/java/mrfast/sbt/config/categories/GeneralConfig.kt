package mrfast.sbt.config.categories

// Import the ConfigProperty annotation if it's in a different package
import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType


object GeneralConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cleaner Action Bar",
        description = "Hides Health, Mana and other attributes from action bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        isParent = true
    )
    var cleanerActionBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health",
        description = "Hides health from action bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Action Bar"
    )
    var hideHealthFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Mana",
        description = "Hides mana from action bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Action Bar"
    )
    var hideManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Overflow Mana",
        description = "Hides overflow mana from action bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Action Bar"
    )
    var hideOverflowManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Defense",
        description = "Hides defense from action bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Action Bar"
    )
    var hideDefenseFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Bar",
        description = "Moveable Health Bar that adjusts depending on absorption and damage taken",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var healthBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Bar",
        description = "Moveable Mana Bar that adjusts depending on abilities and overflow mana",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var manaBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Display",
        description = "Movable health display",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var healthDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Speed Display",
        description = "Movable Speed display",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var speedDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Effective Health Display",
        description = "Movable Effective Health display",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var effectiveHealthDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Display",
        description = "Movable mana",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var manaDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Overflow Mana Display",
        description = "Movable overflow mana display",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var overflowManaDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Defense Display",
        description = "Movable defense display",
        category = "General",
        subcategory = "Health & Mana Bars"
    )
    var defenseDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cleaner Hotbar Area",
        description = "Hides elements like hunger bar, armor bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        isParent = true
    )
    var cleanerHotbarArea = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Armor Bar",
        description = "Hide the armor icons above health bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Hotbar Area"
    )
    var hideArmorBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Hunger Bar",
        description = "Hide the food icons above hotbar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHungerBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health Hearts",
        description = "Hide the health icons above health bar",
        category = "General",
        subcategory = "Health & Mana Bars",
        parentName = "Cleaner Hotbar Area"
    )
    var hideHealthHearts = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Party Member Display",
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