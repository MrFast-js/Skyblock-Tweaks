package mrfast.sbt.apis

import com.google.gson.JsonObject
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT

object ItemApi {
    private var skyblockItems = JsonObject()
    private var skyblockItemPrices = JsonObject()
    var skyblockItemsLoaded = false

    init {
        println("Loading Skyblock Items from HySky API")
        loadSkyblockItems()
    }

    private fun loadSkyblockItems() {
        Thread {
            val items = NetworkUtils.apiRequestAndParse("https://hysky.de/api/items")
            items.remove("timestamp")
            skyblockItems = items
            if (items.entrySet().size > 0) {
                skyblockItemsLoaded = true
                println("Loaded Skyblock Items from HySky API!!")

                println("Loading Lowest Bin Prices from Moulberry NEU API")
                val lowestBins = NetworkUtils.apiRequestAndParse("https://moulberry.codes/lowestbin.json")
                if (lowestBins.entrySet().size > 0) {
                    lowestBins.entrySet().forEach {
                        if (!skyblockItemPrices.has(it.key)) {
                            skyblockItemPrices.add(it.key, JsonObject())
                        }
                        skyblockItemPrices.get(it.key).asJsonObject.addProperty("lowestBin", it.value.asLong)
                        skyblockItemPrices.get(it.key).asJsonObject.addProperty("worth", it.value.asLong)
                    }
                    println("Loaded Lowest Bin Prices from Moulberry NEU API!!")

                    println("Loading Average Bin Prices from Moulberry NEU API")
                    val averageBins =
                        NetworkUtils.apiRequestAndParse("https://moulberry.codes/auction_averages/3day.json")
                    if (averageBins.entrySet().size > 0) {
                        averageBins.entrySet().forEach {
                            if (!skyblockItemPrices.has(it.key)) {
                                skyblockItemPrices.add(it.key, JsonObject())
                            }
                            val item = skyblockItemPrices.get(it.key).asJsonObject
                            it.value.asJsonObject.entrySet().forEach { priceStat ->
                                item.add(priceStat.key+"_avg", priceStat.value)
                            }
                        }

                        println("Loaded Average Bin Prices from Moulberry NEU API!!")
                    }
                }

                println("Loading bazaar prices from hypixel api")
                val bzPrices = NetworkUtils.apiRequestAndParse(
                    "https://api.hypixel.net/skyblock/bazaar",
                    caching = true,
                    useProxy = false
                )
                if (bzPrices.entrySet().size > 0) {
                    val bzItems = bzPrices.get("products").asJsonObject
                    for (product in bzItems.entrySet()) {
                        val quickStats = product.value.asJsonObject.get("quick_status").asJsonObject
                        val sellPrice = quickStats.get("sellPrice").asDouble
                        val buyPrice = quickStats.get("buyPrice").asDouble

                        val productJson = JsonObject()
                        productJson.addProperty("sellPrice", sellPrice)
                        productJson.addProperty("buyPrice", buyPrice)
                        productJson.addProperty("worth", sellPrice)

                        skyblockItemPrices.add(product.key, productJson)
                    }
                }
            }
        }.start()
    }

    fun getItemIdFromName(displayName: String, ignoreFormatting: Boolean? = false): String? {
        return skyblockItems.entrySet().find { entry ->
            val itemName = entry.value.asJsonObject.get("displayname").asString
            val cleanedItemName = if (ignoreFormatting == true) itemName?.clean() else itemName
            val cleanedDisplayName = if (ignoreFormatting == true) displayName.clean() else displayName
            cleanedItemName == cleanedDisplayName
        }?.key
    }

    private val itemStackCache = mutableMapOf<String, ItemStack>()
    fun createItemStack(itemId: String): ItemStack? {
        if (itemStackCache.contains(itemId)) return itemStackCache[itemId]

        if (itemId == "SKYBLOCK_COIN") {
            val itemJson = skyblockItems["COIN_TALISMAN"]?.asJsonObject ?: return null
            var nbtString = itemJson.get("nbttag")?.asString ?: return null
            val mcItemId = itemJson.get("itemid")?.asString ?: return null
            nbtString = nbtString.replace(
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDUyZGNhNjhjOGY4YWY1MzNmYjczN2ZhZWVhY2JlNzE3Yjk2ODc2N2ZjMTg4MjRkYzJkMzdhYzc4OWZjNzcifX19",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTM4MDcxNzIxY2M1YjRjZDQwNmNlNDMxYTEzZjg2MDgzYTg5NzNlMTA2NGQyZjg4OTc4Njk5MzBlZTZlNTIzNyJ9fX0="
            )
            nbtString = nbtString.replace("ff6fbcd7-5138-36b9-a3dc-4c52af38c20d", "2070f6cb-f5db-367a-acd0-64d39a7e5d1b")
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
                itemStack.itemDamage = itemJson.get("damage").asInt
            }

            itemStackCache[itemId] = itemStack
            return itemStack
        }

        // Assuming skyblockItems is a map containing NBT data as strings
        val itemJson = skyblockItems[itemId]?.asJsonObject ?: return null
        val nbtString = itemJson.get("nbttag")?.asString ?: return null
        val mcItemId = itemJson.get("itemid")?.asString ?: return null

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
            itemStack.itemDamage = itemJson.get("damage").asInt
        }

        itemStackCache[itemId] = itemStack
        return itemStack
    }

    fun getItemPriceInfo(itemId: String): JsonObject? {
        if(!skyblockItemPrices.has(itemId) && itemId.contains(";")) {
            // REJUVENATE;1  -> ENCHANTMENT_REJUVENATE_1
            return getItemPriceInfo("ENCHANTMENT_${itemId.replace(";","_")}")
        }

        return skyblockItemPrices.get(itemId)?.asJsonObject
    }

    fun getItemInfo(itemId: String): JsonObject? {
        return skyblockItems.get(itemId)?.asJsonObject
    }

    fun getSkyblockItems(): JsonObject {
        return skyblockItems
    }

    fun getItemInfo(stack: ItemStack): JsonObject? {
        val id = stack.getSkyblockId() ?: return null
        return skyblockItems.get(id).asJsonObject
    }
}