package mrfast.sbt.features.end

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils.toFormattedDuration
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object ZealotSpawnLocations {
    private var zealotSpawns: List<BlockPos> = listOf(
        BlockPos(-646, 5, -274),
        BlockPos(-633, 5, -277),
        BlockPos(-639, 7, -305),
        BlockPos(-631, 5, -327),
        BlockPos(-619, 6, -313),
        BlockPos(-665, 10, -313),
        BlockPos(-632, 5, -260),
        BlockPos(-630, 7, -229),
        BlockPos(-647, 5, -221),
        BlockPos(-684, 5, -261),
        BlockPos(-699, 5, -263),
        BlockPos(-683, 5, -292),
        BlockPos(-698, 5, -319),
        BlockPos(-714, 5, -289),
        BlockPos(-732, 5, -295),
        BlockPos(-731, 5, -275)
    )

    private var bruiserSpawns: List<BlockPos> = listOf(
        BlockPos(-595, 80, -190),
        BlockPos(-575, 72, -201),
        BlockPos(-560, 64, -220),
        BlockPos(-554, 56, -237),
        BlockPos(-571, 51, -240),
        BlockPos(-585, 52, -232),
        BlockPos(-96, 55, -216),
        BlockPos(-578, 53, -214),
        BlockPos(-598, 55, -201),
        BlockPos(-532, 38, -223),
        BlockPos(-520, 38, -235),
        BlockPos(-530, 38, -246),
        BlockPos(-515, 39, -250),
        BlockPos(-516, 39, -264),
        BlockPos(-513, 38, -279),
        BlockPos(-524, 44, -268),
        BlockPos(-536, 48, -252),
        BlockPos(-526, 38, -294),
        BlockPos(-514, 39, -304),
        BlockPos(-526, 39, -317)
    )
    private var mobsSpawnAt = 0L
    private var activeDisplay = "§dZealot Spawn: §510s"

    @SubscribeEvent
    fun onEntityJoinWorld(event: SkyblockMobEvent.Spawn) {
        if (!RenderingConfig.zealotBruiserLocations || !LocationManager.inSkyblock || (LocationManager.currentArea != "Dragon's Nest" && LocationManager.currentArea != "Zealot Bruiser Hideout")) return

        if ((LocationManager.currentArea == "Zealot Bruiser Hideout" && event.sbMob.skyblockMobId == "Zealot Bruiser")) {
            mobsSpawnAt = System.currentTimeMillis() + 8_000
        }
        if (LocationManager.currentArea == "Dragon's Nest" && event.sbMob.skyblockMobId == "Zealot") {
            mobsSpawnAt = System.currentTimeMillis() + 8_000
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!RenderingConfig.zealotBruiserLocations || !LocationManager.inSkyblock || (LocationManager.currentArea != "Dragon's Nest" && LocationManager.currentArea != "Zealot Bruiser Hideout")) return

        val msTillSpawn = mobsSpawnAt - System.currentTimeMillis()
        val secondsRemaining = if (msTillSpawn > 1000) {
            msTillSpawn.toFormattedDuration()
        } else {
            "§aReady"
        }
        val type = if (LocationManager.currentArea == "Dragon's Nest") "Zealot" else "Bruiser"

        activeDisplay = "§d$type Spawn: §5$secondsRemaining"
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!RenderingConfig.zealotBruiserLocations || !LocationManager.inSkyblock || (LocationManager.currentArea != "Dragon's Nest" && LocationManager.currentArea != "Zealot Bruiser Hideout")) return

        val positions = if (LocationManager.currentArea == "Dragon's Nest") zealotSpawns else bruiserSpawns

        for (pos in positions) {
            val color = if (mobsSpawnAt - System.currentTimeMillis() <= 0L) RenderingConfig.zealotBruiserLocReady else RenderingConfig.zealotBruiserLocUnready
            val aabb = AxisAlignedBB(pos.x - 5.0, pos.y + 0.1, pos.z - 5.0, pos.x + 5.0, pos.y - 3.0, pos.z + 5.0)

            if(RenderingConfig.zealotBruiserLocDrawBox) RenderUtils.drawSpecialBB(aabb, color, event.partialTicks)

            if(RenderingConfig.zealotBruiserLocTimer) {
                RenderUtils.draw3DString(
                    activeDisplay,
                    Vec3(pos).addVector(0.0, 1.5, 0.0),
                    event.partialTicks,
                    depth = true
                )
            }
        }
    }
}