package mrfast.sbt.features.auctionHouse

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object AuctionNotifications {
    private val ignoredAuctionIDs = mutableListOf<String>()
    private val outbidRegex = """§6\[Auction\] (.*) §eoutbid you by (.*) coins §efor (.*) §e§lCLICK""".toRegex()
    private var lastOpenedAuctionID: String? = null

    fun capturedAuctionID(): Boolean {
        return lastOpenedAuctionID != null && lastOpenedAuctionID!!.isNotEmpty()
    }

    fun ignoreCurrentAuction() {
        val currentAuctionID = lastOpenedAuctionID ?: return
        ignoredAuctionIDs.add(currentAuctionID)
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if(TickManager.tickCount % 20 != 0) return

        if(event.phase == TickEvent.Phase.START && Utils.mc.currentScreen == null && lastOpenedAuctionID != null) {
            lastOpenedAuctionID = null
        }
    }

    @SubscribeEvent
    fun onChatSent(event: PacketEvent.Sending) {
        if (event.packet is C01PacketChatMessage) {
            val message = event.packet.message
            if (message.startsWith("/viewauction")) {
                val auctionID = message.split(" ")[1].replace("-","")
                lastOpenedAuctionID = auctionID
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        if(event.message.chatStyle != null && event.message.chatStyle.chatClickEvent != null && ignoredAuctionIDs.isNotEmpty()) {
            ignoredAuctionIDs.forEach {
                if (event.message.chatStyle.chatClickEvent.value.replace("-","").contains(it)) {
                    event.isCanceled = true
                }
            }
            if (event.isCanceled) return
        }

        val matchResult = outbidRegex.find(event.message.formattedText)
        if (matchResult != null) {
            val (user, amount, itemName) = matchResult.destructured
            val amountNumber = amount.clean().replace(",","").toLong()

            if (AuctionHouseConfig.customOutbidNotifications) {
                event.isCanceled = true
                val customNotification = AuctionHouseConfig.customOutbidNotificationsText
                    .replace("{bidder}", user)
                    .replace("{amount}", if(AuctionHouseConfig.customOutbidNotificationsAbbreviation) (amountNumber.abbreviateNumber()) else amount)
                    .replace("{item}", itemName)
                    .replace("&", "§")

                val chatComponentText = ChatComponentText(customNotification)
                chatComponentText.chatStyle.chatClickEvent = event.message.chatStyle.chatClickEvent

                if (AuctionHouseConfig.customOutbidNotificationsPlaySound) {
                    Utils.playSound("note.harp", 0.3)
                }

                ChatUtils.sendClientMessage(chatComponentText, false)
            }
        }
    }
}