package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color

object MiningConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Automaton Loot Tracker",
        description = "Tracks the loot from Automatons. Starts after an Automaton is killed",
        category = "Mining",
        subcategory = "Trackers"
    )
    var AutomatonTracker = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Gemstone Tracker",
        description = "Tracks the stats from mining gemstones like Coins per hour",
        category = "Mining",
        subcategory = "Trackers"
    )
    var gemstoneTracker = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Commissions Tracker",
        description = "Tracks your progress on commissions using real numbers instead of percentages",
        category = "Mining",
        subcategory = "Trackers"
    )
    var CommisionsTracker = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Placed Cobblestone",
        description = "Highlights the cobblestone you place in crystal hollows",
        category = "Mining",
        subcategory = "Crystal Hollows",
        isParent = true
    )
    var highlightCobblestone = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Cobblestone Color",
        description = "",
        category = "Mining",
        subcategory = "Crystal Hollows",
        parentName = "Highlight Placed Cobblestone"
    )
    var highlightCobblestoneColor = Color.cyan
}