package mrfast.sbt.config.Categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color

object RiftConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Larva Silk Display",
            description = "Highlights where the line will be drawn when using Larva Silk.",
            category = "The Rift",
            subcategory = "General",
            isParent = true
    )
    var larvaSilkDisplay = true

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Larva Silk Block Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            parentName = "Larva Silk Display"
    )
    var larvaSilkBlockColor: Color = Color.ORANGE

    @ConfigProperty(
            type = ConfigType.COLOR,
            name = "Larva Silk Line Color",
            description = "",
            category = "The Rift",
            subcategory = "General",
            parentName = "Larva Silk Display"
    )
    var larvaSilkLineColor: Color = Color.CYAN
}