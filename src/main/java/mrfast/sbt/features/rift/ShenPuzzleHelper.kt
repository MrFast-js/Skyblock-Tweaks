package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object ShenPuzzleHelper {
    private val startButtonPos = BlockPos(-262, 20, -61)
    private val patterns = listOf(listOf(2, 3, 4, 5, 6, 7), listOf(2, 4, 6), listOf(3, 4, 5), listOf(3, 4, 7), listOf(3, 5, 6, 7))


    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        //if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper) return
        val player = Utils.mc.thePlayer
        if(player.position.distanceSq(startButtonPos)>400) return

        val selectedPattern = patterns[0]
        for ((i, button) in selectedPattern.withIndex()){
            highLightButton(button, event.partialTicks)
            RenderUtils.draw3DString( "Step ${i+1}", startButtonPos.add(0, 0, -1*(button-1)), event.partialTicks, true, true)
        }

    }

    private fun highLightButton(index: Int, partialTicks: Float){
        RenderUtils.drawSpecialBB(startButtonPos.add(0, 0, -1*(index-1)), RiftConfig.shenButtonColor.get(), partialTicks)

    }
}