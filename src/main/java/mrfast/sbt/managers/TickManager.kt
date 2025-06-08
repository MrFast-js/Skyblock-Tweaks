package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.utils.Utils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

@SkyblockTweaks.EventComponent
object TickManager {
    var tickCount = 0

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !Utils.isWorldLoaded()) return

        tickCount++

        // Reset after 5 seconds
        if(tickCount == 100) {
            tickCount = 0
        }
    }
}