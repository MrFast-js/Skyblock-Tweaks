package mrfast.sbt.features.fishing

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

@SkyblockTweaks.EventComponent
object HotspotCircleHighlight {
    private val hotspotPositions = mutableListOf<Vec3>()
    private val particleRadiusMap = ConcurrentHashMap<String, Double>()

    @SubscribeEvent
    fun onRender3d(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || !MiscellaneousConfig.hotspotCircleHighlight) return

        val player = Utils.mc.thePlayer ?: return
        val bobber = player.fishEntity

        // Clear and refresh HOTSPOT locations
        hotspotPositions.clear()
        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (entity is EntityArmorStand && entity.customNameTag.clean() == "HOTSPOT") {
                val hotspotPos = Vec3(entity.posX, entity.posY - 1.6, entity.posZ)

                if (!hotspotPositions.contains(hotspotPos)) hotspotPositions.add(hotspotPos)

                // Get the dynamically stored radius or fallback to default 3.0
                var radius = 3.0;

                if (particleRadiusMap.containsKey(hotspotPos.toString())) {
                    radius = particleRadiusMap[hotspotPos.toString()]!!
                }

                // Default color is red (out of range)
                var color = MiscellaneousConfig.hotspotBobberOutColor.get()

                // Check if the bobber is inside the radius
                if (bobber != null && bobber.getDistance(entity.posX, bobber.posY, entity.posZ) <= radius) {
                    color = MiscellaneousConfig.hotspotBobberInColor.get()
                }

                RenderUtils.drawFilledCircleWithBorder(
                    hotspotPos,
                    radius.toFloat(),
                    72,
                    color,
                    color,
                    event.partialTicks,
                    depthCheckBorder = true
                )
            }
        }
    }

    @SubscribeEvent
    fun onReceivePacket(event: PacketEvent.Received) {
        if (event.packet !is S2APacketParticles || hotspotPositions.isEmpty() || !MiscellaneousConfig.hotspotCircleHighlight) return

        try {
            val packet = event.packet
            val pos = Vec3(packet.xCoordinate, packet.yCoordinate, packet.zCoordinate)

            // Ignore packets far from the player
            val isColoredRight = packet.xOffset == 1.0f && packet.yOffset == 0.41176474f && packet.zOffset == 0.7058824f
            if (packet.particleType != EnumParticleTypes.REDSTONE || !isColoredRight) return // Only process REDSTONE

            // Find closest HOTSPOT within 5 blocks
            val closestHotspot = hotspotPositions.minByOrNull { it.distanceTo(pos) } ?: return
            val distanceToHotspot = closestHotspot.distanceTo(pos)

            if (distanceToHotspot > 5) return // Ignore if over 5 blocks

            // Store radius efficiently, replacing only for closest hotspot
            if (particleRadiusMap[closestHotspot.toString()] == null || distanceToHotspot < particleRadiusMap[closestHotspot.toString()]!!) {
                particleRadiusMap[closestHotspot.toString()] = distanceToHotspot
            }
        } catch (_: Exception) {
        }
    }
}