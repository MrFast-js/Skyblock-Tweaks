package mrfast.sbt.features.slayers

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
            ChatUtils.sendClientMessage("")
            ChatUtils.sendClientMessage(
                "§6§lSlayer Timer\n" +
                        "§9    • Kill: §2" + timeToKill.toFormattedSeconds() + "\n" +
                        "§3    • Spawn: §2" + timeToSpawn.toFormattedSeconds() + "\n" +
                        "§b    • Total Time: §a" + totalTime
                , shortPrefix = true
            )
            ChatUtils.sendClientMessage("")
        }, 100)
    }
}