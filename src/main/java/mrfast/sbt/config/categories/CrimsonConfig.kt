package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import org.fusesource.jansi.Ansi
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
        description = "Highlights Blazes in-range to be hooked with the Moody Grappleshot",
        category = "Crimson Isles",
        subcategory = "Misc",
        parentName = "Moody Grappleshot Highlight"
    )
    var moodyGrappleShotHighlightColor = Color.RED
}