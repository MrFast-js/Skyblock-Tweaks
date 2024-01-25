package mrfast.sbt.config

import net.minecraft.client.Minecraft
import java.io.File
import java.lang.reflect.Field
import java.util.*

abstract class ConfigManager {
    private val configDirectoryPath = "${Minecraft.getMinecraft().mcDataDir}\\config\\skyblocktweaks"
    private val configFilePath = "$configDirectoryPath\\config.json"
    private val configProperties = Properties()

    private fun getProperty(key: String, defaultValue: String): String {
        return configProperties.getProperty(key, defaultValue)
    }

    protected fun setProperty(key: String, value: String) {
        configProperties.setProperty(key, value)
    }

    fun loadConfig() {
        val configFile = File(configFilePath)
        if (configFile.exists()) {
            configFile.inputStream().use { input ->
                configProperties.load(input)
            }

            // Get all fields in the class, including private ones
            val fields = javaClass.declaredFields

            for (field in fields) {
                // Check if the field is annotated with @ConfigProperty
                if (field.isAnnotationPresent(ConfigProperty::class.java)) {
                    field.isAccessible = true // Allow access to private fields
                    val configPropertyAnnotation = field.getAnnotation(ConfigProperty::class.java)
                    val annotationName = configPropertyAnnotation.name

                    // Save the default value to the defaultMap
                    saveDefault(annotationName, field)

                    val defaultValue = field.get(this).toString()

                    // Load the value from the config file or use the default value
                    val loadedValue = getProperty(annotationName, defaultValue)

                    // Convert the loaded value to the appropriate type
                    val convertedValue = when (field.type) {
                        Boolean::class.java -> loadedValue.toBoolean()
                        Int::class.java -> loadedValue.toInt()
                        String::class.java -> loadedValue
                        else -> throw UnsupportedOperationException("Unsupported type: ${field.type}")
                    }

                    // Set the field value with the loaded value
                    field.set(this, convertedValue)
                }
            }
        }
    }

    private fun saveDefault(propertyName: String, field: Field) {
        val defaultValue = field.get(this).toString()
        defaultMap[propertyName] = defaultValue
    }

    fun saveConfig() {
        val configDirectory = File(configDirectoryPath)
        configDirectory.mkdirs()

        val configFile = File(configFilePath)
        configFile.outputStream().use { output ->
            configProperties.store(output, null)
        }
    }

    companion object {
        val defaultMap: MutableMap<String, String> = mutableMapOf()

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
