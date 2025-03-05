package mrfast.sbt.apis

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.JsonToNBT
import java.lang.Integer.parseInt
import java.util.*

@SkyblockTweaks.EventComponent
object ItemApi {
    private var skyblockItems = JsonObject()
    var liveAuction = JsonObject()

    var skyblockItemsLoaded = false

    init {
        println("Loading Skyblock Items from Neu Repo Zip")
        // Update Item Prices every 15 Minutes
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateSkyblockItemData(false)
            }
        }, 0, 1000 * 60 * 15)
    }

    fun updateSkyblockItemData(logging: Boolean) {
        Thread {
            if (!skyblockItemsLoaded) {
                skyblockItems = JsonObject()
                try {
                    if (logging) println("Starting to download Skyblock Items from NEU Repo")
                    NetworkUtils.downloadAndProcessRepo()
                    NetworkUtils.NeuItems.entrySet().forEach {
                        var newKey = it.key
                        // Convert NEU ID system to Skyblock-Tweaks ID system
                        // MEGALODON;3 -> MEGALODON-EPIC
                        // ULTIMATE_WISE;2 -> ENCHANTMENT_ULTIMATE_WISE_2
                        if (newKey.contains(";")) {
                            if (it.value.asJsonObject.get("displayname").asString.contains("[Lvl")) {
                                val parts = it.key.split(";")
                                newKey = "${parts[0]}-${ItemUtils.intToPetTier(parseInt(parts[1]))}"
                            }
                            if (it.value.asJsonObject.get("itemid").asString == "minecraft:enchanted_book") {
                                newKey = "ENCHANTMENT_${it.key.replace(";", "_")}"
                            }
                        }
                        // Add rarity to item api
                        if (it.value.asJsonObject.has("lore")) {
                            val lore = it.value.asJsonObject.get("lore").asJsonArray
                            val rarity = ItemUtils.extractRarity(lore[lore.size() - 1].asString)
                            it.value.asJsonObject.addProperty("rarity", rarity)
                        }

                        skyblockItems.add(newKey, it.value)
                    }
                } catch (e: Exception) {
                    println("There was a problem loading Skyblock Items.. ${e.message}")
                }
            }

            skyblockItemsLoaded = true
            if (logging) println("Loaded Skyblock Items from NEU Repo!")

            while (Utils.mc.theWorld == null || Utils.mc.theWorld?.playerEntities?.stream() == null) {
                Thread.sleep(5000)
            }

            if (logging) println("Loading Lowest Item Prices from SBT API")
            try {
                val data = NetworkUtils.apiRequestAndParse(
                    url = "${DeveloperConfig.modAPIURL}аpi/pricingData",
                    useProxy = false,
                    caching = false
                )
                if (data.entrySet().size == 0) {
                    print("There was a problem loading SBT Prices..")
                    return@Thread
                }

                if (logging) println("Loaded ${data.entrySet().size} Items From Skyblock-Tweaks Item API")
                data.entrySet().forEach {
                    val item = it.value.asJsonObject
                    val itemId = it.key

                    if(!skyblockItems.has(itemId)) {
                        skyblockItems.add(itemId, item)
                        return@forEach
                    }

                    val itemJson = skyblockItems[itemId]?.asJsonObject ?: JsonObject()

                    // Merge all properties from `item` into `itemJson`
                    for ((key, value) in item.entrySet()) {
                        itemJson.add(key, value)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("There was a problem loading SBT Prices..")
                return@Thread
            }


            if (logging) println("Loading Live Item Prices from SBT API")
            try {
                val data = NetworkUtils.apiRequestAndParse(
                    url = "${DeveloperConfig.modAPIURL}аpi/liveAuctions",
                    useProxy = false,
                    caching = false
                )
                if (data.entrySet().size == 0) {
                    print("There was a problem loading SBT Live Prices..")
                    return@Thread
                }

                if (logging) println("Loaded Live ${data.entrySet().size} Auctions Item From Skyblock-Tweaks Item API")

                liveAuction = data
            } catch (e: Exception) {
                e.printStackTrace()
                println("There was a problem loading SBT Live Prices..")
                return@Thread
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

    fun getItemInfo(itemId: String): JsonObject? = skyblockItems[itemId]?.asJsonObject

    fun getSkyblockItems(): JsonObject = skyblockItems

    fun getItemInfo(stack: ItemStack): JsonObject? = stack.getSkyblockId()?.let { skyblockItems[it]?.asJsonObject }
}
