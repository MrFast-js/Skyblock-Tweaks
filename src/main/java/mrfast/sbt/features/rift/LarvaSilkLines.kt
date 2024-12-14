package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.config.categories.RiftConfig.larvaSilkBlockColor
import mrfast.sbt.config.categories.RiftConfig.larvaSilkLineColor
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object LarvaSilkLines {
    private var startingSilkPos: BlockPos? = null

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.message.unformattedText.clean().startsWith("You cancelled the wire")) {
            startingSilkPos = null
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (shouldReturn() || startingSilkPos == null) return
        val heldItem = Utils.mc.thePlayer.heldItem?.getSkyblockId() ?: return
        if (heldItem == "LARVA_SILK") {
            RenderUtils.drawSpecialBB(startingSilkPos!!, larvaSilkBlockColor.get(), event.partialTicks)
            val lookingBlock: MovingObjectPosition = Utils.mc.thePlayer.rayTrace(4.0, event.partialTicks)

            if (lookingBlock.blockPos != null && lookingBlock.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                val starting = Vec3(
                    startingSilkPos!!.x + 0.5,
                    startingSilkPos!!.y + 0.5,
                    startingSilkPos!!.z + 0.5
                )
                val finish = Vec3(lookingBlock.blockPos.x + 0.5, lookingBlock.blockPos.y + 0.5, lookingBlock.blockPos.z + 0.5)

                RenderUtils.drawLine(
                    starting,
                    finish,
                    2,
                    larvaSilkLineColor.get(),
                    event.partialTicks
                )
                RenderUtils.drawSpecialBB(
                    lookingBlock.blockPos,
                    larvaSilkBlockColor.get(),
                    event.partialTicks
                )
            }
        }
    }

    @SubscribeEvent
    fun onBlockInteraction(event: PlayerInteractEvent) {
        if (shouldReturn()) return

        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            val heldItem = Utils.mc.thePlayer.heldItem?.getSkyblockId() ?: return

            if (heldItem == "LARVA_SILK") {
                // If we haven't started silk, start it
                if (startingSilkPos == null) {
                    startingSilkPos = event.pos
                    return
                }
                // If we have started silk, end it
                startingSilkPos = null
            }
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        startingSilkPos = null
    }

    private fun shouldReturn(): Boolean {
        return !LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.larvaSilkDisplay
    }
}