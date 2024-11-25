package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
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
    var mythologicalHelperActualColor = CustomColor(255, 85, 85)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Prediction Line Color",
        description = "",
        category = "§1§rEvents",
        subcategory = "Diana",
        parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperPredictionColor = CustomColor(Color.WHITE)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Next Burrow Line Color",
        description = "",
        category = "§1§rEvents",
        subcategory = "Diana",
        parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperNextColor = CustomColor(Color.CYAN)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Default Burrow Color",
        description = "",
        category = "§1§rEvents",
        subcategory = "Diana",
        parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperDefaultColor = CustomColor(Color.GREEN)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mob Burrow Color",
        description = "",
        category = "§1§rEvents",
        subcategory = "Diana",
        parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperMobColor = CustomColor(Color.RED)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Treasure Burrow Color",
        description = "",
        category = "§1§rEvents",
        subcategory = "Diana",
        parentName = "Diana Mythological Helper"
    )
    var mythologicalHelperTreasureColor = CustomColor(0xFFAA00)
}