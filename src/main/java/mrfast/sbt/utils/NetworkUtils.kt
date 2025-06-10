package mrfast.sbt.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import moe.nea.libautoupdate.UpdateUtils
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.DataManager
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.StringReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.security.KeyStore
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException

object NetworkUtils {
    private val sbtApiURL = DeveloperConfig.modAPIURL
    private var client: CloseableHttpClient = HttpClients.createDefault()
    private val jsonCache: MutableMap<String, CacheObject> = HashMap()
    private const val zipUrl = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-Repo/archive/master.zip"
    var tempApiAuthKey = ""

    private var okHttpClient = OkHttpClient.Builder()
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

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

            val sslSocketFactory = ctx.socketFactory
            val trustManager = tmf.trustManagers[0] as X509TrustManager

            okHttpClient = OkHttpClient.Builder()
                .callTimeout(10, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory, trustManager)
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
    private val gson = Gson()

    suspend fun apiRequestAndParse(
        url: String, headers: MutableMap<String, String> = mutableMapOf(), caching: Boolean = true, useProxy: Boolean = true
    ): JsonObject {
        headers["user-agent"] = "Skyblock-Tweaks"

        var modifiedUrlString = url
        if (url.contains("api.hypixel.net") && useProxy) {
            modifiedUrlString = url.replace("https://api.hypixel.net", sbtApiURL + "аpi")
        }
        val usingSBTAPI = modifiedUrlString.contains(sbtApiURL)

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

        val player = Utils.getPlayer()!!

        if (usingSBTAPI) {
            if (tempApiAuthKey.isNotEmpty()) {
                headers["temp-auth-key"] = tempApiAuthKey
            }
            val nearby = Utils.getWorld()
                .playerEntities
                .toList()
                .stream()
                .map { e -> e.uniqueID.toString() }
                .limit(20)
                .collect(Collectors.toList())

            headers["x-players"] = nearby.toString()
            headers["x-request-author"] = Utils.getPlayer()!!.toString()
            headers["x-version"] = SkyblockTweaks.MOD_VERSION
        }


        val requestBuilder = Request.Builder().url(modifiedUrlString)
        headers.forEach { (key, value) -> requestBuilder.addHeader(key, value) }

        val request = requestBuilder.build()

        return withContext(Dispatchers.IO) {
            val call = okHttpClient.newCall(request)
            try {
                val response = call.execute()

                response.use {
                    val responseBody = response.body.string()
                    val json = try {
                        val reader = JsonReader(StringReader(responseBody))
                        reader.isLenient = true
                        gson.fromJson(reader, JsonObject::class.java)
                    } catch (e: Exception) {
                        JsonObject()
                    }

                    if (usingSBTAPI) {
                        if (json.has("auth-key")) {
                            tempApiAuthKey = json.get("auth-key").asString
                            if (DeveloperConfig.logNetworkRequests) {
                                println("GOT SBT AUTH KEY $tempApiAuthKey")
                            }
                            return@withContext apiRequestAndParse(modifiedUrlString, headers, caching, useProxy)
                        }
                        if (it.code != 200) {
                            if (DeveloperConfig.showServerErrors && player != null) {
                                ChatUtils.sendClientMessage(
                                    "§cServer Error: ${json.get("cause").asString} §e§o${json.get("err_code")} $modifiedUrlString",
                                    true
                                )
                            }
                            return@withContext JsonObject()
                        }
                    }

                    if (caching) {
                        val cache = CacheObject(modifiedUrlString, json)
                        jsonCache[modifiedUrlString] = cache
                    }

                    return@withContext json
                }
            } catch (e: SSLHandshakeException) {
                if(usingSBTAPI) {
                    ChatUtils.sendPlayerMessage("§cSSL Handshake Exception with SBT API! Contact MrFast on Discord!")
                }
                e.printStackTrace()
                return@withContext JsonObject()
            }
        }
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

    suspend fun getActiveProfile(playerUUID: String): JsonObject? {
        if (latestProfileCache.containsKey(playerUUID)) return latestProfileCache[playerUUID]

        val apiUrl = "https://api.hypixel.net/skyblock/profiles?uuid=$playerUUID"
        val profiles = apiRequestAndParse(apiUrl).getAsJsonArray("profiles")
        val active = profiles.firstOrNull { it.asJsonObject["selected"].asBoolean }?.asJsonObject
            ?: profiles.firstOrNull()?.asJsonObject

        latestProfileCache[playerUUID] = active!!

        return active
    }


    private val nameCache = mutableMapOf<String, String>()

    suspend fun getUUID(username: String, formatted: Boolean = false): String? {
        nameCache.entries.find { it.value.equals(username, ignoreCase = true) }?.let {
            return if (formatted) formatUUID(it.key) else it.key
        }

        var output: String?=null
        val uuidResponse = apiRequestAndParse("https://api.mojang.com/users/profiles/minecraft/$username")

        if(uuidResponse.has("id")) {
            val uuid = uuidResponse["id"].asString
            val name = uuidResponse["name"].asString.lowercase()
            nameCache[uuid] = name
            output = if (formatted) formatUUID(uuid) else uuid
        }

        return output
    }

    private fun formatUUID(input: String): String {
        return input.replace(Regex("(.{8})(.{4})(.{4})(.{4})(.{12})"), "$1-$2-$3-$4-$5")
    }


    var NeuItems = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuItems.json"))
    private var NeuMobs = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuMobs.json"))
    var NeuConstants = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuConstants.json"))

    fun downloadAndProcessRepo(force: Boolean = false) {
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
        val request = HttpHead(zipUrl).apply {
            if (previousEtag.isNotEmpty()) {
                setHeader("If-None-Match", previousEtag)
            }
        }

        val response = client.execute(request)
        val matchesLastETag = response.statusLine.statusCode == 304

        // If ETag is different, download and process the ZIP file
        if (!matchesLastETag || force) {
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

            val currentEtag = response.getFirstHeader("ETag")?.value
            // Save the new ETag to file
            currentEtag?.let {
                Files.write(etagFile.toPath(), it.toByteArray(StandardCharsets.UTF_8))
            }
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuItems.json"), NeuItems)
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuMobs.json"), NeuMobs)
            DataManager.saveDataToFile(ConfigManager.modDirectoryPath.resolve("repo/NeuConstants.json"), NeuConstants)
        } else {
            if(CustomizationConfig.developerMode) println("Debug: ETag matches. No need to download. Loading from file...")
            NeuItems = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuItems.json"))
            NeuMobs = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuMobs.json"))
            NeuConstants = DataManager.loadDataFromFile(ConfigManager.modDirectoryPath.resolve("repo/NeuConstants.json"))

            if(NeuItems.entrySet().size == 0 || NeuMobs.entrySet().size == 0 || NeuConstants.entrySet().size == 0) {
                println("Failed to load NEU API data from file. Redownloading...")
                downloadAndProcessRepo(true)
            }
        }
    }
}