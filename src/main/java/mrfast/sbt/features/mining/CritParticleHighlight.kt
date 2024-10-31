package mrfast.sbt.features.mining

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiningConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.ceil
import kotlin.math.floor

@SkyblockTweaks.EventComponent
object CritParticleHighlight {
    private var currentlyMinedBlock: BlockPos? = null
    private var particlePosition: Vec3? = null

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.Received) {
        if (event.packet !is S2APacketParticles || !MiningConfig.CritParticleHighlight) return

        val packet = event.packet
        val type = packet.particleType
        val pos = Vec3(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate)
        if (type == EnumParticleTypes.CRIT) {
            val blockX = floor(pos.xCoord).toInt()
            val blockY = floor(pos.yCoord).toInt()
            val blockZ = floor(pos.zCoord).toInt()

            // Initialize a single BlockPos for the detected face
            currentlyMinedBlock = when {
                pos.zCoord - blockZ <= 0.11 -> BlockPos(blockX, blockY, blockZ - 1) // North Face
                ceil(pos.zCoord) - pos.zCoord <= 0.11 -> BlockPos(blockX, blockY, blockZ + 1) // South Face
                pos.xCoord - blockX <= 0.11 -> BlockPos(blockX - 1, blockY, blockZ) // West Face
                ceil(pos.xCoord) - pos.xCoord <= 0.11 -> BlockPos(blockX + 1, blockY, blockZ) // East Face
                pos.yCoord - blockY <= 0.11 -> BlockPos(blockX, blockY - 1, blockZ) // Bottom Face
                ceil(pos.yCoord) - pos.yCoord <= 0.11 -> BlockPos(blockX, blockY + 1, blockZ) // Top Face
                else -> BlockPos(blockX, blockY, blockZ) // Default to the center block
            }
            particlePosition = pos
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (currentlyMinedBlock == null || particlePosition == null || !MiningConfig.CritParticleHighlight || Utils.mc.theWorld == null) return

        val selectedBlock = Utils.mc.theWorld.getBlockState(currentlyMinedBlock).block
        if (selectedBlock == Blocks.bedrock || selectedBlock == Blocks.air) {
            particlePosition = null
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (currentlyMinedBlock == null || particlePosition == null || !MiningConfig.CritParticleHighlight) return

        val pos = particlePosition!!
        val aabb = AxisAlignedBB(
            pos.xCoord - 0.08, pos.yCoord - 0.08, pos.zCoord - 0.08,
            pos.xCoord + 0.08, pos.yCoord + 0.08, pos.zCoord + 0.08
        )
        // Get the player's eye position and look vector
        val eyePos = Utils.mc.thePlayer.getPositionEyes(event.partialTicks)
        val lookVec = Utils.mc.thePlayer.lookVec

        // Determine color based on intersection
        val color = if (aabb.calculateIntercept(
                eyePos,
                eyePos.addVector(lookVec.xCoord * 100.0, lookVec.yCoord * 100.0, lookVec.zCoord * 100.0)
            ) != null
        ) MiningConfig.CritParticleHighlightFocused else MiningConfig.CritParticleHighlightUnfocused

        RenderUtils.drawFilledBB(aabb, color, event.partialTicks, 0.5f)
    }

}