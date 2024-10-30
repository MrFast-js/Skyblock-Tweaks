package mrfast.sbt.utils

import com.google.gson.*
import com.google.gson.stream.MalformedJsonException
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.utils.ItemUtils.getExtraAttributes
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

@SkyblockTweaks.EventComponent
object DevUtils {
    fun prettyPrintNBTtoString(nbt: NBTTagCompound): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        return gson.toJson(convertNBTtoJSON(nbt))
    }

    fun prettyPrintJsonToString(json: JsonObject): String {
        try {
            val gson = GsonBuilder().setPrettyPrinting().create()
            return gson.toJson(json)
        } catch (event: MalformedJsonException) {
            return "{}"
        }
    }

    fun convertStringToJson(string: String): JsonElement? {
        return JsonParser().parse(string)
    }

    private fun convertNBTtoJSON(nbt: NBTTagCompound): JsonObject {
        val jsonObject = JsonObject()
        for (key in nbt.keySet) {
            val tag = nbt.getTag(key)
            val jsonElement = when (tag) {
                is NBTTagCompound -> convertNBTtoJSON(tag)
                is NBTTagList -> convertNBTListToJSON(tag)
                else -> JsonParser().parse(tag.toString())
            }
            jsonObject.add(key, jsonElement)
        }
        return jsonObject
    }

    private fun convertNBTListToJSON(nbtList: NBTTagList): JsonArray {
        val jsonArray = JsonArray()
        for (i in 0 until nbtList.tagCount()) {
            val tag = nbtList.get(i)

            when (tag) {
                is NBTTagString -> jsonArray.add(JsonPrimitive(tag.string))
                is NBTTagCompound -> jsonArray.add(convertNBTtoJSON(tag))
                // Handle other types if necessary
                else -> jsonArray.add(JsonNull.INSTANCE)
            }
        }
        return jsonArray
    }

    /**
     * Allows for developer mode users to use various debug features
     * Holding LCTRL -> Show Extra Attributes
     * Holding LCTRL + LSHIFT -> Show Item Data provided by HySky Api
     * Holding LCTRL + KEY C -> Copy Viewed Lore
     */
    private var copyingTooltip = false

    @SubscribeEvent
    fun onToolTip(event: ItemTooltipEvent) {
        if (!CustomizationConfig.developerMode) return
        if (event.itemStack.getExtraAttributes() != null && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                event.toolTip.clear()
                val id = event.itemStack.getSkyblockId() ?: return
                val itemData = ItemApi.getItemInfo(id) ?: return
                itemData.remove("nbttag")
                itemData.remove("lore")

                val genericItemData = prettyPrintJsonToString(itemData).replace("\"", "").split("\n")
                event.toolTip.add("Generic Item Data")
                event.toolTip.addAll(genericItemData)

                val priceData = ItemApi.getItemPriceInfo(id) ?: return

                val itemPriceData = prettyPrintJsonToString(priceData).replace("\"", "").split("\n")
                event.toolTip.add("Item Price Data")
                event.toolTip.addAll(itemPriceData)
            } else {
                val nbt = prettyPrintNBTtoString(event.itemStack.getExtraAttributes()!!).replace("\"", "").split("\n")
                event.toolTip.addAll(nbt)
            }
        }
        if (event.itemStack != null && Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            val nbt = prettyPrintNBTtoString(event.itemStack.serializeNBT()).replace("\"", "").split("\n")
            event.toolTip.clear()
            event.toolTip.addAll(nbt)
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_C) && !copyingTooltip) {
            copyingTooltip = true
            Utils.setTimeout({
                copyingTooltip = false
            }, 500)
            Utils.copyToClipboard(event.toolTip.joinToString("\n"))
        }
    }
}