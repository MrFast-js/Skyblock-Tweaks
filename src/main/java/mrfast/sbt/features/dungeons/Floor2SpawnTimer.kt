package mrfast.sbt.features.dungeons

import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.RenderUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Floor2SpawnTimer {
    private var startCounting = false
    private var time = 7.75
    private val priestPos = BlockPos(-29, 71, -4)
    private val warriorPos = BlockPos(13, 71, -4)
    private val magePos = BlockPos(13, 71, -23)
    private val archPos = BlockPos(-29, 71, -23)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationUtils.dungeonFloor != 2) return

        if (event.phase != TickEvent.Phase.START) return

        if (startCounting) {
            time -= 0.05
            if (time <= -5) {
                startCounting = false
                time = 7.75
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationUtils.dungeonFloor != 2) return

        val clean = event.message.unformattedText
        if (clean == "[BOSS] Scarf: If you can beat my Undeads, I'll personally grant you the privilege to replace them.") {
            startCounting = true
        }
    }

    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if (!DungeonConfig.floor2SpawnTimer || LocationUtils.dungeonFloor != 2) return

        if (startCounting) {
            GlStateManager.pushMatrix()
            GlStateManager.scale(2f, 2f, 2f)
            if (time + 0.2 > 0) {
                RenderUtils.draw3DString("Warrior §e${(time + 0.2).format(1)}s", Vec3(warriorPos), event.partialTicks)
            }
            if (time + 0.3 > 0) {
                RenderUtils.draw3DString("Priest §e${(time + 0.3).format(1)}s", Vec3(priestPos), event.partialTicks)
            }
            if (time + 0.4 > 0) {
                RenderUtils.draw3DString("Mage §e${(time + 0.4).format(1)}s", Vec3(magePos), event.partialTicks)
            }
            if (time + 0.5 > 0) {
                RenderUtils.draw3DString("Archer §e${(time + 0.5).format(1)}s", Vec3(archPos), event.partialTicks)
            }
            GlStateManager.scale(1 / 2f, 1 / 2f, 1 / 2f)
            GlStateManager.popMatrix()
        }
    }

    private fun Double.format(decimalPlaces: Int): String = "%.${decimalPlaces}f".format(this)

}