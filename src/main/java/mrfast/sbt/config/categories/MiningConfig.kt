package mrfast.sbt.config.categories

import mrfast.sbt.config.Config
import mrfast.sbt.guis.components.CustomColor
import mrfast.sbt.managers.ConfigProperty
import mrfast.sbt.managers.ConfigType
import java.awt.Color

object MiningConfig : Config() {
    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Crit Particle Highlight",
        description = "Highlights crit particles in certain scenarios, allowing for helping with Precision Mining HOTM Upgrade, and Treasure Chests.",
        category = "Mining",
        subcategory = "Render",
        isParent = true
    )
    var CritParticleHighlight = true

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Unfocused Highlight Color",
        description = "Color of the highlight when unfocused",
        category = "Mining",
        parentName = "Crit Particle Highlight",
        subcategory = "Render"
    )
    var CritParticleHighlightUnfocused = CustomColor(Color.WHITE)

    @ConfigProperty(
        type = ConfigType.COLOR,
        name = "Focused Highlight Color",
        description = "Color of the highlight when focused",
        category = "Mining",
        parentName = "Crit Particle Highlight",
        subcategory = "Render"
    )
    var CritParticleHighlightFocused = CustomColor(Color.RED)

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Dwarven Mines Map",
        description = "Show a map of the dwarven mines, highlighting key points of interest and helping you navigate.",
        category = "Mining",
        subcategory = "Misc."
    )
    var dwarvenMinesMap = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Gemstone Grinder Overlay",
        description = "Enables an overlay for the Gemstone Grinder GUI, displaying useful information about gemstone slots, including costs and other details.",
        category = "Mining",
        subcategory = "Overlays"
    )
    var gemstoneOverlay = true

    @ConfigProperty(
        type = ConfigType.TOGGLE,
        name = "Forge Flipper Overlay",
        description = "Shows profitable forge crafts to flip when in the Forge Menus",
        category = "Mining",
        subcategory = "Overlays",
        isParent = true
    )
    var forgeFlipperOverlay = true

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Max Price (Millions)",
        description = "Hides Flips that require a cost above ",
        category = "Mining",
        subcategory = "Overlays",
        parentName = "Forge Flipper Overlay"
    )
    var forgeFlipperMaxPrice = 0

    @ConfigProperty(
        type = ConfigType.NUMBER,
        name = "Max HOTM Level",
        description = "Hides Flips that require a HOTM level above this value",
        category = "Mining",
        subcategory = "Overlays",
        parentName = "Forge Flipper Overlay"
    )
    var forgeFlipperMaxHotm = 10
}