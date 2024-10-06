package mrfast.sbt.apis

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import java.util.*

@SkyblockTweaks.EventComponent
object ItemApi {
    private var skyblockItems = JsonObject()
    private var skyblockItemPrices = JsonObject()
    private var skyblockItemsLoaded = false

    init {
        println("Loading Skyblock Items from HySky API")

        // Update Item Prices every ~15 Minutes
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                loadSkyblockItems(false)
            }
        }, 0, 1000 * 60 * 15)
    }

    private fun loadSkyblockItems(logging: Boolean) {
        Thread {
            if(!skyblockItemsLoaded) {
                val items = NetworkUtils.apiRequestAndParse("https://hysky.de/api/items")
                items.remove("timestamp")
                skyblockItems = items
            }
            if (skyblockItems.entrySet().size > 0) {
                skyblockItemsLoaded = true
                if (logging) println("Loaded Skyblock Items from HySky API!!")

                if (logging) println("Loading Lowest Bin Prices from Moulberry NEU API")
                try {
                    val lowestBins =
                        NetworkUtils.apiRequestAndParse("https://moulberry.codes/lowestbin.json", caching = false)
                    if (lowestBins.entrySet().size > 0) {
                        lowestBins.entrySet().forEach {
                            if (!skyblockItemPrices.has(it.key)) {
                                skyblockItemPrices.add(it.key, JsonObject())
                            }
                            skyblockItemPrices[it.key].asJsonObject.addProperty("lowestBin", it.value.asLong)
                            skyblockItemPrices[it.key].asJsonObject.addProperty("basePrice", it.value.asLong)
                        }
                        if (logging) println("Loaded Lowest Bin Prices from Moulberry NEU API!!")

                        if (logging) println("Loading Average Bin Prices from Moulberry NEU API")
                        val averageBins =
                            NetworkUtils.apiRequestAndParse(
                                "https://moulberry.codes/auction_averages/3day.json",
                                caching = false
                            )
                        if (averageBins.entrySet().size > 0) {
                            averageBins.entrySet().forEach {
                                if (!skyblockItemPrices.has(it.key)) {
                                    skyblockItemPrices.add(it.key, JsonObject())
                                }

                                val item = skyblockItemPrices[it.key].asJsonObject
                                it.value.asJsonObject.entrySet().forEach { priceStat ->
                                    item.add(priceStat.key + "_avg", priceStat.value)
                                }

                                if (item.has("price_avg")) {
                                    item.addProperty(
                                        "basePrice",
                                        item["price_avg"].asLong
                                    )
                                }

                                // Change 'worth' depending on available prices and anti market manipulations math, requires lbin & abin
                                if (skyblockItemPrices[it.key].asJsonObject.has("lowestBin") && skyblockItemPrices[
                                        it.key
                                    ].asJsonObject.has("price_avg")
                                ) {
                                    val lbin = item["lowestBin"].asDouble
                                    var abin = item["price_avg"].asDouble
                                    if (item.has("clean_price_avg")) {
                                        abin = item["clean_price_avg"].asDouble
                                    }

                                    var basePrice = (abin * 0.4 + lbin * 0.6)

                                    // If the lowest bin is more than average bin * 50% it can be inferred that likely the lowest bin is currently high or possibly manipulated
                                    // Find a semi middle ground that is fair, but much more weighted towards lbin for selling faster
                                    if (lbin > abin + 300_000 || lbin > abin * 1.5) {
                                        basePrice = (abin * 0.6 + lbin * 0.4)
                                    }
                                    if (lbin > abin + 500_00) {
                                        basePrice = lbin - 100_000
                                    }

                                    item.addProperty("basePrice", basePrice.toLong())
                                }
                            }

                            if (logging) println("Loaded Average Bin Prices from Moulberry NEU API!!")
                        }
                    }
                } catch (e: Exception) {
                    println("There was a problem loading NEU Prices.. Retrying in 5 seconds..")
                    Utils.setTimeout({
                        loadSkyblockItems(logging)
                    }, 5000)
                    return@Thread
                }

                if (logging) println("Loading bazaar prices from hypixel api")
                val bzPrices = NetworkUtils.apiRequestAndParse(
                    "https://api.hypixel.net/skyblock/bazaar",
                    caching = true,
                    useProxy = false
                )
                if (bzPrices.entrySet().size > 0) {
                    val bzItems = bzPrices["products"].asJsonObject
                    for (product in bzItems.entrySet()) {
                        val quickStats = product.value.asJsonObject["quick_status"].asJsonObject
                        val sellPrice = quickStats["sellPrice"].asDouble
                        val buyPrice = quickStats["buyPrice"].asDouble

                        val productJson = JsonObject()
                        productJson.addProperty("sellPrice", sellPrice)
                        productJson.addProperty("buyPrice", buyPrice)
                        productJson.addProperty("basePrice", sellPrice)

                        skyblockItemPrices.add(product.key, productJson)
                    }
                }
            }
        }.start()
    }

    fun getItemIdFromName(displayName: String, ignoreFormatting: Boolean? = false): String? {
        return skyblockItems.entrySet().find { entry ->
            val itemName = entry.value.asJsonObject["displayname"].asString
            val cleanedItemName = if (ignoreFormatting == true) itemName?.clean() else itemName
            val cleanedDisplayName = if (ignoreFormatting == true) displayName.clean() else displayName
            cleanedItemName == cleanedDisplayName
        }?.key
    }

    private val itemStackCache = mutableMapOf<String, ItemStack>()
    fun createItemStack(itemId: String): ItemStack? {
        if (itemStackCache.contains(itemId)) return itemStackCache[itemId]

        // Use coin talisman as texture for skyblock_coin
        if (itemId == "SKYBLOCK_COIN") {
            val itemJson = skyblockItems["COIN_TALISMAN"]?.asJsonObject ?: return null
            var nbtString = itemJson["nbttag"]?.asString ?: return null
            val mcItemId = itemJson["itemid"]?.asString ?: return null
            nbtString = nbtString.replace(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUyZGNhNjhjOGY4YWY1MzNmYjczN2ZhZWVhY2JlNzE3Yjk2ODc2N2ZjMTg4MjRkYzJkMzdhYzc4OWZjNzcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0="
            )
            nbtString =
                nbtString.replace("ff6fbcd7-5138-36b9-a3dc-4c52af38c20d", "2070f6cb-f5db-367a-acd0-64d39a7e5d1b")
            nbtString = nbtString.replace("COIN_TALISMAN", "")

            // Parse NBT string
            val nbtCompound = try {
                JsonToNBT.getTagFromJson(nbtString)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            // Create ItemStack with NBT and item ID
            val itemStack = ItemStack(Item.getByNameOrId(mcItemId))
            itemStack.tagCompound = nbtCompound
            itemStack.setStackDisplayName("§6Skyblock Coins")

            if (itemJson.has("damage")) {
                itemStack.itemDamage = itemJson["damage"].asInt
            }

            itemStackCache[itemId] = itemStack
            return itemStack
        }

        if (itemId == "ACCESSORY_BAG") {
            val itemJson = skyblockItems["LARGE_DUNGEON_SACK"]?.asJsonObject ?: return null
            var nbtString = itemJson["nbttag"]?.asString ?: return null
            val mcItemId = itemJson["itemid"]?.asString ?: return null
            nbtString = nbtString.replace(
                "ewogICJ0aW1lc3RhbXAiIDogMTU5NzgxMDkwMzc1NCwKICAicHJvZmlsZUlkIiA6ICJmNjE1NzFmMjY1NzY0YWI5YmUxODcyMjZjMTEyYWEwYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJGZWxpeF9NYW5nZW5zZW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmI5NmM1ODVjY2QzNWYwNzNkYTM4ZDE2NWNiOWJiMThmZjEzNmYxYTE4NGVlZTNmNDQ3MjUzNTQ2NDBlYmJkNCIKICAgIH0KICB9Cn0",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTYxYTkxOGMwYzQ5YmE4ZDA1M2U1MjJjYjkxYWJjNzQ2ODkzNjdiNGQ4YWEwNmJmYzFiYTkxNTQ3MzA5ODVmZiJ9fX0"
            )
            nbtString =
                nbtString.replace("74dacf03-2bd5-3f50-a933-0a2ce640cdd9", "f7cab91e-3744-4982-815c-dfee179ee5f5")
            nbtString = nbtString.replace("LARGE_DUNGEON_SACK", "")

            // Parse NBT string
            val nbtCompound = try {
                JsonToNBT.getTagFromJson(nbtString)
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }

            // Create ItemStack with NBT and item ID
            val itemStack = ItemStack(Item.getByNameOrId(mcItemId))
            itemStack.tagCompound = nbtCompound
            itemStack.setStackDisplayName("§aAccessory Bag")

            if (itemJson.has("damage")) {
                itemStack.itemDamage = itemJson["damage"].asInt
            }

            itemStackCache[itemId] = itemStack
            return itemStack
        }

        // Assuming skyblockItems is a map containing NBT data as strings
        val itemJson = skyblockItems[itemId]?.asJsonObject ?: return null
        val nbtString = itemJson["nbttag"]?.asString ?: return null
        val mcItemId = itemJson["itemid"]?.asString ?: return null

        // Parse NBT string
        val nbtCompound = try {
            JsonToNBT.getTagFromJson(nbtString)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        // Create ItemStack with NBT and item ID
        val itemStack = ItemStack(Item.getByNameOrId(mcItemId))
        itemStack.tagCompound = nbtCompound

        if (itemJson.has("damage")) {
            itemStack.itemDamage = itemJson["damage"].asInt
        }

        itemStackCache[itemId] = itemStack
        return itemStack
    }

    fun getItemPriceInfo(itemId: String): JsonObject? {
        if (!skyblockItemPrices.has(itemId) && itemId.contains(";")) {
            // REJUVENATE;1  -> ENCHANTMENT_REJUVENATE_1
            return getItemPriceInfo("ENCHANTMENT_${itemId.replace(";", "_")}")
        }

        return skyblockItemPrices[itemId]?.asJsonObject
    }

    fun getItemInfo(itemId: String): JsonObject? = skyblockItems[itemId]?.asJsonObject

    fun getSkyblockItems(): JsonObject = skyblockItems

    fun getItemInfo(stack: ItemStack): JsonObject? = stack.getSkyblockId()?.let { skyblockItems[it].asJsonObject }
}