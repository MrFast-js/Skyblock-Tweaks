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
        name = "Auto Holy Icey Spicy",
        description = "Automatically uses §9Holy Ice§r before Twin Claws activates.",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        isParent = true,
        risky = true
    )
    var AutoIceySpicy = false

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Swap Delay",
        description = "Delay in milliseconds to swap to the §9Holy Ice§r in your hotbar. Default is 300ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Holy Icey Spicy"
    )
    var autoIceySpicySwapDelay = 300

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Use Item Delay",
        description = "Delay in milliseconds after swapping to the §9Holy Ice§r to use it. Default is 200ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Holy Icey Spicy"
    )
    var autoIceySpicyUseDelay = 200

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Total Delay",
        description = "Delay in milliseconds in between how many times this can activate. Default is 1200ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Holy Icey Spicy"
    )
    var autoIceySpicyTotalDelay = 4000


    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auto Melon Muncher",
        description = "Automatically eats healing melons when below a % health.",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        isParent = true,
        risky = true
    )
    var AutoMelonMuncher = false

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Health Trigger %",
        description = "The % of health to trigger the Auto Melon Muncher. Default is 50%",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Melon Muncher"
    )
    var autoMelonMuncherPercent = 50

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Swap Delay",
        description = "Delay in milliseconds to swap to the melon in your hotbar. Default is 300ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Melon Muncher"
    )
    var autoMelonMuncherMelonDelay = 300

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Use Item Delay",
        description = "Delay in milliseconds after swapping to the melon to use it. Default is 200ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Melon Muncher"
    )
    var autoMelonMuncherUseDelay = 200

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Total Delay",
        description = "Delay in milliseconds in between how many times this can activate. Default is 1200ms",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Melon Muncher"
    )
    var autoMelonMuncherTotalDelay = 1200

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auto Tower Toucher",
        description = "Automatically punches the Killer Springs during Vampire Slayer",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        isParent = true,
        risky = true
    )
    var AutoTowerPuncher = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Highlight Tower",
        description = "",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Tower Toucher"
    )
    var AutoTowerPuncherHighlight = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Highlight Color",
        description = "",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Tower Toucher"
    )
    var AutoTowerPuncherHighlightColor = Color.RED

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Minimum Punches Per Second",
        description = "",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Tower Toucher"
    )
    var AutoTowerPuncherMin = 8

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Maximum Punches Per Second",
        description = "",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Tower Toucher"
    )
    var AutoTowerPuncherMax = 10

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Auto Stake Smoker",
        description = "Automatically equips Steake Steak when the Bloodfiend is below 20%, allowing you to smoke his ass.",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        isParent = true,
        risky = true
    )
    var AutoSteakSmoker = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Force Hold",
        description = "Doesnt allow your held item to be changed off of the Steak until slayer is killed.",
        category = "The Rift",
        subcategory = "Vampire Slayer",
        parentName = "Auto Stake Smoker"
    )
    var AutoSteakSmokerForce = false

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
        category = "The Rift",
        subcategory = "Stat Displays",
        parentName = "Rift Time Bar"
    )
    var riftBarFillColor = CustomColor(0x5A0075FF)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Rift Bar Background",
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
        category = "The Rift",
        subcategory = "Puzzles",
        parentName = "Shen Puzzle Helper"
    )
    var shenButtonColor = CustomColor(0x55FF55)


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
        category = "The Rift",
        subcategory = "General",
        parentName = "Larva Silk Display"
    )
    var larvaSilkBlockColor = CustomColor(Color.ORANGE)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Larva Silk Line Color",
        category = "The Rift",
        subcategory = "General",
        parentName = "Larva Silk Display"
    )
    var larvaSilkLineColor = CustomColor(Color.CYAN)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Rift Time Display",
        description = "Displays the time remaining in the rift as a movable ui element",
        category = "The Rift",
        subcategory = "Stat Displays",
        isParent = true
    )
    var riftTimeDisplay = false

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Show max time",
        category = "The Rift",
        subcategory = "Stat Displays",
        parentName = "Rift Time Display"
    )
    var riftTimeShowMax = false

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Text Color",
        category = "The Rift",
        subcategory = "Stat Displays",
        parentName = "Rift Time Display"
    )
    var riftTimeColor = CustomColor(85,255,255)

    @ConfigProperty(
        type = ConfigType.DROPDOWN,
        name = "Text Style",
        category = "The Rift",
        subcategory = "Stat Displays",
        dropdownOptions = ["Shadowed", "Default", "Outlined"],
        parentName = "Rift Time Display"
    )
    var riftTimeTextStyle = "Shadowed"
}