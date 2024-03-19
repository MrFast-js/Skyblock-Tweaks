package mrfast.sbt.managers

import com.google.gson.JsonObject
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SkyblockInventoryItemEvent
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.matches
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max

object SackManager {
    private var sacks = JsonObject()

    @SubscribeEvent
    fun onProfileSwap(event: ProfileLoadEvent) {
        sacks = DataManager.getProfileDataDefault("sackData", JsonObject()) as JsonObject
    }

    @SubscribeEvent
    fun onChatEvent(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val clean = event.message.unformattedText.clean()
        val sackChangeRegex =
            "\\[Sacks\\] (?:\\+\\d{1,3}(?:,\\d{3})* items|-\\d{1,3}(?:,\\d{3})* items|\\+\\d{1,3}(?:,\\d{3})* items, -\\d{1,3}(?:,\\d{3})* items)\\. \\(Last \\d+s\\.\\)"

        if (clean.matches(sackChangeRegex)) {
            for (segment in event.message.siblings) {
                val chatMsgPart = segment.unformattedText.replace("[^0-9]".toRegex(), "")
                if (chatMsgPart.isEmpty()) continue

                val hoverComponentParts = segment.chatStyle.chatHoverEvent?.value?.siblings ?: return
                var lastCapturedNumber = -1

                for (component in hoverComponentParts) {
                    if (lastCapturedNumber == -1) {
                        val short = component.unformattedText.trim().replace("+", "").replace("[^0-9]".toRegex(), "")
                        if (short.isEmpty()) continue

                        var int = short.toInt()
                        if (component.unformattedText.contains("-")) int *= -1

                        lastCapturedNumber = int
                        continue
                    }
                    val material = component.unformattedText
                    val itemId = ItemApi.getItemIdFromName(material,true)?: return

                    val oldValue = sacks.get(material)?.asDouble ?: 0.0

                    if (lastCapturedNumber > 0) {
                        MinecraftForge.EVENT_BUS.post(
                            SkyblockInventoryItemEvent.SackItemEvent(
                                SkyblockInventoryItemEvent.EventType.GAINED,
                                lastCapturedNumber,
                                material,
                                itemId
                            )
                        )
                    } else {
                        MinecraftForge.EVENT_BUS.post(
                            SkyblockInventoryItemEvent.SackItemEvent(
                                SkyblockInventoryItemEvent.EventType.LOST,
                                lastCapturedNumber,
                                material,
                                itemId
                            )
                        )
                    }

                    sacks.addProperty(material, max(oldValue + lastCapturedNumber, 0.0))
                    DataManager.saveProfileData("sackData", sacks)

                    lastCapturedNumber = -1
                }
            }
        }
    }
}