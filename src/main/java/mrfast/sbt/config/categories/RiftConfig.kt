package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object RiftConfig : Config() {

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Nearly Coherent Rod Radius Display",
        description = "Shows the effective radius of the rabbit attraction area when holding a §aNearly Coherent doR gnihsiF",
        category = "The Rift",
        subcategory = "Farming",
        isParent = true
    )
    var nearlyCoherentRodRadius = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Circle Color",
        description = "",
        category = "The Rift",
        subcategory = "Farming",
        parentName = "Nearly Coherent Rod Radius Display"
    )
    var nearlyCoherentRodRadiusColor = CustomColor(0x00FFFF)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Thrown Anchor",
        description = "Highlights the location of the anchor thrown by the T5 Vampire Slayer",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        isParent = true
    )
    var highlightVampireAnchors = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Draw Tracer",
        description = "Draws a line from the crosshair to the anchor",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Highlight Thrown Anchor"
    )
    var highlightVampireAnchorsTracer = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Color",
        description = "",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Highlight Thrown Anchor"
    )
    var highlightVampireAnchorColor = CustomColor(0xFFD500)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Rift Time Bar",
        description = "Moveable Rift Time Bar that adjusts depending on your time left in the rift / damage taken",
        category = "The Rift",
        subcategory = "Stat Displays",
        isParent = true
    )
    var riftTimeBar = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Rift Fill Color",
        description = "",
        category = "The Rift",
        subcategory = "Stat Displays",
        parentName = "Rift Time Bar"
    )
    var riftBarFillColor = CustomColor(0x5A0075FF)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Rift Bar Background",
        description = "",
        category = "The Rift",
        subcategory = "Stat Displays",
        parentName = "Rift Time Bar"
    )
    var riftBarBarColor = CustomColor(0x000000)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Shen Puzzle Helper",
        description = "Highlights the buttons you need to press for the Shen Puzzle",
        category = "The Rift",
        subcategory = "Puzzles",
        isParent = true
    )
    var shenPuzzleHelper = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Button Highlight Color",
        description = "",
        category = "The Rift",
        subcategory = "Puzzles",
        parentName = "Shen Puzzle Helper"
    )
    var shenButtonColor = CustomColor(0x55FF55)


//    @ConfigProperty(
//            type = ConfigType.TOGGLE,
//            name = "Larva Silk Display",
//            description = "Highlights where the line will be drawn when using Larva Silk.",
//            category = "The Rift",
//            subcategory = "General",
//            isParent = true
//    )
//    var larvaSilkDisplay = true
//
//    @ConfigProperty(
//            type = ConfigType.COLOR,
//            name = "Larva Silk Block Color",
//            description = "",
//            category = "The Rift",
//            subcategory = "General",
//            parentName = "Larva Silk Display"
//    )
//    var larvaSilkBlockColor: Color = Color.ORANGE
//
//    @ConfigProperty(
//            type = ConfigType.COLOR,
//            name = "Larva Silk Line Color",
//            description = "",
//            category = "The Rift",
//            subcategory = "General",
//            parentName = "Larva Silk Display"
//    )
//    var larvaSilkLineColor: Color = Color.CYAN
}