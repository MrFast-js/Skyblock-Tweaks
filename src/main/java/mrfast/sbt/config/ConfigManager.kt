package mrfast.sbt.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import net.minecraft.client.Minecraft
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Field
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

abstract class ConfigManager {
    private val configDirectoryPath = "${Minecraft.getMinecraft().mcDataDir}\\config\\skyblocktweaks"
    private val configFilePath = "$configDirectoryPath\\config.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class Category(
            val name: String,
            var subcategories: MutableMap<String, Subcategory> = mutableMapOf()
    )

    data class Subcategory(
            val name: String,
            var features: MutableMap<String, Feature> = mutableMapOf()
    )

    data class Feature(
            val name: String,
            val description: String,
            var value: Any,
            var type: PropertyType
    )

    fun saveConfig() {
        val configDirectory = File(configDirectoryPath)
        configDirectory.mkdirs()

        val configFile = File(configFilePath)

        // Convert fields to a Map
        val fieldMap = mutableMapOf<String, Any>()

        // Get all fields in the class, including private ones
        val properties = Config::class.memberProperties

        for (property in properties) {
            val annotation = property.findAnnotation<ConfigProperty>()

            if (annotation != null) {
                val fieldName = property.name
                val value = property.getter.call(this) ?: continue

                // Create a new Feature
                val feature = Feature(annotation.name, annotation.description, value, annotation.type)
                val category = categories.getOrPut(annotation.category) { Category(annotation.category) }
                val subcategory = category.subcategories.getOrPut(annotation.subcategory) { Subcategory(annotation.subcategory) }

                // Add the Feature to the Subcategory
                subcategory.features[annotation.name] = feature

                println(category)
                println(subcategory)
                println(subcategory.features)

                fieldMap[fieldName] = value
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
        val configFile = File(configFilePath)
        if (configFile.exists()) {
            try {
                // Read JSON content from the config file using Gson
                val gson = Gson()
                val reader = FileReader(configFile)
                val fieldMap = gson.fromJson(reader, Map::class.java)

                // Get all properties in the class, including private ones
                val properties = Config::class.memberProperties
                println(properties)
                for (property in properties) {
                    val propertyName = property.name
                    if (fieldMap.containsKey(propertyName)) {
                        var loadedValue = fieldMap[propertyName]
                        // the field map converted ints to doubles, this is undoing
                        if (loadedValue is Double) loadedValue = loadedValue.toInt()
                        val field: Field = Config::class.java.getDeclaredField(propertyName)
                        field.isAccessible = true

                        saveDefault(propertyName,field.get(Config))

                        // update the field from Config.kt
                        field.set(Config, loadedValue)
                    }
                }

                reader.close()
            } catch (e: JsonSyntaxException) {
                // Handle JSON syntax exception
                e.printStackTrace()
            }
        }
    }


    private fun saveDefault(propertyName: String, field: Any) {
        defaultMap[propertyName] = field
    }

    companion object {
        var categories: MutableMap<String, Category> = mutableMapOf()
        var defaultMap: MutableMap<String, Any> = mutableMapOf()
    }
}

enum class PropertyType {
    TOGGLE,
    COLOR,
    DROPDOWN,
    BUTTON,
    NUMBER,
    CHECKBOX,
    KEYBIND,
    TEXT
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class ConfigProperty(
        val type: PropertyType,
        val name: String,
        val description: String,
        val category: String,
        val subcategory: String
)
