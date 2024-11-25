package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object MiningConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Crit Particle Highlight",
        description = "Highlights crit particles in certain scenarios, allowing for helping with Precision Mining HOTM Upgrade, and Treasure Chests.",
        category = "Mining",
        subcategory = "Render",
        isParent = true
    )
    var CritParticleHighlight = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Unfocused Highlight Color",
        description = "Color of the highlight when unfocused",
        category = "Mining",
        parentName = "Crit Particle Highlight",
        subcategory = "Render"
    )
    var CritParticleHighlightUnfocused = CustomColor(Color.WHITE)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Focused Highlight Color",
        description = "Color of the highlight when focused",
        category = "Mining",
        parentName = "Crit Particle Highlight",
        subcategory = "Render"
    )
    var CritParticleHighlightFocused = CustomColor(Color.RED)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Dwarven Mines Map",
        description = "Show a map of the dwarven mines, highlighting key points of interest and helping you navigate.",
        category = "Mining",
        subcategory = "Misc."
    )
    var dwarvenMinesMap = true


//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Automaton Loot Tracker",
//        description = "Tracks the loot from Automatons. Starts after an Automaton is killed",
//        category = "Mining",
//        subcategory = "Trackers"
//    )
//    var AutomatonTracker = false
//
//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Gemstone Tracker",
//        description = "Tracks the stats from mining gemstones like Coins per hour",
//        category = "Mining",
//        subcategory = "Trackers"
//    )
//    var gemstoneTracker = false
//
//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Commissions Tracker",
//        description = "Tracks your progress on commissions using real numbers instead of percentages",
//        category = "Mining",
//        subcategory = "Trackers"
//    )
//    var CommisionsTracker = true
//
//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Highlight Placed Cobblestone",
//        description = "Highlights the cobblestone you place in crystal hollows",
//        category = "Mining",
//        subcategory = "Crystal Hollows",
//        isParent = true
//    )
//    var highlightCobblestone = false
//
//    @ConfigProperty(
//        type = ConfigType.COLOR,
//        name = "Cobblestone Color",
//        description = "",
//        category = "Mining",
//        subcategory = "Crystal Hollows",
//        parentName = "Highlight Placed Cobblestone"
//    )
//    var highlightCobblestoneColor = Color.cyan
}