package mrfast.sbt.utils

import com.google.gson.*
import mrfast.sbt.config.Categories.CustomizationConfig
import mrfast.sbt.utils.ItemUtils.getExtraAttributes
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object DevUtils {
    fun prettyPrintNBTtoString(nbt: NBTTagCompound): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(convertNBTtoJSON(nbt))
    }

    private fun convertNBTtoJSON(nbt: NBTTagCompound, showList:Boolean = false): JsonObject {
        val jsonObject = JsonObject()
        for (key in nbt.keySet) {
            val tag = nbt.getTag(key)
            val jsonElement = when (tag) {
                is NBTTagCompound -> convertNBTtoJSON(tag)
                is NBTTagList -> if (showList) convertNBTListToJSON(tag) else JsonNull.INSTANCE
                else -> JsonParser().parse(tag.toString())
            }
            jsonObject.add(key, jsonElement)
        }
        return jsonObject
    }

    private fun convertNBTListToJSON(nbtList: NBTTagList): JsonArray {
        println(nbtList)
        val jsonArray = JsonArray()
        for (i in 0 until nbtList.tagCount()) {
            val tag = nbtList.get(i)
            println(tag)

            when (tag) {
                is NBTTagString -> jsonArray.add(JsonPrimitive(tag.string))
                is NBTTagCompound -> jsonArray.add(convertNBTtoJSON(tag))
                // Handle other types if necessary
                else -> jsonArray.add(JsonNull.INSTANCE)
            }
        }
        return jsonArray
    }

    @SubscribeEvent
    fun onToolTip(event:ItemTooltipEvent) {
        if(event.itemStack.getExtraAttributes()!=null && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) && CustomizationConfig.developerMode) {
            val nbt = prettyPrintNBTtoString(event.itemStack.getExtraAttributes()!!).replace("\"","").split("\n")
            event.toolTip.addAll(nbt)
        }
    }
}