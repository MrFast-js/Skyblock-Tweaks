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