package mrfast.sbt.config.Categories

import mrfast.sbt.config.Config
import mrfast.sbt.config.ConfigProperty
import mrfast.sbt.config.ConfigType
import java.awt.Color

object CustomizationConfig : Config() {
    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Outdated Version Notification",
            description = "Receive a chat notification when using an outdated version of Skyblock Features",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    var updateNotify = true

    @ConfigProperty(
            type = ConfigType.DROPDOWN,
            name = "Update Check Type",
            description = "Choose between Full and Beta Releases for update checks",
            category = "§2§rCustomization",
            subcategory = "Mod",
            dropdownOptions = ["Full (Full Releases)", "Pre (Beta Releases)"]
    )
    var updateCheckType = "Pre (Beta Releases)"

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "§cDeveloper Mode",
            description = "§eDeveloper Mode§r causes more logs to happen, as well as enabling certain debug features.",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    var developerMode = false

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Use At Own Risk Features",
            description = "Toggles whether §cUse At Own Risk§r features will show inside of the config menu",
            category = "§2§rCustomization",
            subcategory = "Mod"
    )
    var riskyFeatures = true

    @ConfigProperty(
            type = ConfigType.TOGGLE,
            name = "Chroma Config Border",
            description = "Woah rainbows",
            category = "§2§rCustomization",
            subcategory = "Colors"
    )
    var chromaConfigBorder = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Toggle Switch Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Colors"
    )
    var toggleSwitchColor = Color.GREEN
}