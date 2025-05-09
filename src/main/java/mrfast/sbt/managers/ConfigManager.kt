package mrfast.sbt.managers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.LinkedTreeMap
import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.guis.components.CustomColor
import net.minecraft.client.Minecraft
import java.awt.Color
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Field
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class ConfigManager {
    private val configFilePath = modDirectoryPath.resolve("config.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()

    companion object {
        val modDirectoryPath = Minecraft.getMinecraft().mcDataDir.resolve("config").resolve("skyblocktweaks")
        var categories: MutableMap<String, Category> = mutableMapOf()
        var defaultMap: MutableMap<String, Any> = mutableMapOf()
    }

    data class Category(
        val name: String,
        var subcategories: MutableMap<String, Subcategory> = mutableMapOf(),
    )

    data class Subcategory(
        val name: String,
        var features: MutableMap<String, Feature> = mutableMapOf(),
    )

    data class Feature(
        val name: String,
        val description: String,
        var value: Any,
        var type: ConfigType,
        val field: Field,
        val isParent: Boolean = false,
        val placeholder: String,
        var dropdownOptions: Array<String> = arrayOf(),
        val parentName: String = "",
        var parentFeature: Feature? = null,
        var featureComponent: UIComponent = UIContainer(),
        var subcategory: Subcategory = Subcategory(""),
        var optionElements: MutableMap<String, Feature> = mutableMapOf(),
        var optionsHidden: Boolean = true
    )

    fun saveConfig() {
        modDirectoryPath.mkdirs()

        val configFile = configFilePath

        // Convert fields to a Map
        val fieldMap = mutableMapOf<String, Any>()

        // Get all fields in the class, including private ones
        for (config in SkyblockTweaks.config.list) {
            val properties = config::class.memberProperties

            for (property in properties) {
                val configAnnotation = property.findAnnotation<ConfigProperty>()

                if (configAnnotation != null) {
                    val fieldName = property.name
                    val value = property.getter.call(config) ?: continue
                    val field = config::class.java.getDeclaredField(fieldName)

                    field.isAccessible = true
                    // Create a new Feature
                    val feature = Feature(
                        configAnnotation.name,
                        configAnnotation.description,
                        value,
                        configAnnotation.type,
                        field,
                        placeholder = configAnnotation.placeholder,
                        parentName = configAnnotation.parentName,
                        isParent = configAnnotation.isParent,
                    )
                    if (feature.type == ConfigType.DROPDOWN) {
                        feature.dropdownOptions = configAnnotation.dropdownOptions
                    }

                    val category = categories.getOrPut(configAnnotation.category) { Category(configAnnotation.category) }
                    val subcategory = category.subcategories.getOrPut(configAnnotation.subcategory) { Subcategory(configAnnotation.subcategory) }

                    subcategory.features[fieldName] = feature
                    feature.subcategory = subcategory

                    if (Runnable::class.java.isAssignableFrom(field.type)) continue

                    fieldMap[fieldName] = value
                }
            }
        }

        val json = gson.toJson(fieldMap)

        // Write the JSON to the config file
        FileWriter(configFile).use { writer ->
            writer.write(json)
        }
    }

    // Goes through config file and sets each config options value equal to its config.json equivalent
    fun loadConfig() {
        val configFile = configFilePath
        if (configFile.exists()) {
            try {
                // Read JSON content from the config file using Gson
                val gson = Gson()
                val reader = FileReader(configFile)
                val fieldMap = gson.fromJson(reader, Map::class.java)

                // Get all properties in the class, including private ones
                for (config in SkyblockTweaks.config.list) {
                    val properties = config::class.memberProperties
                    for (property in properties) {
                        val propertyName = property.name

                        if (fieldMap.containsKey(propertyName)) {
                            var loadedValue = fieldMap[propertyName]
                            // the field map converted ints to doubles, this is undoing
                            if (loadedValue is Double) loadedValue = loadedValue.toInt()
                            val field: Field = config::class.java.getDeclaredField(propertyName)
                            field.isAccessible = true

                            // Deserialize nested objects (e.g., CustomColor) if needed
                            if (loadedValue is LinkedTreeMap<*, *>) {
                                // Check if it's a CustomColor object
                                if (field.type == CustomColor::class.java) {
                                    loadedValue = CustomColor(Color.RED).fromString(loadedValue.toString())
                                } else {
                                    // Otherwise, deserialize it to the appropriate type
                                    loadedValue = gson.fromJson(loadedValue.toString(), field.type)
                                }
                            }

                            saveDefault(propertyName, field.get(SkyblockTweaks.config))
                            // update the field from Config.kt
                            try {
                                field.set(SkyblockTweaks.config, loadedValue)
                            } catch (e: Exception) {
                                // type of config option changed, ie boolean -> int
                            }
                        }
                    }
                }

                reader.close()
            } catch (e: JsonSyntaxException) {
                // Handle JSON syntax exception
                e.printStackTrace()
            }
        }
    }

    private fun saveDefault(propertyName: String, value: Any) {
        defaultMap.putIfAbsent(propertyName, value)
    }
}

enum class ConfigType {
    TOGGLE,
    LABEL,
    COLOR,
    DROPDOWN,
    BUTTON,
    NUMBER,
    KEYBIND,
    TEXT
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class ConfigProperty(
    val type: ConfigType,
    val name: String,
    val description: String = "",
    val isParent: Boolean = false,
    val parentName: String = "",
    val dropdownOptions: Array<String> = arrayOf(),
    val category: String,
    val subcategory: String,
    val placeholder: String = "",
    val risky: Boolean = false,
)