package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color


object MiscellaneousConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Fire Veil Timer",
            description = "Shows the time until the Fire Veil ability ends.",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    var fireVeilTimer = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Prevent §5Gloomlock Grimoire§r Death",
            description = "Stops the §5Gloomlock Grimoire's§r ability from killing the player by blocking the ability if §cPlayer Health < 25%",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    var gloomlockGrimoireProtection = true

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show §5Fire Freeze Staff§r Freeze Timer",
            description = "Shows a timer in the world for when the §5Fire Freeze Staff's§r ability will freeze",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    var fireFreezeStaffFreezeTimer = true

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show Prehistoric Egg Distance Counter",
            description = "Shows the blocks walked on the Prehistoric Egg item",
            category = "Miscellaneous",
            subcategory = "Items"
    )
    var prehistoricEggDistance = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Cooldown Tracker",
            description = "Shows a display with your hotbar items' cooldowns on each item",
            category = "Miscellaneous",
            subcategory = "Items",
            isParent = true
    )
    var cooldownTracker = true

    @ConfigProperty(
            type = ConfigType.DROPDOWN,
            name = "Cooldown Display Type",
            description = "Draws a square behind items that are currently on cooldown",
            category = "Miscellaneous",
            subcategory = "Items",
            parentName = "Cooldown Tracker",
            dropdownOptions = ["Slot Background", "Item Bar"]
    )
    var cooldownTrackerType = "Item bar"

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Background Color",
            description = "",
            category = "Miscellaneous",
            subcategory = "Items",
            parentName = "Cooldown Tracker"
    )
    var cooldownTrackerSquareColor = Color.white

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Bar Color",
            description = "",
            category = "Miscellaneous",
            subcategory = "Items",
            parentName = "Cooldown Tracker"
    )
    var cooldownTrackerBarColor = Color.cyan

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Show Estimated Value",
            description = "Shows the Estimated Value for various items in Skyblock. Calculates using things like enchants and stars",
            category = "Miscellaneous",
            subcategory = "Item Price Info"
    )
    var showEstimatedPrice = true
}