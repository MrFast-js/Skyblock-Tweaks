package mrfast.sbt.managers

import com.google.gson.*
import mrfast.sbt.config.ConfigManager
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.regex.Pattern


object DataManager {
    private val saveDataFilePath = ConfigManager.modDirectoryPath.resolve("profilesData.json")
    private var pfidSentInChat = false

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load?) {
        pfidSentInChat = false
        // Use api request to get profile id because no Profile id message is sent because only one profile
        Utils.setTimeout({
            if (!pfidSentInChat) {
                pfidSentInChat = true
                if (currentProfileId == null) {
                    currentProfileId = NetworkUtils.getActiveProfileId(Utils.mc.thePlayer.uniqueID.toString())
                    dataJson.addProperty("currentProfileId", currentProfileId)
                    saveDataToFile()
                    MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
                }
                MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
            }
        }, 7000)
    }


    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val regexPattern = "Profile ID: (\\S+)"
        val pattern = Pattern.compile(regexPattern)
        val matcher = pattern.matcher(event.message.unformattedText)
        if (matcher.find()) {
            pfidSentInChat = true
            // Don't update if it's the same
            if (currentProfileId != null && currentProfileId == matcher.group(1)) return
            currentProfileId = matcher.group(1)
            dataJson.addProperty("currentProfileId", currentProfileId)
            saveDataToFile()
            MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
        }
    }

    private var dataFile: File = saveDataFilePath
    private var dataJson = JsonObject()
    private var currentProfileId: String? = null

    init {
        loadDataFromFile()
        if (dataJson.has("currentProfileId")) {
            currentProfileId = dataJson["currentProfileId"].asString
        }
        MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
    }

    private fun loadDataFromFile() {
        try {
            val jsonContent = String(Files.readAllBytes(Paths.get(dataFile.path)))
            dataJson = JsonParser().parse(jsonContent).getAsJsonObject()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveData(dataName: String?, dataValue: Any) {
        dataJson.add(dataName, convertToJsonObject(dataValue))
        saveDataToFile()
    }

    fun getData(dataName: String?): Any? {
        return convertFromJsonElement(dataJson[dataName])
    }

    // Works with data names such as "subset1.list.option2" or even just "option2"
    fun saveProfileData(dataName: String, dataValue: Any) {
        if (currentProfileId == null) return
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
        profileJson!!.add(parts[parts.size - 1], convertToJsonObject(dataValue))
        saveDataToFile()
    }

    private fun saveDataToFile() {
        try {
            FileWriter(dataFile).use { writer ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                gson.toJson(dataJson, writer)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun convertToJsonObject(dataValue: Any): JsonElement {
        return when (dataValue) {
            is String -> {
                JsonPrimitive(dataValue.toString())
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
        return if (getProfileData(dataName) == null) {
            obj
        } else getProfileData(dataName)
    }

    fun getProfileData(dataName: String): Any? {
        var profileJson = dataJson.getAsJsonObject(currentProfileId)
            ?: return null
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

