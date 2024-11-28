package mrfast.sbt.features.slayers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.customevents.SlayerEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ScoreboardUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object SlayerManager {
    var slayerStartedAt = System.currentTimeMillis()
    var slayerSpawnedAt = System.currentTimeMillis()
    private var hasSlayerSpawned = false

    var spawnedSlayer: SkyblockMobDetector.SkyblockMob? = null
    private val minibosses = mutableListOf(
        "Revenant Sycophant",
        "Revenant Champion",
        "Deformed Revenant",
        "Atoned Champion",
        "Atoned Revenant",
        "Tarantula Vermin",
        "Tarantula Beast",
        "Mutant Tarantula",
        "Pack Enforcer",
        "Sven Follower",
        "Sven Alpha",
        "Voidling Devotee",
        "Voidling Radical",
        "Voidcrazed Maniac",
        "Flare Demon",
        "Kindleheart Demon",
        "Burningsoul Demon"
    )

    fun isMiniboss(sbMob: SkyblockMobDetector.SkyblockMob): Boolean {
        return minibosses.contains(sbMob.skyblockMobId)
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val msg = event.message.unformattedText

        if (msg.trim().startsWith("SLAYER QUEST STARTED!")) {
            slayerStartedAt = System.currentTimeMillis()
            spawnedSlayer = null
        }
        if (msg.trim().startsWith("NICE! SLAYER BOSS SLAIN!") || msg.trim().startsWith("SLAYER QUEST COMPLETE!")) {
            if(hasSlayerSpawned) {
                MinecraftForge.EVENT_BUS.post(SlayerEvent.Death())
            }
            spawnedSlayer = null
            hasSlayerSpawned = false
            slayerSpawnedAt = 0
            slayerStartedAt = 0
        }
    }


    @SubscribeEvent
    fun onSbMobSpawn(event: SkyblockMobEvent.Render) {
        if (hasSlayerSpawned || event.sbMob.skyblockMobId == null) return
        val id = event.sbMob.skyblockMobId ?: return

        if (id.endsWith("Slayer")) {
            var nextLine = false
            var slayerName = ""
            for (line: String in ScoreboardUtils.getSidebarLines(true)) {
                if (nextLine) {
                    nextLine = false
                    slayerName = getActualSlayerName(line)
                }
                if (line.contains("Slayer Quest")) {
                    nextLine = true
                }
                if (line.contains("Slay the boss!")) {
                    if (CustomizationConfig.developerMode) {
                        ChatUtils.sendClientMessage("Detected sidebar slayer spawned")
                    }
                    hasSlayerSpawned = true
                }
            }
            if (id.startsWith(slayerName) && hasSlayerSpawned) {
                slayerSpawnedAt = System.currentTimeMillis()
                spawnedSlayer = event.sbMob
                if (CustomizationConfig.developerMode) {
                    ChatUtils.sendClientMessage("Slayer Spawned")
                }
            }
        }
    }

    private fun getActualSlayerName(sidebarName: String): String {
        return when {
            sidebarName.contains("Revenant Horror V") -> "Atoned Horror"
            sidebarName.contains("Riftslalker Bloodfiend") -> "Bloodfiend"
            else -> sidebarName
        }
    }

}

