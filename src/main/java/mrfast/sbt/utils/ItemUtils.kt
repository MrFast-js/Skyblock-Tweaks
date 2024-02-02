package mrfast.sbt.utils

import com.google.gson.JsonObject
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

object ItemUtils {

    fun getHeldItem(): ItemStack? {
        return Utils.mc.thePlayer.heldItem
    }
    fun ItemStack.getSkyblockId(): String? {
        val nbt = this.getExtraAttributes()
        if(nbt!=null && nbt.hasKey("id")) {
            return nbt.getString("id")
        }
        return null
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