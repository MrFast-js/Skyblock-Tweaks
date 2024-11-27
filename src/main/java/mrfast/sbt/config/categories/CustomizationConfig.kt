package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object CustomizationConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Outdated Version Notification",
        description = "Receive a chat notification when using an outdated version of Skyblock Tweaks",
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
        name = "Christmas Snow Effect",
        description = "Enables a snowing effect in guis during December",
        category = "§2§rCustomization",
        subcategory = "Theme",
        isParent = true
    )
    var snowEffect = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show Only In This Menu",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Christmas Snow Effect"
    )
    var snowEffectExclusive = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Force Enabled",
        description = "Forces the snowing effect to be enabled, even if it's not December",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Christmas Snow Effect"
    )
    var snowEffectForce = false


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
        name = "Background Blur",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme"
    )
    var backgroundBlur = true

    @ConfigProperty(
        type = ConfigType.DROPDOWN,
        name = "Config Gui Theme",
        description = "Woah shapes and colors.",
        category = "§2§rCustomization",
        subcategory = "Theme",
        dropdownOptions = ["Gray", "Dark + Cyan", "Dark + Orange","Dark + Pink"]
    )
    var selectedTheme = "Dark + Cyan"

    @ConfigProperty(
        type = ConfigType.LABEL,
        name = "Custom Menu Colors",
        description = "Allows changing of almost every color in this gui!",
        category = "§2§rCustomization",
        subcategory = "Theme",
        isParent = true
    )
    var customMenuColors = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Enabled Switch Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var onSwitchColor = CustomColor(Color.GREEN)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Disabled Switch Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var offSwitchColor = CustomColor(Color.GRAY)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Feature Border Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var featureBorderColor = CustomColor(Color.GRAY)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Window Border Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var windowBorderColor = CustomColor(Color.CYAN)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Feature Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var featureBackgroundColor = CustomColor(34, 34, 34)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Header Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var headerBackgroundColor= CustomColor(34, 34, 34)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Main Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var mainBackgroundColor = CustomColor(22, 22, 22)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Categories Background Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var sidebarBackgroundColor = CustomColor(28, 28, 28)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Gui Line Colors",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var guiLineColors = CustomColor(130, 130, 130)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Default Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var defaultCategoryColor = CustomColor(180, 180, 180)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Hovered Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var hoveredCategoryColor = CustomColor(255, 255, 255)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Selected Category Color",
        description = "",
        category = "§2§rCustomization",
        subcategory = "Theme",
        parentName = "Custom Menu Colors"
    )
    var selectedCategoryColor = CustomColor(0, 255, 255)
}