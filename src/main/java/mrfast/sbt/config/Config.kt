package mrfast.sbt.config

// Import the ConfigProperty annotation if it's in a different package
import kotlin.reflect.KProperty

object Config : ConfigManager() {

    class TestFeature1 {
        @ConfigProperty(
                type = PropertyType.TOGGLE,
                name = "Test feature 1",
                description = "womp womp 1",
                category = "General",
                subcategory = "Test"
        )
        var testFeature1 = true

        @ConfigProperty(
                type = PropertyType.TOGGLE,
                name = "Test feature option 1",
                description = "womp womp 1",
                category = "General",
                subcategory = "Test"
        )
        var testFeatureOption1 = true

        @ConfigProperty(
                type = PropertyType.TOGGLE,
                name = "Test feature option 2",
                description = "womp womp 1",
                category = "General",
                subcategory = "Test"
        )
        var testFeatureOption2 = true
    }

    @ConfigProperty(
            type = PropertyType.NUMBER,
            name = "Test feature 2",
            description = "scooby doo where are you we got some clues to get now, yeah yeah scooby dooby doo where are youu, we got somewhere to be",
            category = "General",
            subcategory = "Misc"
    )
    var testFeature2 = 5

    @ConfigProperty(
            type = PropertyType.TEXT,
            name = "Test feature 3",
            description = "scooby doo",
            category = "General",
            subcategory = "Test"
    )
    var testFeature3 = "test123"


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
            saveConfig()
        }
    }
}