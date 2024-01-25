package mrfast.sbt.config

// Import the ConfigProperty annotation if it's in a different package
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.ConfigManager
import kotlin.reflect.KProperty

class Config : ConfigManager() {

    @ConfigProperty(
            type = PropertyType.TOGGLE,
            name = "Test feature 1",
            description = "womp womp 1",
            category = "General",
            subcategory = "Test"
    )
    var testFeature1 = true

    @ConfigProperty(
            type = PropertyType.NUMBER,
            name = "Test feature 2",
            description = "scooby doo",
            category = "General",
            subcategory = "Test"
    )
    var testFeature2 = true

    @ConfigProperty(
            type = PropertyType.NUMBER,
            name = "Test feature 3",
            description = "scooby doo",
            category = "General",
            subcategory = "Test"
    )
    var testFeature3 = true



    // Save the config options to the file
    fun save() {
        saveConfig()
    }

    // Load the config options from config.json
    fun load() {
        loadConfig()
    }

    fun resetToDefault(property: KProperty<*>) {
        val propertyName = property.name
        val defaultValue = defaultMap[propertyName]
        if (defaultValue != null) {
            setProperty(propertyName, defaultValue)
            saveConfig()
        }
    }
}