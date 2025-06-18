package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
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
        name = "Interpolate Health and Mana",
        description = "Smoothly counts health and mana features instead of 'jumping'",
        category = "General",
        subcategory = "Stat Displays",
    )
    var interpolateStats = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Clean Action Bar",
        description = "Hides Health, Mana, and other attributes from the action bar. §aRecommended to use with Stat Displays",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var cleanerActionBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideHealthFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Drill Fuel Status",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideDrillFuel = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Mana",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Overflow Mana",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideOverflowManaFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Rift Time",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideRiftTimeFromBar = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Secret Count",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Clean Action Bar"
    )
    var hideSecretsFromBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Defense",
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
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarHealthColor = CustomColor(0xff5555)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Absorption Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarAbsorbColor = CustomColor(0xFFAA00)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Health Bar Background",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarBarColor = CustomColor(0x242424)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Health Bar Border Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Bar"
    )
    var healthBarBarBorderColor = CustomColor(0x323232)

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
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarManaColor = CustomColor(0x5654eb)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Overflow Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarOverflowColor = CustomColor(0x55FFFF)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mana Bar Background",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarBarColor = CustomColor(0x242424)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mana Bar Border Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarBarBorderColor = CustomColor(0x323232)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Overflow Mana",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Bar"
    )
    var manaBarShowOverflow = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Drill Fuel Bar",
        description = "Moveable Fuel Bar that shows your drills fuel status",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var drillFuelBar = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Drill Fuel Bar Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Drill Fuel Bar"
    )
    var drillFuelBarColor = CustomColor(0x0F540F)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Drill Fuel Bar Background",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Drill Fuel Bar"
    )
    var drillFuelBarBarColor = CustomColor(Color.BLACK)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Health Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var healthNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Max Health",
        description = "Will display your current health our of max health.",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Number"
    )
    var showMaxHealth = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Normal Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Number"
    )
    var healthDisplayColor = CustomColor(255, 85, 85)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Absorption Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Health Number"
    )
    var healthDisplayAbsorptionColor = CustomColor(0xFFAA00)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Speed Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var speedNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Secret Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var secretNumber = true

    @ConfigProperty(
        type = ConfigType.DROPDOWN,
        name = "Text Style",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Secret Number",
        dropdownOptions = ["Shadowed", "Default", "Outlined"]
    )
    var secretNumberTextStyle = "Shadowed"

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Speed Number"
    )
    var speedNumberColor = CustomColor(255, 255, 255)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Effective Health Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var effectiveHealthNumber = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Max Effective Health",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Effective Health Number"
    )
    var showMaxEffectiveHealth = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Effective Health Number"
    )
    var effectiveHealthNumberColor = CustomColor(0, 170, 0)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Drill Fuel Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var drillFuelDisplay = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Drill Fuel Number"
    )
    var drillFuelDisplayColor = CustomColor(0x167716)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Mana Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var manaNumber = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Mana Number"
    )
    var manaNumberColor = CustomColor(0x5555FF)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Overflow Mana Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var overflowManaNumber = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Overflow Mana Number"
    )
    var manaOverflowNumberColor = CustomColor(0, 170, 170)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Defense Number",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var defenseNumber = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Defense Color",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Defense Number"
    )
    var defenseNumberColor = CustomColor(85, 85, 255)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Cleaner Hotbar Hud",
        description = "Hides elements like hunger bar, armor bar",
        category = "General",
        subcategory = "Stat Displays",
        isParent = true
    )
    var cleanerHotbarArea = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Armor Bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Hud"
    )
    var hideArmorBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Hunger Bar",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Hud"
    )
    var hideHungerBar = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Health Hearts",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Hud"
    )
    var hideHealthHearts = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Hide Air Bubbles",
        category = "General",
        subcategory = "Stat Displays",
        parentName = "Cleaner Hotbar Hud"
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
        subcategory = "Party"
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
        type = ConfigType.DROPDOWN,
        name = "Text Style",
        category = "General",
        subcategory = "Item Pickup Log",
        parentName = "Show item pickup log",
        dropdownOptions = ["Shadowed", "Default", "Outlined"]
    )
    var itemPickupLogTextStyle = "Shadowed"

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Material IDs",
        category = "General",
        subcategory = "Item Pickup Log",
        parentName = "Show item pickup log"
    )
    var itemPickupLogItemIds = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Material Prices",
        category = "General",
        subcategory = "Item Pickup Log",
        parentName = "Show item pickup log"
    )
    var itemPickupLogItemPrices = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Kat Flipper Overlay",
        description = "Shows profitable pets to flip when in the Kat Pet Sitter Menu",
        category = "General",
        subcategory = "Overlays"
    )
    var katFlipperOverlay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Lowballing Overlay",
        description = "Shows a customizable overlay in the trade menu showing the amount to offer for an item",
        category = "General",
        subcategory = "Overlays",
        isParent = true
    )
    var lowballingOverlay = false

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Lowballing Overlay Offer Percentage %",
        description = "The percentage of the item value you are offering",
        category = "General",
        subcategory = "Overlays",
        parentName = "Lowballing Overlay"
    )
    var lowballingOverlayOfferPercentage = 60

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Experimentation Overlay",
        description = "Shows coins per RNG when looking at the rng selector menu in the Experimentation Table",
        category = "General",
        subcategory = "Overlays"
    )
    var experimentationOverlay = false
}