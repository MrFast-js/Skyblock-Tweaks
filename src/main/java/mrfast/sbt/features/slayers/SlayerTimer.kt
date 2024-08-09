package mrfast.sbt.features.slayers

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.SlayerConfig
import mrfast.sbt.customevents.SlayerEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.toFormattedSeconds
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object SlayerTimer {
    @SubscribeEvent
    fun onSlayerDeath(event: SlayerEvent.Death) {
        if (!SlayerConfig.slayerTimer) return

        val timeToSpawn = System.currentTimeMillis() - SlayerManager.slayerStartedAt
        val timeToKill = System.currentTimeMillis() - SlayerManager.slayerSpawnedAt
        val totalTime = (timeToSpawn + timeToKill).toFormattedSeconds()
        // Delay until after the slayer rewards message
        Utils.setTimeout({
            ChatUtils.sendClientMessage(
                ChatFormatting.GOLD.toString() + ChatFormatting.BOLD.toString() + "Slayer Timer\n" +
                        ChatFormatting.AQUA + "        • Total Time: " + totalTime + "\n" +
                        ChatFormatting.YELLOW + "        • Spawn: " + timeToSpawn.toFormattedSeconds() + "\n" +
                        ChatFormatting.YELLOW + "        • Kill: " + timeToKill.toFormattedSeconds()
            )
        }, 100)
    }
}