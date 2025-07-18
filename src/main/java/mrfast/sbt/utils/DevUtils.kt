package mrfast.sbt.utils

import com.google.gson.*
import com.google.gson.stream.MalformedJsonException
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.utils.ItemUtils.getExtraAttributes
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import net.minecraft.nbt.NBTTagByteArray
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString
import net.minecraft.network.play.server.S29PacketSoundEffect
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
        try {
            for (key in nbt.keySet) {
                val tag = nbt.getTag(key)
                val jsonElement = when (tag) {
                    is NBTTagCompound -> convertNBTtoJSON(tag)
                    is NBTTagList -> convertNBTListToJSON(tag)
                    is NBTTagByteArray -> JsonObject()
                    else -> JsonParser().parse(tag.toString())
                }
                jsonObject.add(key, jsonElement)
            }
        } catch (event: MalformedJsonException) {
            event.printStackTrace()
            return jsonObject
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

                // Remove these tags as they are too big to display
                itemData.remove("nbttag")
                itemData.remove("lore")

                val genericItemData = prettyPrintJsonToString(itemData).replace("\"", "").split("\n")
                event.toolTip.add("Generic Item Data")
                event.toolTip.addAll(genericItemData)
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

    fun getRecentSounds(): List<SoundPacket> {
        return mostRecentSounds
    }

    private val mostRecentSounds = mutableListOf<SoundPacket>()
    class SoundPacket(val name: String, val pitch: Float, val volume: Float, val time: Long = System.currentTimeMillis())

    @SubscribeEvent
    fun onSoundPacket(event: PacketEvent.Received) {
        if (!CustomizationConfig.developerMode) return

        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            mostRecentSounds.add(SoundPacket(packet.soundName, packet.pitch, packet.volume))
            if (mostRecentSounds.size > 20) {
                mostRecentSounds.removeAt(0)
            }
        }
    }
}