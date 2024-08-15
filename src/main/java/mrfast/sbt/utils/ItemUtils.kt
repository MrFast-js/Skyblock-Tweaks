package mrfast.sbt.utils

import mrfast.sbt.apis.ItemApi
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
                val petInfo = DevUtils.convertStringToJson(nbt.getString("petInfo"))?.asJsonObject ?: return null
                val tierInt = petTierToInt(petInfo.get("tier").asString)
                "${petInfo.get("type").asString};$tierInt"
            }

            nbt.hasKey("runes") -> {
                val runeType = nbt.getCompoundTag("runes")?.keySet?.firstOrNull()
                runeType?.let {
                    "${runeType}_RUNE;${nbt.getCompoundTag("runes").getInteger(runeType)}"
                }
            }

            else -> id
        }
    }

    private fun petTierToInt(tier: String): Int {
        if (tier == "MYTHIC") return 5
        if (tier == "LEGENDARY") return 4
        if (tier == "EPIC") return 3
        if (tier == "RARE") return 2
        if (tier == "UNCOMMON") return 1
        if (tier == "COMMON") return 0
        return -1
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

    fun ItemStack.getLore(): MutableList<String> {
        val lore = mutableListOf(this.displayName)

        if (hasTagCompound()) {
            val tagCompound = tagCompound

            if (tagCompound != null && tagCompound.hasKey("display", 10)) {
                val displayTag = tagCompound.getCompoundTag("display")

                if (displayTag.hasKey("Lore", 9)) {
                    val loreTagList = displayTag.getTagList("Lore", 8)

                    for (i in 0 until loreTagList.tagCount()) {
                        lore.add(loreTagList.getStringTagAt(i))
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

    fun ItemStack.getItemBasePrice(): Double {
        val id = this.getSkyblockId() ?: return -1.0;
        if (ItemApi.getItemPriceInfo(id) != null) {
            return ItemApi.getItemPriceInfo(id)?.get("basePrice")?.asDouble ?: 0.0
        }
        return 0.0
    }

    fun getSuggestListingPrice(itemStack: ItemStack): Int? {
        val itemID = itemStack.getSkyblockId()!!
        val pricingData = ItemApi.getItemPriceInfo(itemID) ?: return null

        val lbin = if (pricingData.has("lowestBin")) pricingData.get("lowestBin").asDouble else null
        val abin = if (pricingData.has("price_avg")) pricingData.get("price_avg").asDouble else null

        val suggestedListingPrice = when {
            lbin != null && abin != null -> Math.round((lbin * 0.6 + abin * 0.4) * 0.99).toInt()
            lbin != null -> Math.round(lbin - 1000).toInt()
            abin != null -> Math.round(abin - 1000).toInt()
            else -> 0
        }

        var bonusPricing = 0.0
        val extraAttributes = itemStack.getExtraAttributes()!!

        if (extraAttributes.hasKey("rarity_upgrades") && !extraAttributes.hasKey("item_tier")) {
            val recombInfo = ItemApi.getItemPriceInfo("RECOMBOBULATOR_3000")
            bonusPricing += recombInfo?.get("sellPrice")!!.asInt * 0.6
        }

        return (suggestedListingPrice + bonusPricing).toInt()
    }
}