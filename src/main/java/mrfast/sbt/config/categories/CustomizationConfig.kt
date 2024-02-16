package mrfast.sbt.config.categories

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

    // No uses yet
//    @ConfigProperty(
//        type = ConfigType.TOGGLE,
//        name = "Use At Own Risk Features",
//        description = "Toggles whether §cUse At Own Risk§r features will show inside of the config menu",
//        category = "§2§rCustomization",
//        subcategory = "Mod"
//    )
//    var riskyFeatures = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Chroma Config Border",
        description = "Woah rainbows",
        category = "§2§rCustomization",
        subcategory = "Theme"
    )
    var chromaConfigBorder = true

    @ConfigProperty(
        type = ConfigType.DROPDOWN,
        name = "Config Gui Theme",
        description = "Woah shapes and colors. §cRequires reopening this GUI",
        category = "§2§rCustomization",
        subcategory = "Theme",
        dropdownOptions = ["Default", "§eSpace", "§bOcean", "§3MacOS"]
    )
    var selectedTheme = "Default"

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Custom Menu Colors",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        isParent = true
    )
    var customMenuColors = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Enabled Switch Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var enabledSwitchColor: Color = Color.GREEN

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Feature Border Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var featureBorderColor: Color = Color.GRAY

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Feature Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var featureBackgroundColor: Color = Color(34, 34, 34)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Header Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var headerBackgroundColor: Color = Color(34, 34, 34)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Main Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var mainBackgroundColor : Color = Color(22, 22, 22)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Categories Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var sidebarBackgroundColor : Color = Color(28, 28, 28)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Gui Line Colors",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var guiLineColors: Color = Color(130, 130, 130)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Default Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var defaultCategoryColor: Color = Color(180, 180, 180)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Hovered Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var hoveredCategoryColor: Color = Color(255, 255, 255)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Selected Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var selectedCategoryColor: Color = Color(0, 255, 255)
}