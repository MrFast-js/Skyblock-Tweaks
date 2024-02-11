package mrfast.sbt.utils

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object ItemUtils {

    fun getHeldItem(): ItemStack? {
        return Utils.mc.thePlayer.heldItem
    }

    fun ItemStack.getSkyblockId(): String? {
        val nbt = this.getExtraAttributes()
        if (nbt != null && nbt.hasKey("id")) {
            if (nbt.getString("id").equals("PET")) {
                val petInfo = DevUtils.convertStringToJson(nbt.getString("petInfo"))?.asJsonObject ?: return null
                val tierInt = petTierToInt(petInfo.get("tier").asString)

                return petInfo.get("type").asString + ";$tierInt"
            }
            return nbt.getString("id")
        }
        return null
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

    fun ItemStack.getExtraAttributes(): NBTTagCompound? {
        if (hasTagCompound()) {
            if (tagCompound != null && tagCompound.hasKey("ExtraAttributes")) {
                return tagCompound.getCompoundTag("ExtraAttributes")
            }
        }
        return null
    }

    fun ItemStack.getLore(): MutableList<String> {
        val lore = mutableListOf<String>()

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
}