package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color

object SlayerConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Slayer Timer",
        description = "Shows a breakdown of time to defeat the slayer including kill time & spawn time.",
        category = "Slayers",
        subcategory = "Misc"
    )
    var slayerTimer = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Slayer Bosses",
        description = "Shows a glow effect on summoned slayers and mini-bosses.",
        category = "Slayers",
        subcategory = "Misc",
        isParent = true
    )
    var highlightSlayerBosses = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Mini-Boss Color",
        description = "",
        category = "Slayers",
        subcategory = "Misc",
        parentName = "Highlight Slayer Bosses"
    )
    var miniBossColor = Color.GREEN

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Slayer Boss Color",
        description = "",
        category = "Slayers",
        subcategory = "Misc",
        parentName = "Highlight Slayer Bosses"
    )
    var slayerBossColor = Color.RED

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Voidgloom Laser Phase",
        description = "",
        category = "Slayers",
        subcategory = "Misc",
        parentName = "Highlight Slayer Bosses"
    )
    var voidgloomLaserPhase = Color.CYAN

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Voidgloom Hits Phase",
        description = "",
        category = "Slayers",
        subcategory = "Misc",
        parentName = "Highlight Slayer Bosses"
    )
    var voidgloomHitsPhase = Color.MAGENTA
}