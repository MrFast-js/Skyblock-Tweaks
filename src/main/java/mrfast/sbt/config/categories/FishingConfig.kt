package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType


object FishingConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Hide Geyser Particles",
            description = "Hides the annoying particles in the §6Blazing Volcano.",
            category = "Fishing",
            subcategory = "Hide Things"
    )
    var hideGeyserParticles = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Highlight Geyser Box",
            description = "Creates a box of where the geyser area is in the §6Blazing Volcano",
            category = "Fishing",
            subcategory = "Highlights"
    )
    var geyserBoundingBox = false
}