package mrfast.sbt.utils

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.features.general.ItemPriceDescription
import mrfast.sbt.managers.ProfileManager
import mrfast.sbt.utils.Utils.clean
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.apache.commons.codec.binary.Base64InputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

@SkyblockTweaks.EventComponent
object ItemUtils {
    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        skyblockIDcache.clear()
    }

    fun getHeldItem(): ItemStack? {
        return Utils.getPlayer()!!.heldItem
    }

    fun ItemStack.getItemUUID(): String? {
        val nbt = this.getExtraAttributes() ?: return null
        if (!nbt.hasKey("uuid")) return null
        return nbt.getString("uuid")
    }

    // Convert NEU ID system to Skyblock-Tweaks ID system
    // MEGALODON;3 -> MEGALODON-EPIC
    // ULTIMATE_WISE;2 -> ENCHANTMENT_ULTIMATE_WISE_2
    fun convertNeuItemToSBT(neuId: String, neuData: JsonObject): String {
        var newId = neuId
        if (newId.contains(";")) {
            if(neuData.asJsonObject.get("displayname").asString.contains("[Lvl")) {
                newId = convertNeuPetID(neuId)
            }
            if (neuData.asJsonObject.get("itemid").asString == "minecraft:enchanted_book") {
                newId = "ENCHANTMENT_${newId.replace(";", "_")}"
            }
        }
        return newId
    }

    // MEGALODON;3 -> MEGALODON-EPIC
    fun convertNeuPetID(neuId: String): String {
        val parts = neuId.split(";")

        return "${parts[0]}-${intToPetTier(Integer.parseInt(parts[1]))}"
    }

    private val enchantCache = mutableMapOf<String, String?>()

    private fun getEnchantFromName(name: String): String? {
        val cleanedName = name.clean()

        enchantCache[cleanedName]?.let { return it }

        for ((id, itemJSON) in ItemApi.getSkyblockItems().entrySet()) {
            val itemId = itemJSON.asJsonObject?.get("itemid")?.asString ?: continue
            if (itemId != "minecraft:enchanted_book") continue

            val lore = itemJSON.asJsonObject.getAsJsonArray("lore") ?: continue
            if (lore.size() > 0 && lore[0].asString.clean() == cleanedName) {
                enchantCache[cleanedName] = id
                return id
            }
        }

        enchantCache[cleanedName] = null
        return null
    }

    private val skyblockIDcache = mutableMapOf<ItemStack, String?>()
    fun ItemStack.getSkyblockId(): String? {
        if(skyblockIDcache.containsKey(this)) {
            return skyblockIDcache[this]
        }
        val nbt = this.getExtraAttributes()

        if (nbt == null) {
            skyblockIDcache[this] = getEnchantFromName(this.displayName) ?: ItemApi.getItemIdFromName(this.displayName)
            return skyblockIDcache[this]
        }

        val id = nbt.getString("id")?: return null

        if (id == "PET" && nbt.hasKey("petInfo")) {
            val petInfoNbt = nbt.getString("petInfo") ?: return null
            val petInfoJson = DevUtils.convertStringToJson(petInfoNbt) ?: return null
            if (!petInfoJson.isJsonObject) return null
            val petInfo = petInfoJson.asJsonObject
            val tier = petInfo.get("tier").asString
            skyblockIDcache[this] = "${petInfo.get("type").asString}-$tier"
            return skyblockIDcache[this]
        }

        if (id == "RUNE" && nbt.hasKey("runes")) {
            val runeType = nbt.getCompoundTag("runes")?.keySet?.firstOrNull()
            if (runeType != null) {
                skyblockIDcache[this] = "${runeType}_RUNE-${nbt.getCompoundTag("runes").getInteger(runeType)}"
                return skyblockIDcache[this]
            }
        }

        if (id == "NEW_YEAR_CAKE") {
            val year = nbt.getInteger("new_years_cake")
            return "NEW_YEAR_CAKE-$year"
        }

        if (id == "ENCHANTED_BOOK" || this.item == Items.enchanted_book) {
            val enchants = this.getSkyblockEnchants()
            if (enchants.isNotEmpty()) {
                val enchName = enchants.keys.first()
                val enchLvl = enchants[enchName]
                skyblockIDcache[this] = "ENCHANTMENT_${enchName.uppercase()}_$enchLvl"
                return skyblockIDcache[this]
            } else {
                return id
            }
        }

        return id
    }

    fun intToPetTier(tier: Int): String {
        if (tier == 5) return "MYTHIC"
        if (tier == 4) return "LEGENDARY"
        if (tier == 3) return "EPIC"
        if (tier == 2) return "RARE"
        if (tier == 1) return "UNCOMMON"
        if (tier == 0) return "COMMON"
        return "UNKNOWN"
    }

    fun rarityToColor(tier: Int): String {
        if (tier == 6) return "§c"
        if (tier == 5) return "§d"
        if (tier == 4) return "§6"
        if (tier == 3) return "§5"
        if (tier == 2) return "§9"
        if (tier == 1) return "§a"
        return "§f"
    }

    fun ItemStack.getDataString(): String {
        val output = mutableListOf<String>()
        val reforge = this.getExtraAttributes()?.getString("modifier") ?: ""
        val recomb = this.getExtraAttributes()?.hasKey("rarity_upgrades") ?: false
        val hpb = this.getExtraAttributes()?.getInteger("hot_potato_count") ?: 0
        val stars = this.getExtraAttributes()?.getInteger("dungeon_item_level") ?: 0
        val masterStars = this.getExtraAttributes()?.getInteger("upgrade_level") ?: 0
        val scrolls = this.getExtraAttributes()?.getTagList("ability_scroll", 8)
        val attributes = getAttributesShort(this)

        this.getSkyblockEnchants().forEach { (key, value) ->
            if (key.contains("ultimate")) {
                output.add("E:${key.replace("ultimate_", "")}$value")
            }
        }
        if (recomb) {
            output.add("R")
        }
        if (reforge.isNotEmpty()) {
            output.add("Re:${reforge}")
        }
        if (hpb > 0) {
            output.add("${hpb}HP")
        }
        if (masterStars > 0) {
            output.add("${masterStars}S")
        } else if (stars > 0) {
            output.add("${stars}S")
        }
        if (scrolls != null) {
            val total = mutableListOf<String>()
            for (i in 0 until scrolls.tagCount()) {
                val scroll = scrolls.getStringTagAt(i).substring(0, 3)
                total.add(scroll)
            }
            if (total.isNotEmpty()) output.add("SC:${total.joinToString(",")}")
        }
        if (attributes.isNotEmpty()) {
            output.add("AT:$attributes")
        }

        return output.joinToString("+")
    }

    val attributeShortNames = mapOf(
        "mana_pool" to "MP",
        "mana_regeneration" to "MR",
        "veteran" to "VE",
        "dominance" to "DO",
        "mending" to "VI",
        "magic_find" to "MF",
        "speed" to "SP",
        "breeze" to "BR",
        "arachno" to "AR",
        "arachno_resistance" to "AR",
        "attack_speed" to "AS",
        "combo" to "CO",
        "elite" to "EL",
        "ignition" to "IG",
        "life_recovery" to "LR",
        "midas_touch" to "MT",
        "undead" to "UN",
        "undead_resistance" to "UR",
        "mana_steal" to "MS",
        "ender" to "EN",
        "ender_resistance" to "ER",
        "blazing" to "BZ",
        "blazing_resistance" to "BL",
        "warrior" to "WA",
        "deadeye" to "DE",
        "experience" to "EX",
        "lifeline" to "LL",
        "life_regeneration" to "LR",
        "fortitude" to "FO",
        "blazing_fortune" to "BF",
        "fishing_experience" to "FE",
        "double_hook" to "DH",
        "fisherman" to "FM",
        "fishing_speed" to "FS",
        "HUNTER" to "HU",
        "trophy_hunter" to "TH",
        "infection" to "IN",
        "hunter" to "HU" // note: both "HUNTER" and "hunter" exist
    )

    private fun getAttributesShort(stack: ItemStack): String {
        val attributes = stack.getAttributes()
        val out = mutableListOf<String>()
        attributes.forEach { (name, lvl) ->
            val abbreviation = attributeShortNames[name] ?: name.take(2)
            out.add("$abbreviation$lvl")
        }
        return out.joinToString(",")
    }

    fun ItemStack.getExtraAttributes(): NBTTagCompound? {
        if (hasTagCompound()) {
            if (tagCompound != null && tagCompound.hasKey("ExtraAttributes")) {
                return tagCompound.getCompoundTag("ExtraAttributes")
            }
        }
        return null
    }

    fun ItemStack.getSkyblockEnchants(): MutableMap<String, Int> {
        val attributes = this.getExtraAttributes() ?: return mutableMapOf()
        val enchants = attributes.getCompoundTag("enchantments")
        val enchantsOut = mutableMapOf<String, Int>()
        for (enchantment in enchants.keySet) {
            val lvl = enchants.getInteger(enchantment)
            enchantsOut[enchantment] = lvl
        }
        return enchantsOut
    }

    fun ItemStack.getAttributes(): MutableMap<String, Int> {
        val attributes = this.getExtraAttributes() ?: return mutableMapOf()
        val attributeMap = attributes.getCompoundTag("attributes")
        val attributeOut = mutableMapOf<String, Int>()
        for (attribute in attributeMap.keySet) {
            val lvl = attributeMap.getInteger(attribute)
            attributeOut[attribute] = lvl
        }
        return attributeOut
    }

    fun ItemStack.getLore(clean: Boolean? = false): MutableList<String> {
        val lore = mutableListOf(this.displayName)

        if (hasTagCompound()) {
            if (tagCompound != null && tagCompound.hasKey("display", 10)) {
                val displayTag = tagCompound.getCompoundTag("display")

                if (displayTag.hasKey("Lore", 9)) {
                    val loreTagList = displayTag.getTagList("Lore", 8)

                    for (i in 0 until loreTagList.tagCount()) {
                        var line = loreTagList.getStringTagAt(i)
                        if (clean == true) line = line.clean()
                        lore.add(line)
                    }
                }
            }
        }

        return lore
    }

    fun decodeBase64Inventory(data: String?): MutableList<ItemStack?> {
        val itemStacks = mutableListOf<ItemStack?>()

        if (data != null) {
            val decode = Base64.getDecoder().decode(data)

            try {
                val compound = CompressedStreamTools.readCompressed(ByteArrayInputStream(decode))
                val list = compound.getTagList("i", Constants.NBT.TAG_COMPOUND)
                for (i in 0 until list.tagCount()) {
                    val tag = list.getCompoundTagAt(i)
                    val item = ItemStack.loadItemStackFromNBT(tag) ?: null

                    itemStacks.add(item)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return itemStacks
    }

    fun decodeBase64Item(data: String?): ItemStack? {
        if (data != null) {
            try {
                // Decode base64 string and read compressed NBT data
                val decodedBytes = Base64InputStream(ByteArrayInputStream(data.toByteArray(StandardCharsets.UTF_8)))
                val taglist = CompressedStreamTools.readCompressed(decodedBytes)
                val compound = taglist.getTagList("i", 10).getCompoundTagAt(0)

                // Step 4: Load item stack from NBT
                return ItemStack.loadItemStackFromNBT(compound)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }

    fun getItemBasePrice(id: String, sell:Boolean=true): Double {
        if(id == "SKYBLOCK_COIN") return 1.0

        val itemInfo = ItemApi.getItemInfo(id) ?: return -1.0

        if (sell && ProfileManager.onIronman) {
            if(itemInfo.has("npcSell")) {
                return itemInfo.get("npcSell").asDouble
            }
        }

        if (sell && itemInfo.has("bazaarSell")) {
            return itemInfo.get("bazaarSell").asDouble
        }
        if (!sell && itemInfo.has("bazaarBuy")) {
            return itemInfo.get("bazaarBuy").asDouble
        }

        if (itemInfo.has("lowestBin") && itemInfo.has("avgLowestBin")) {
            if(itemInfo.get("lowestBin").asDouble < itemInfo.get("avgLowestBin").asDouble) {
                return itemInfo.get("lowestBin").asDouble
            } else {
                return itemInfo.get("avgLowestBin").asDouble
            }
        }
        if (itemInfo.has("lowestBin")) {
            return itemInfo.get("lowestBin").asDouble
        }
        if (itemInfo.has("avgLowestBin")) {
            return itemInfo.get("avgLowestBin").asDouble
        }

        return -1.0;
    }

    fun ItemStack.getItemBasePrice(): Double {
        val id = this.getSkyblockId() ?: return -1.0
        return getItemBasePrice(id)
    }

    fun getSuggestListingPrice(itemStack: ItemStack): JsonObject? {
        if (AuctionHouseConfig.usePriceMatch) {
            val match = getPriceMatch(itemStack)

            if (match != null) {
                val priceMatch = match.first
                val percentMatch = match.second

                if (percentMatch > 90) {
                    return JsonObject().apply {
                        addProperty("price", priceMatch * 0.99)
                        addProperty("bin", true)
                    }
                }
            }
        }

        val itemID = itemStack.getSkyblockId()?: return null
        val pricingData = ItemApi.getItemInfo(itemID) ?: return null

        val lbin = if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asLong else null
        val abin = if (pricingData.has("avgLowestBin")) pricingData.get("avgLowestBin").asLong else null

        val activeBins = if (pricingData.has("activeBin")) pricingData.get("activeBin").asInt else null
        val aucPrice = if (pricingData.has("aucPrice")) pricingData.get("aucPrice").asLong else null
        val aucSoldPrices = if (pricingData.has("aucSoldPrices")) pricingData.get("aucSoldPrices").asJsonArray else null
        val avgAucSoldPrice = aucSoldPrices?.takeIf { it.size() > 0 }?.let { prices ->
            prices.sumOf { it.asLong } / prices.size()
        } ?: -1L

        val binSoldPrices = if (pricingData.has("binSoldPrices")) pricingData.get("binSoldPrices").asJsonArray else null
        val avgBinSoldPrice = binSoldPrices?.takeIf { it.size() > 0 }?.let { prices ->
            prices.sumOf { it.asLong } / prices.size()
        } ?: -1L

        val binActivePrices =
            if (pricingData.has("activeBinPrices")) pricingData.get("activeBinPrices").asJsonArray else null
        val avgActiveBINPrice = binActivePrices?.takeIf { it.size() > 0 }?.let { prices ->
            prices.sumOf { it.asLong } / prices.size()
        } ?: -1L

        // If there are active BIN prices, suggest BIN price if it's lower than the average BIN price
        if (binActivePrices != null &&
            avgActiveBINPrice != -1L &&
            abin != null &&
            avgActiveBINPrice < abin
            ) {
            return JsonObject().apply {
                addProperty("price", avgActiveBINPrice)
                addProperty("bin", true)
            }
        }

        var suggestedListingPrice = 0L
        if (activeBins != null) {
            if (activeBins > 5) {
                suggestedListingPrice = if (abin != null && lbin != null) {
                    // If the average BIN price is more than double the lowest BIN price, don't suggest listing price
                    if (abin > (lbin * 1.75)) {
                        (avgBinSoldPrice * 0.99).toLong()
                    } else {
                        Math.round((lbin * 0.6 + abin * 0.4) * 0.99)
                    }
                } else {
                    // Calc suggested listing price based on lowest and average BIN prices
                    when {
                        lbin != null -> lbin - 1000L
                        abin != null -> abin - 1000L
                        else -> 0L
                    }
                }
            } else {
                // Calc suggested listing price based on average AUC price for items that should be sold as auctions not BIN
                if (avgAucSoldPrice != -1L) {
                    if (aucPrice != null) {
                        suggestedListingPrice = Math.round((avgAucSoldPrice * 0.6 + aucPrice * 0.4) * 0.99)
                    }
                } else if (avgBinSoldPrice != -1L && avgActiveBINPrice != -1L) {
                    suggestedListingPrice = Math.round((avgBinSoldPrice * 0.6 + avgActiveBINPrice * 0.4) * 0.99)
                } else {
                    // Backup
                    if (abin != null && lbin != null) {
                        suggestedListingPrice = Math.round((lbin * 0.6 + abin * 0.4) * 0.99)
                    }
                }
            }
        }

        var bonusPricing = 0.0
        val extraAttributes = itemStack.getExtraAttributes()!!

        if (extraAttributes.hasKey("rarity_upgrades") && !extraAttributes.hasKey("item_tier")) {
            val recombInfo = ItemApi.getItemInfo("RECOMBOBULATOR_3000")
            val price = recombInfo?.get("bazaarSell")?.asInt ?: 0
            bonusPricing += price * 0.6
        }

        return JsonObject().apply {
            addProperty("price", (suggestedListingPrice + bonusPricing).toLong())
            if (activeBins != null) {
                addProperty("bin", activeBins > 5)
            }
        }
    }

    fun extractRarity(itemDescription: String): String? {
        val rarities = listOf(
            "COMMON",
            "UNCOMMON",
            "RARE",
            "EPIC",
            "LEGENDARY",
            "MYTHIC",
            "DIVINE",
            "SPECIAL",
            "VERY_SPECIAL",
            "SUPREME"
        )
        // Reverse so uncommon is checked before common
        for (rarity in rarities.reversed()) {
            if (itemDescription.contains(rarity, ignoreCase = true)) {
                return rarity
            }
        }

        return null
    }

    fun getPriceMatch(stack: ItemStack): Pair<Long, Double>? {
        if (stack.getSkyblockId() == null) return null
        return getPriceMatch(stack.getDataString(), stack.getSkyblockId()!!)
    }

    private fun getPriceMatch(dataString: String, id: String): Pair<Long, Double>? {
        if (dataString == "" || !ItemApi.liveAuctionData.has(id)) return null
        val itemData = ItemApi.liveAuctionData.get(id).asJsonObject

        return ItemPriceDescription.findBestMatch(itemData.asJsonObject, dataString, id)
    }
}
