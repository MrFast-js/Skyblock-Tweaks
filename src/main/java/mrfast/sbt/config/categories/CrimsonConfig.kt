package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType


object CrimsonConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Crimson Isles Map",
            description = "Displays a live easy to follow map with named locations in the crimson isles",
            category = "Crimson Isles",
            subcategory = "Misc"
    )
    var crimsonIslesMap = false
}