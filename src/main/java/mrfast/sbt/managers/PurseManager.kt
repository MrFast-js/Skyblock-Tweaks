package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.PurseChangeEvent
import mrfast.sbt.utils.ScoreboardUtils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object PurseManager {
    var coinsInPurse = 0
    private var stopNextChange = false
    var PURSE_REGEX = """Purse: ([0-9,.]+)""".toRegex()

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        if (event.message.unformattedText.clean() == "Withdrawing coins..." || event.message.unformattedText.clean() == "Depositing coins...") {
            stopNextChange = true
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        val lines = ScoreboardUtils.getSidebarLines(true)
        for (line in lines) {
            if (line.matches(PURSE_REGEX)) {
                val groups = line.getRegexGroups(PURSE_REGEX) ?: break
                val purseCoins = groups[1].toString().replace("[^0-9]", "").replace(",", "").toDouble().toInt()

                if (coinsInPurse != purseCoins) {
                    if (stopNextChange) {
                        stopNextChange = false
                    } else {
                        MinecraftForge.EVENT_BUS.post(PurseChangeEvent(purseCoins - coinsInPurse))
                    }
                    coinsInPurse = purseCoins
                }
                break
            }
        }
    }
}