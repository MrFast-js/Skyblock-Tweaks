package mrfast.sbt.managers

import com.google.gson.*
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.utils.ChatUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths

@SkyblockTweaks.EventComponent
object DataManager {
    private var dataJson = JsonObject()
    private var dataFile = ConfigManager.modDirectoryPath.resolve("data/profilesData.json")

    init {
        val profileData = loadDataFromFile(dataFile)
        dataJson = profileData

        if (dataJson.has("selectedProfileIds")) {
            // Load past profile ids from files
            for (entry in dataJson["selectedProfileIds"].asJsonObject.entrySet()) {
                ProfileManager.profileIds[entry.key] = entry.value.asString
            }
        }
    }

    fun reloadDataFile() {
        ChatUtils.sendClientMessage("§7Reloading SBT Data..", shortPrefix = true)
        dataJson = loadDataFromFile(dataFile)

        ChatUtils.sendClientMessage("§aSaved SBT Data!", shortPrefix = true)
        saveDataToFile(dataFile, dataJson)
    }

    fun loadDataFromFile(saveDataFilePath: File): JsonObject {
        try {
            val jsonContent = String(Files.readAllBytes(Paths.get(saveDataFilePath.path)), Charsets.UTF_8)
            return JsonParser().parse(jsonContent).asJsonObject
        } catch (e: java.nio.file.NoSuchFileException) {
            println("File not found: ${saveDataFilePath.path}")
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JsonSyntaxException) {
            println("Invalid JSON syntax in file: ${saveDataFilePath.path}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return JsonObject()
    }

    fun saveData(dataName: String?, dataValue: Any) {
        dataJson.add(dataName, convertToJsonObject(dataValue))
        saveDataToFile(dataFile, dataJson)
    }

    fun getData(dataName: String?): Any? {
        if (dataJson.has(dataName)) return convertFromJsonElement(dataJson[dataName])
        return null
    }

    fun getDataDefault(dataName: String, obj: Any?): Any? {
        return getData(dataName) ?: obj
    }

    // Works with data names such as "subset1.list.option2" or even just "option2"
    fun saveProfileData(dataName: String, dataValue: Any) {
        val currentProfileId = ProfileManager.getCurrentProfileId()
        if(currentProfileId == null) {
            if(CustomizationConfig.developerMode) ChatUtils.sendClientMessage("Profile ID not found, please try again.")
            ProfileManager.sendProfileIdCommand()
            return
        }
        var profileJson = dataJson.getAsJsonObject(currentProfileId)
        if (profileJson == null) {
            profileJson = JsonObject()
            dataJson.add(currentProfileId, profileJson)
        }
        val parts = dataName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in 0 until parts.size - 1) {
            if (!profileJson!!.has(parts[i]) || !profileJson[parts[i]].isJsonObject) {
                profileJson.add(parts[i], JsonObject())
            }
            profileJson = profileJson.getAsJsonObject(parts[i])
        }
        profileJson.add(parts[parts.size - 1], convertToJsonObject(dataValue))
        saveDataToFile(dataFile, dataJson)
    }

    fun saveDataToFile(savePath: File, newData: JsonObject) {
        try {
            // Ensure the parent directories exist
            savePath.parentFile?.mkdirs()
            // Ensure the file exists
            if (!savePath.exists()) {
                savePath.createNewFile()
            }

            FileOutputStream(savePath).bufferedWriter(Charsets.UTF_8).use { writer ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(newData)
                writer.write(jsonString)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun convertToJsonObject(dataValue: Any): JsonElement {
        return when (dataValue) {
            is String -> {
                JsonPrimitive(dataValue)
            }

            is Number -> {
                JsonPrimitive(dataValue)
            }

            is Boolean -> {
                JsonPrimitive(dataValue)
            }

            else -> {
                // Handle other types as needed
                Gson().toJsonTree(dataValue)
            }
        }
    }

    fun getProfileDataDefault(dataName: String, obj: Any?): Any? {
        return getProfileData(dataName) ?: obj
    }

    private fun getProfileData(dataName: String): Any? {
        val currentProfileId = ProfileManager.getCurrentProfileId() ?: return null

        var profileJson = dataJson.getAsJsonObject(currentProfileId) ?: return null
        val parts = dataName.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        for (i in 0 until parts.size - 1) {
            val element = profileJson[parts[i]]
            profileJson = if (element != null && element.isJsonObject) {
                element.getAsJsonObject()
            } else {
                return null
            }
        }
        val lastElement = profileJson[parts[parts.size - 1]]
        return if (lastElement != null) {
            convertFromJsonElement(lastElement)
        } else {
            null
        }
    }

    private fun convertFromJsonElement(jsonElement: JsonElement): Any? {
        return if (jsonElement.isJsonPrimitive) {
            if (jsonElement.getAsJsonPrimitive().isBoolean) {
                jsonElement.getAsJsonPrimitive().asBoolean
            } else if (jsonElement.getAsJsonPrimitive().isNumber) {
                val str = jsonElement.getAsJsonPrimitive().asString
                if (str.contains(".")) {
                    return jsonElement.getAsJsonPrimitive().asNumber.toDouble()
                }
                if (str.length > 10) {
                    jsonElement.getAsJsonPrimitive().asNumber.toLong()
                } else jsonElement.getAsJsonPrimitive().asNumber.toInt()
            } else {
                jsonElement.getAsJsonPrimitive().asString
            }
        } else if (jsonElement.isJsonArray) {
            jsonElement.getAsJsonArray()
        } else if (jsonElement.isJsonObject) {
            jsonElement.getAsJsonObject()
        } else {
            null // Unsupported JSON element type
        }
    }
}

