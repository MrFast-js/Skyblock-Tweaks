package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@SkyblockTweaks.EventComponent
object ScarfSpawnTimers {
    private var startMinionCount = false
    private var startBossCount = false

    private var time = 0.0
    private val priestPos = Vec3(-28.5, 72.5, -3.5)
    private val warriorPos = Vec3(14.5, 72.5, -3.5)
    private val magePos = Vec3(14.5, 72.5, -22.5)
    private val archPos = Vec3(-28.5, 72.5, -22.5)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationManager.dungeonFloor != 2 || event.phase != TickEvent.Phase.START || !LocationManager.inSkyblock) return

        if(TickManager.tickCount % 2 != 0) return

        if (startMinionCount || startBossCount) {
            time -= 0.1
            if (time <= -5) {
                startMinionCount = false
                startBossCount = false
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationManager.dungeonFloor != 2) return

        val clean = event.message.unformattedText
        if (clean == "[BOSS] Scarf: If you can beat my Undeads, I'll personally grant you the privilege to replace them.") {
            time = 7.75
            startMinionCount = true
        }
        if (clean == "[BOSS] Scarf: Those toys are not strong enough I see.") {
            time = 10.0
            startBossCount = true
            // Spawns 8 seconds after, do same countdown stuff
        }
    }

    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationManager.dungeonFloor != 2) return

        if (startMinionCount) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(2f, 2f, 2f)
            if (time + 0.2 > 0) {
                RenderUtils.draw3DString("§cWarrior §e${(time + 0.2).format(1)}s", warriorPos, event.partialTicks)
            }
            if (time + 0.3 > 0) {
                RenderUtils.draw3DString("§dPriest §e${(time + 0.3).format(1)}s", priestPos, event.partialTicks)
            }
            if (time + 0.4 > 0) {
                RenderUtils.draw3DString("§bMage §e${(time + 0.4).format(1)}s", magePos, event.partialTicks)
            }
            if (time + 0.5 > 0) {
                RenderUtils.draw3DString("§aArcher §e${(time + 0.5).format(1)}s", archPos, event.partialTicks)
            }
            GlStateManager.scale(1 / 2f, 1 / 2f, 1 / 2f)
            GlStateManager.popMatrix()
        }
        if (startBossCount) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(2f, 2f, 2f)
            if (time > 0) {
                RenderUtils.draw3DString(
                    "§6Scarf §e${(time + 0.4).format(1)}s",
                    Vec3(-7.5, 72.0, -10.5),
                    event.partialTicks
                )
            }
            GlStateManager.scale(1 / 2f, 1 / 2f, 1 / 2f)
            GlStateManager.popMatrix()
        }
    }

    private fun Double.format(decimalPlaces: Int): String = "%.${decimalPlaces}f".format(this)

}