package mrfast.sbt.utils

import com.google.gson.JsonObject
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.utils.Utils.clean
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompressedStreamTools
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.Constants
import org.apache.commons.codec.binary.Base64InputStream
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*

object ItemUtils {

    fun getHeldItem(): ItemStack? {
        return Utils.mc.thePlayer.heldItem
    }

    fun ItemStack.getItemUUID(): String? {
        val nbt = this.getExtraAttributes() ?: return null
        if (!nbt.hasKey("uuid")) return null
        return nbt.getString("uuid")
    }

    fun ItemStack.getSkyblockId(): String? {
        val nbt = this.getExtraAttributes() ?: return null

        if (!nbt.hasKey("id")) return null

        val id = nbt.getString("id")
        return when {
            id == "PET" && nbt.hasKey("petInfo") -> {
                val petInfoNbt = nbt.getString("petInfo") ?: return null
                val petInfoJson = DevUtils.convertStringToJson(petInfoNbt) ?: return null
                if (!petInfoJson.isJsonObject) return null
                val petInfo = petInfoJson.asJsonObject
                val tier = petInfo.get("tier").asString
                "${petInfo.get("type").asString}-$tier"
            }

            id == "RUNE" && nbt.hasKey("runes") -> {
                val runeType = nbt.getCompoundTag("runes")?.keySet?.firstOrNull()
                runeType?.let {
                    "${runeType}_RUNE-${nbt.getCompoundTag("runes").getInteger(runeType)}"
                }
            }

            id == "NEW_YEAR_CAKE" -> {
                val year = nbt.getInteger("new_years_cake")
                "NEW_YEAR_CAKE-$year"
            }

            id == "ENCHANTED_BOOK" || this.item == Items.enchanted_book -> {
                val enchants = this.getSkyblockEnchants()
                if (enchants.isNotEmpty()) {
                    val enchName = enchants.keys.first()
                    val enchLvl = enchants[enchName]

                    "ENCHANTMENT_${enchants.keys.first().uppercase()}_$enchLvl"
                } else {
                    id
                }
            }

            else -> id
        }
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
        if(recomb) {
            output.add("R")
        }
        if(reforge.isNotEmpty()) {
            output.add("Re:${reforge}")
        }
        if(hpb > 0) {
            output.add("${hpb}HP")
        }
        if(masterStars > 0) {
            output.add("${masterStars}S")
        } else if(stars > 0) {
            output.add("${stars}S")
        }
        if(scrolls != null) {
            val total = mutableListOf<String>()
            for(i in 0 until scrolls.tagCount()) {
                val scroll = scrolls.getStringTagAt(i).substring(0, 3)
                total.add(scroll)
            }
            if(total.isNotEmpty()) output.add("SC:${total.joinToString(",")}")
        }
        if(attributes.isNotEmpty()) {
            output.add("AT:$attributes")
        }

        return output.joinToString("+")
    }

    private fun getAttributesShort(stack: ItemStack): String {
        val attributes = stack.getAttributeShards()
        val out = mutableListOf<String>()
        attributes.forEach { (name, lvl) ->
            // if attribute name is one word, take first 2 letters,
            // else take first letter and first 3 letters of second word
            val abbreviation = name.split('_').let {
                if (it.size == 1) it[0].take(2) else it[0].take(1) + it[1].take(3)
            }
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

    fun ItemStack.getAttributeShards(): MutableMap<String, Int> {
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
            val tagCompound = tagCompound

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

    fun getItemBasePrice(id: String): Double {
        val itemInfo = ItemApi.getItemInfo(id) ?: return -1.0

        if (itemInfo.has("bazaarSell")) {
            return itemInfo.get("bazaarSell").asDouble
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
        val itemID = itemStack.getSkyblockId()!!
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
        if (binActivePrices != null) {
            if (avgActiveBINPrice != -1L) {
                if (abin != null) {
                    if (avgActiveBINPrice < abin) {
                        return JsonObject().apply {
                            addProperty("price", avgActiveBINPrice)
                            addProperty("bin", true)
                        }
                    }
                }
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
                }
            }
        }

        var bonusPricing = 0.0
        val extraAttributes = itemStack.getExtraAttributes()!!

        if (extraAttributes.hasKey("rarity_upgrades") && !extraAttributes.hasKey("item_tier")) {
            val recombInfo = ItemApi.getItemInfo("RECOMBOBULATOR_3000")
            bonusPricing += recombInfo?.get("bazaarSell")!!.asInt * 0.6
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
}
