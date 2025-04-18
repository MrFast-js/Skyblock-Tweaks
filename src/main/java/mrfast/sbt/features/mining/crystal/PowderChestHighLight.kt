package mrfast.sbt.features.mining.crystal

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiningConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils

import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.common.MinecraftForge

import java.awt.Color
import java.util.concurrent.ConcurrentHashMap

@SkyblockTweaks.EventComponent
object PowderChestHighlighter {

    private val chestSpawns = ConcurrentHashMap<BlockPos, Long>()
    private const val CHEST_LIFETIME_MS = 60000L // 60 seconds

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!MiningConfig.PowderChestESP) return

        val mc = net.minecraft.client.Minecraft.getMinecraft()
        val world = mc.theWorld ?: return
        val now = System.currentTimeMillis()

        for (tile in world.loadedTileEntityList) {
            if (tile is TileEntityChest) {
                val pos = tile.pos
                chestSpawns.putIfAbsent(pos, now)
            }
        }

        chestSpawns.entries.removeIf { (pos, spawnTime) ->
            val tileEntity = world.getTileEntity(pos)
            if (tileEntity !is TileEntityChest) {
                true
            } else if (now - spawnTime > CHEST_LIFETIME_MS) {
                true
            } else {
                RenderUtils.drawSpecialBB(pos, Color(255, 255, 0, 128), event.partialTicks)
                false
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        chestSpawns.clear()
    }

    private fun shouldReturn(): Boolean {
        val isNotInCrystalHollows = !LocationManager.currentArea.contains("Crystal Hollows", ignoreCase = true)
        return !LocationManager.inSkyblock || isNotInCrystalHollows
    }
}