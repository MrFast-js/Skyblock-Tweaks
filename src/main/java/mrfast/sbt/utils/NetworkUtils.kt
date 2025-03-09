package mrfast.sbt.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import moe.nea.libautoupdate.UpdateUtils
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.DataManager
import net.minecraft.util.ChatComponentText
import org.apache.http.HttpEntity
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.KeyStore
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.net.ssl.*


object NetworkUtils {
    private val myApiUrl = DeveloperConfig.modAPIURL
    private var client: CloseableHttpClient = HttpClients.createDefault()
    private val jsonCache: MutableMap<String, CacheObject> = HashMap()
    private const val zipUrl = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-Repo/archive/master.zip"
    var tempApiAuthKey = ""

    // Follow these directions
    // https://moddev.nea.moe/https/#false-hope
    init {
        try {
            val myKeyStore = KeyStore.getInstance("JKS")

            // Load the keystore from the resources folder
            myKeyStore.load(NetworkUtils.javaClass.getResourceAsStream("/mykeystore.jks"), "changeit".toCharArray())

            // Initialize KeyManagerFactory and TrustManagerFactory with the keystore
            val kmf: KeyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
            val tmf: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            kmf.init(myKeyStore, null)
            tmf.init(myKeyStore)

            // Use SSLConnectionSocketFactory which implements LayeredConnectionSocketFactory
            val ctx = SSLContext.getInstance("TLS");
            ctx!!.init(kmf.keyManagers, tmf.trustManagers, null);

            // Build the HttpClient with the custom SSL socket factory
            val sslSocketFactory = SSLConnectionSocketFactory(ctx)
            client = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .setUserAgent("Skyblock-Tweaks")
                .build()

            UpdateUtils.patchConnection {
                if (it is HttpsURLConnection) it.sslSocketFactory = ctx.socketFactory
            }
        } catch (e: Exception) {
            println("Failed to load keystore. A lot of API requests won't work")
            e.printStackTrace()
        }
    }

    data class CacheObject(val url: String, val response: JsonObject, val createdAt: Long = System.currentTimeMillis())

    fun apiRequestAndParse(
        url: String, headers: List<String> = listOf(), caching: Boolean = true, useProxy: Boolean = true
    ): JsonObject {
        var modifiedUrlString = url
        if (url.contains("api.hypixel.net") && useProxy) {
            modifiedUrlString = url.replace("https://api.hypixel.net", myApiUrl + "аpi")
        }
        val isMyApi = modifiedUrlString.contains(myApiUrl)

        if (CustomizationConfig.developerMode && DeveloperConfig.logNetworkRequests) {
            val message = if (modifiedUrlString.contains("#")) {
                val urlString = modifiedUrlString.split("#")[0]
                val reason = modifiedUrlString.split("#")[1]
                "Sending request to $urlString Reason: $reason"
            } else {
                "Sending request to $modifiedUrlString"
            }
            println(message)
        }

        if (jsonCache.containsKey(modifiedUrlString) && caching) {
            val obj = jsonCache[modifiedUrlString]
            if ((System.currentTimeMillis() - obj!!.createdAt) < 1000 * 60 * 5) {
                if (CustomizationConfig.developerMode && DeveloperConfig.logNetworkRequests) println("Using Cache For: $modifiedUrlString")
                return obj.response
            }
        }

        val player = Utils.mc.thePlayer

        try {
            val request = HttpGet(URL(modifiedUrlString).toURI())
            request.protocolVersion = HttpVersion.HTTP_1_1

            for (header in headers) {
                val (name, value) = header.split("=")
                request.setHeader(name, value)
            }

            if (isMyApi) {
                if (tempApiAuthKey.isNotEmpty()) {
                    request.setHeader("temp-auth-key", tempApiAuthKey)
                }
                val nearby = Utils.mc.theWorld
                    .playerEntities
                    .stream()
                    .map { e -> e.uniqueID.toString() }
                    .limit(20)
                    .collect(Collectors.toList())

                request.setHeader("x-players", nearby.toString())
                request.setHeader("x-request-author", Utils.mc.thePlayer.toString())
                request.setHeader("x-version", SkyblockTweaks.MOD_VERSION)
            }


            client.execute(request).use { response ->
                val entity: HttpEntity = response.entity
                val statusCode = response.statusLine.statusCode

                BufferedReader(InputStreamReader(entity.content, StandardCharsets.UTF_8)).use { inStream ->
                    val parsedJson = Gson().fromJson(inStream, JsonObject::class.java)

                    if (isMyApi) {
                        if (parsedJson.has("auth-key")) {
                            tempApiAuthKey = parsedJson.get("auth-key").asString
                            if (DeveloperConfig.logNetworkRequests) {
                                println("GOT SBT AUTH KEY $tempApiAuthKey")
                            }
                            return apiRequestAndParse(modifiedUrlString, headers, caching, useProxy)
                        }
                        if (statusCode != 200) {
                            if (DeveloperConfig.showServerErrors && player != null) {
                                ChatUtils.sendClientMessage(
                                    "§cServer Error: ${parsedJson.get("cause").asString} §e§o${parsedJson.get("err_code")} $modifiedUrlString",
                                    true
                                )
                            }
                            return JsonObject()
                        }
                    }
                    val cache = CacheObject(modifiedUrlString, parsedJson)
                    jsonCache[modifiedUrlString] = cache

                    return parsedJson
                }
            }
        } catch (ex: SSLHandshakeException) {
            ex.printStackTrace()
            if (DeveloperConfig.showServerErrors && player != null) {
                player.addChatMessage(ChatComponentText("§cSSL Handshake Exception! §cThis API request has been blocked by your network! §e§o$modifiedUrlString"))
            }
        } catch (ex: Exception) {
            if (DeveloperConfig.showServerErrors && player != null) {
                player.addChatMessage(ChatComponentText("§cEncountered Exception when connecting to §e§o$modifiedUrlString"))
            }
            ex.printStackTrace()
        }
        return JsonObject()
    }

    // Currently untested
    fun postJsonObjectToApi(apiUrl: String, jsonObject: JsonObject): JsonObject? {
        try {
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection

            // Set up the HTTP request method
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            // Write the JSON data to the request
            val outputStream = connection.outputStream
            outputStream.write(jsonObject.toString().toByteArray(StandardCharsets.UTF_8))
            outputStream.flush()
            outputStream.close()

            // Get the response code
            val responseCode = connection.responseCode

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response
                BufferedReader(InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)).use { inStream ->
                    val gson = Gson()
                    return gson.fromJson(inStream, JsonObject::class.java)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private var latestProfileCache = HashMap<String, JsonObject>()

    fun getActiveProfile(playerUUID: String): JsonObject? {
        if (latestProfileCache.containsKey(playerUUID)) return latestProfileCache[playerUUID]

        val apiUrl = "https://api.hypixel.net/skyblock/profiles?uuid=$playerUUID"
        val profiles = apiRequestAndParse(apiUrl).getAsJsonArray("profiles")
        val active = profiles.firstOrNull { it.asJsonObject["selected"].asBoolean }?.asJsonObject
            ?: profiles.firstOrNull()?.asJsonObject

        latestProfileCache[playerUUID] = active!!

        return active
    }


    private val nameCache = mutableMapOf<String, String>()

    fun getUUID(username: String, formatted: Boolean = false): String? {
        nameCache.entries.find { it.value.equals(username, ignoreCase = true) }?.let {
            return if (formatted) formatUUID(it.key) else it.key
        }

        try {
            val uuidResponse = apiRequestAndParse("https://api.mojang.com/users/profiles/minecraft/$username")
            val uuid = uuidResponse["id"].asString
            val name = uuidResponse["name"].asString.lowercase()
            nameCache[uuid] = name
            return if (formatted) formatUUID(uuid) else uuid
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun formatUUID(input: String): String {
        return input.replace(Regex("(.{8})(.{4})(.{4})(.{4})(.{12})"), "$1-$2-$3-$4-$5")
    }


    var NeuItems = JsonObject()
    private var NeuMobs = JsonObject()
    private var NeuConstants = JsonObject()

    fun downloadAndProcessRepo() {
        val etagFile = ConfigManager.modDirectoryPath.resolve("repo/NEUAPI-ETAG.txt")

        // Ensure the ETag file exists, create if not
        if (!etagFile.exists()) {
            etagFile.parentFile?.mkdirs()
            if (!etagFile.exists()) {
                etagFile.createNewFile()
            }
        }

        // Read previous ETag from file
        val previousEtag = String(Files.readAllBytes(etagFile.toPath()), Charsets.UTF_8)

        // Get current ETag from server
        val currentEtag = client.execute(HttpHead(zipUrl).apply {
            previousEtag.takeIf { it.isNotEmpty() }?.let { setHeader("If-None-Match", it) }
        }).takeIf { it.statusLine.statusCode != 304 }?.getFirstHeader("ETag")?.value

        // If ETag is different, download and process the ZIP file
        if (currentEtag != previousEtag) {
            client.execute(HttpGet(zipUrl))
                .takeIf { it.statusLine.statusCode == 200 }?.entity?.content?.use { zipStream ->
                    ZipInputStream(zipStream).use { zip ->
                        var entry: ZipEntry? = zip.nextEntry
                        while (entry != null) {
                            if (entry.name.endsWith(".json")) {
                                val jsonContent = zip.bufferedReader().readText()
                                val name = entry.name.split("/").last().removeSuffix(".json")
                                val value = JsonParser().parse(jsonContent).asJsonObject
                                when {
                                    entry.name.contains("/items/") -> {
                                        NeuItems.add(name, value)
                                    }

                                    entry.name.contains("/mobs/") -> {
                                        NeuMobs.add(name, value)
                                    }

                                    entry.name.contains("/constants/") -> {
                                        NeuConstants.add(name, value)
                                    }
                                }
                            }
                            entry = zip.nextEntry
                        }
                    }
                }
            // Save the new ETag to file
            currentEtag?.let {
                Files.write(etagFile.toPath(), it.toByteArray(StandardCharsets.UTF_8))
            }
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuItems.json"), NeuItems)
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuMobs.json"), NeuMobs)
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuConstants.json"), NeuConstants)
        } else {
            println("Debug: ETag matches. No need to download. Loading from file...")
            NeuItems = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuItems.json"))
            NeuMobs = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuMobs.json"))
            NeuConstants = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuConstants.json"))
        }
    }
}