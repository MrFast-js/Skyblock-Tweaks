package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color

object EventsConfig : Config() {

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Diana Mythological Helper",
            description = "Draw an extended line of where the Mythological burrow could be",
            category = "§1§rEvents",
            subcategory = "Diana",
            isParent = true
    )
    var mythologicalHelper = true

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Roughly Predict Burrows",
            description = "Marks a position in the world as to where the pointed to burrow could be. §CWarning these are rough estimates, they are not to be depended on.",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperPrediction = false

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Actual Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperActualColor: Color = Color(255, 85, 85)

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Prediction Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperPredictionColor: Color = Color.WHITE

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Next Burrow Line Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperNextColor: Color = Color.CYAN

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Default Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperDefaultColor: Color = Color.GREEN

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Mob Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperMobColor: Color = Color.RED

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Treasure Burrow Color",
            description = "",
            category = "§1§rEvents",
            subcategory = "Diana",
            parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperTreasureColor: Color = Color(0xFFAA00)
}