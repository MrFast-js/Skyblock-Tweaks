package mrfast.sbt.managers

import com.google.gson.*
import mrfast.sbt.config.ConfigManager
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


object DataManager {
    private val saveDataFilePath = ConfigManager.modDirectoryPath.resolve("profilesData.json")
    private var dataFile: File = saveDataFilePath
    private var dataJson = JsonObject()
    private var profileIds = mutableMapOf<String,String>() // UUID, PFID

    init {
        loadDataFromFile()
        if (dataJson.has("profileIds")) {
            // Load past profile ids from files
            for (entry in dataJson["profileIds"].asJsonObject.entrySet()) {
                profileIds[entry.key] = entry.value.asString
            }
        }
    }

    private var listenForProfileId = false
    private fun sendProfileIdCommand() {
        listenForProfileId = true
        ChatUtils.sendPlayerMessage("/profileid")
    }

    var sentInitialLoad = false

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        val clean = event.message.unformattedText.clean()
        val profileIdPattern = Regex("Profile ID: (\\S+)")
        val suggestPattern = Regex("CLICK THIS TO SUGGEST IT IN CHAT .*")
        val firstJoinPattern = Regex("Latest update: SkyBlock .*") // First startup, get profile id

        if (event.type.toInt() == 2) return

        if (clean.matches(firstJoinPattern)) {
            sendProfileIdCommand()
        }

        if (clean.matches(profileIdPattern)) {
            val groups = profileIdPattern.find(clean)?.groupValues ?: return

            if (groups.size >= 2) {
                val newProfileId = groups[1]
                val currentProfile = profileIds[Utils.mc.thePlayer.uniqueID.toString()]

                if (currentProfile == null || currentProfile != newProfileId || !sentInitialLoad) {
                    sentInitialLoad = true
                    MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
                }
                profileIds[Utils.mc.thePlayer.uniqueID.toString()] = newProfileId
                dataJson.add("profileIds", convertToJsonObject(profileIds))
                saveDataToFile()
            }
            if (listenForProfileId) event.isCanceled = true
        }

        if (clean.matches(suggestPattern) && listenForProfileId) {
            event.isCanceled = true
            if (clean.contains("[NO DASHES]")) {
                listenForProfileId = false
            }
        }
    }

    private fun loadDataFromFile() {
        try {
            val jsonContent = String(Files.readAllBytes(Paths.get(dataFile.path)), Charsets.UTF_8)
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
        if (dataJson.has(dataName)) return convertFromJsonElement(dataJson[dataName])
        return null
    }

    fun getDataDefault(dataName: String, obj: Any?): Any? {
        return getData(dataName) ?: obj
    }

    // Works with data names such as "subset1.list.option2" or even just "option2"
    fun saveProfileData(dataName: String, dataValue: Any) {
        val currentProfileId = profileIds[Utils.mc.thePlayer.uniqueID.toString()] ?: return
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
        saveDataToFile()
    }

    private fun saveDataToFile() {
        try {
            FileWriter(dataFile.path, false).use { writer ->
                val gson = GsonBuilder().setPrettyPrinting().create()
                val jsonString = gson.toJson(dataJson)
                val escapedJsonString = jsonString.replace("§", "\\u00A7") // Escape special characters
                writer.write(escapedJsonString)
            }
        } catch (e: IOException) {
            throw RuntimeException(e)
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

    fun getProfileData(dataName: String): Any? {
        val currentProfileId = profileIds[Utils.mc.thePlayer.uniqueID.toString()]

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

