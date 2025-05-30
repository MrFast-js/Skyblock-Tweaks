package mrfast.sbt.features.beastiary

import com.mojang.realmsclient.gui.ChatFormatting
import gg.essential.elementa.utils.withAlpha
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.SkyblockMobDetector
import mrfast.sbt.apis.SkyblockMobDetector.getSkyblockMob
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.config.categories.SlayerConfig
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.customevents.SkyblockMobEvent
import mrfast.sbt.features.slayers.SlayerManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@SkyblockTweaks.EventComponent
object BeastiaryMobHighlight {
    private val trackedMobs = mutableListOf<String>()

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!MiscellaneousConfig.beastiaryMobHighlight || trackedMobs.isEmpty()) return

        val mob = getSkyblockMob(event.entity) ?: return

        if (trackedMobs.contains(mob.skyblockMobId)) {
            OutlineUtils.outlineEntity(event, MiscellaneousConfig.beastiaryMobHighlightColor.get())
        }
    }

    @SubscribeEvent
    fun onRender(event: SkyblockMobEvent.Render) {
        if (!MiscellaneousConfig.beastiaryMobHighlight || trackedMobs.isEmpty() || !MiscellaneousConfig.beastiaryMobHighlightTracer) return

        val mob = event.sbMob

        if (trackedMobs.contains(mob.skyblockMobId)) {
            RenderUtils.drawLineToEntity(
                mob.skyblockMob,
                2,
                MiscellaneousConfig.beastiaryMobHighlightLineColor.get(),
                event.partialTicks!!
            )
        }
    }

    @SubscribeEvent
    fun onMouseInput(event: MouseEvent) {
        if (!MiscellaneousConfig.beastiaryMobHighlight) return

        if (event.button == 2 && event.buttonstate) { // 2 corresponds to the middle mouse button
            // Get the entity the player is looking at
            val targetEntity = getTargetEntity()

            if (targetEntity != null) {
                val sbMob = getSkyblockMob(targetEntity)

                if (sbMob != null) {
                    if (sbMob.skyblockMobId == null) {
                        ChatUtils.sendClientMessage(ChatFormatting.RED.toString() + "This mob could not be identified for the bestiary tracker!", shortPrefix = true)
                    }
                }

                if (sbMob?.skyblockMobId == null) {
                    return
                }

                if (trackedMobs.contains(sbMob.skyblockMobId)) {
                    trackedMobs.remove(sbMob.skyblockMobId)
                    Utils.playSound("random.orb", 0.1)
                    ChatUtils.sendClientMessage("§cStopped highlighting ${sbMob.skyblockMobId}!", shortPrefix = true)
                } else {
                    trackedMobs.add(sbMob.skyblockMobId!!)
                    Utils.playSound("random.orb", 1.0)
                    ChatUtils.sendClientMessage("§aStarted highlighting " + sbMob.skyblockMobId + "!", shortPrefix = true)
                }
            }
        }
    }

    private fun getTargetEntity(): Entity? {
        val range = 4.0
        val player = Utils.mc.thePlayer
        val start = Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ)
        val look = player.lookVec
        val end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range)
        var hitEntity: Entity? = null
        var closestDistance = range

        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (entity === Utils.mc.thePlayer) continue

            if (entity.canBeCollidedWith()) {
                val collisionSize = entity.collisionBorderSize.toDouble()
                val boundingBox = entity.entityBoundingBox.expand(collisionSize, collisionSize, collisionSize)
                val intercept = boundingBox.calculateIntercept(start, end)

                if (intercept != null) {
                    val distance = start.distanceTo(intercept.hitVec)
                    if (distance < closestDistance) {
                        closestDistance = distance
                        hitEntity = entity
                    }
                }
            }
        }
        return hitEntity
    }
}