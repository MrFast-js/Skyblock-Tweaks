package mrfast.sbt.features.auctionHouse

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object AuctionNotificationSimplifier {
    private val outbidRegex = """§6\[Auction\] (.*) §eoutbid you by (.*) coins §efor (.*) §e§lCLICK""".toRegex()

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val matchResult = outbidRegex.find(event.message.formattedText)
        if (matchResult != null) {
            val (user, amount, itemName) = matchResult.destructured

            if (AuctionHouseConfig.customOutbidNotifications) {
                event.isCanceled = true
                val customNotification = AuctionHouseConfig.customOutbidNotificationsText.replace("{bidder}", user)
                    .replace("{amount}", amount).replace("{item}", itemName).replace("&", "§")
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