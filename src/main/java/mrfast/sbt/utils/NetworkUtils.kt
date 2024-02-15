package mrfast.sbt.utils

import com.google.gson.Gson
import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import net.minecraft.util.ChatComponentText
import org.apache.http.HttpEntity
import org.apache.http.HttpVersion
import org.apache.http.client.methods.HttpGet
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.SSLContextBuilder
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.cert.X509Certificate
import java.util.stream.Collectors
import javax.net.ssl.SSLHandshakeException

object NetworkUtils {
    private val myApiUrl = DeveloperConfig.modAPIURL
    private var client: CloseableHttpClient = HttpClients.createDefault()
    private val jsonCache: MutableMap<String, CacheObject> = HashMap()
    private var tempApiAuthKey = ""

    init {
        val builder = try {
            SSLContextBuilder().loadTrustMaterial(null) { _: Array<X509Certificate?>?, _: String? -> true }
        } catch (ex: Exception) {
            throw RuntimeException(ex)
        }
        val sslcsf = try {
            SSLConnectionSocketFactory(builder.build())
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        client = HttpClients.custom().setSSLSocketFactory(sslcsf).setUserAgent("Mozilla/5.0").build()
        println("CREATED CUSTOM CLIENT")
    }

    data class CacheObject(val url: String, val response: JsonObject, val createdAt: Long = System.currentTimeMillis())

    fun apiRequestAndParse(
        urlString: String,
        headers: List<String> = listOf(),
        caching: Boolean = true,
        useProxy: Boolean = true
    ): JsonObject {
        var modifiedUrlString = urlString
        if (urlString.contains("api.hypixel.net") && useProxy) {
            modifiedUrlString = urlString.replace("https://api.hypixel.net", myApiUrl + "аpi")
        }
        val isMyApi = modifiedUrlString.contains(myApiUrl)

        if (CustomizationConfig.developerMode) {
            val message = if (modifiedUrlString.contains("#")) {
                val url = modifiedUrlString.split("#")[0]
                val reason = modifiedUrlString.split("#")[1]
                "Sending request to $url Reason: $reason"
            } else {
                "Sending request to $modifiedUrlString"
            }
            println(message)
        }

        if (jsonCache.containsKey(modifiedUrlString) && caching) {
            val obj = jsonCache[modifiedUrlString]
            if ((System.currentTimeMillis() - obj!!.createdAt) < 1000 * 60 * 5) {
                if (CustomizationConfig.developerMode) println("Using Cache For: $modifiedUrlString")
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

                val nearby = Utils.mc.theWorld.playerEntities.stream()
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
                            println("GOT AUTH KEY $tempApiAuthKey")
                            return apiRequestAndParse(modifiedUrlString, headers, caching, useProxy)
                        }
                        if (statusCode != 200) {
                            ChatUtils.sendClientMessage(
                                "§cServer Error: ${parsedJson.get("cause").asString} §e§o${parsedJson.get("err_code")} $modifiedUrlString"
                            )
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
            player.addChatMessage(ChatComponentText("§cThis API request has been blocked by your network! $modifiedUrlString"))
        } catch (ex: Exception) {
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

    private var latestProfileCache = HashMap<String, String>()
    fun getActiveProfile(playerUUID: String): String? {
        latestProfileCache[playerUUID]?.let { cachedProfile ->
            return cachedProfile
        }

        val apiUrl = "https://api.hypixel.net/skyblock/profiles?uuid=$playerUUID"
        val profiles = apiRequestAndParse(apiUrl).getAsJsonArray("profiles")

        return profiles
            .firstOrNull { it.asJsonObject.get("selected").asBoolean }
            ?.asJsonObject?.get("profile_id")?.asString
            ?: profiles.firstOrNull()?.asJsonObject?.get("profile_id")?.asString
    }
}