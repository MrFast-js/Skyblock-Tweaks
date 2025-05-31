package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object CrimsonConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Crimson Isles Map",
        description = "Displays a live easy to follow map with named locations in the crimson isles",
        category = "Crimson Isles",
        subcategory = "Misc"
    )
    var crimsonIslesMap = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Item Attribute Overlay",
        description = "Displays item attributes on top of drawn item stacks",
        category = "Crimson Isles",
        subcategory = "Misc",
        isParent = true
    )
    var itemAttributeOverlay = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Normal Text Color",
        description = "Color of the item attribute text",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayTextColor = CustomColor(Color.CYAN)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Display God Rolls",
        description = "Marks items with a god roll differently, god rolls are items with attributes that are above a certain tier and have a high suggested price",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayShowGodRoll = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "God roll Text Color",
        description = "Color of the item attribute text",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayGodRollTextColor = CustomColor(Color.YELLOW)

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "God Roll Tier Threshold",
        description = "Minimum tier of an attribute to be considered a god roll. \n§eRecommended: 6",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayGRTier = 6

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "God Roll Price Threshold",
        description = "Minimum price of an attribute to be considered a god roll. \n§eRecommended: 50,000,000",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayGRPrice = 50_000_000


    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Text Scale",
        description = "Scale of the item attribute text, 0-100%",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Item Attribute Overlay"
    )
    var itemAttributeOverlayTextScale = 60

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Moody Grappleshot Highlight",
        description = "Highlights Blazes in-range to be hooked with the Moody Grappleshot",
        category = "Crimson Isles",
        subcategory = "Misc",
        isParent = true
    )
    var moodyGrappleShotHighlight = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Highlight Color",
        description = "",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Moody Grappleshot Highlight"
    )
    var moodyGrappleShotHighlightColor = CustomColor(Color.RED)
}