package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Robot
import java.awt.event.InputEvent
import kotlin.random.Random

@SkyblockTweaks.EventComponent
object AutoTowerToucher {
    private const val TOWER_SKIN_ID =
        "ewogICJ0aW1lc3RhbXAiIDogMTcxOTU5NDQxNjY5NywKICAicHJvZmlsZUlkIiA6ICJjY2MxNGM2ZDUwMDE0MjBmYmMxYjkyMTM2Y2JmOWU4MSIsCiAgInByb2ZpbGVOYW1lIiA6ICJXaGlybGluZ0F0b2w5NDQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzdmN2E3YmM4YWM4NmYyM2NhN2JmOThhZmViNzY5NjAyMjdlMTgzMmZlMjA5YTMwMjZmNmNlYjhiZGU3NGY1NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9"
    private var towerEntities = mutableListOf<EntityArmorStand>()
    private var punching = false
    private var leftPunch = false

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (Utils.mc.thePlayer == null || !RiftConfig.AutoTowerPuncher) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift") return

        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (entity.getDistanceToEntity(Utils.mc.thePlayer) > 8 || entity !is EntityArmorStand) continue

            if (entity.getCurrentArmor(3) != null && towerEntities.size < 6) {
                val id = entity.getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner")
                    .getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0)
                    .getString("Value")

                if (id == TOWER_SKIN_ID && entity !in towerEntities) {
                    towerEntities.add(entity)
                }
            }
        }

        if (towerEntities.isNotEmpty()) {
            if (towerEntities.any { it.isDead || it.getDistanceToEntity(Utils.mc.thePlayer) > 8 }) {
                towerEntities.clear()  // Clear the list if any entity is dead or too far
                return
            }
        }

        if (!punching) {
            val lookingAt = getTargetEntity(5.0)?: return
            if (lookingAt !in towerEntities) {
                return
            }
            if(!isGameWindowFocused()) return

            punching = true

            // Send the use item action
            if(leftPunch) {
                performLeftClick()
            } else {
                performRightClick()
            }

            leftPunch = !leftPunch

            val minDelay = 1000.0 / RiftConfig.AutoTowerPuncherMin
            val maxDelay = 1000.0 / RiftConfig.AutoTowerPuncherMax

            Utils.setTimeout({
                punching = false
            }, Random.nextInt(minDelay.toInt(), maxDelay.toInt()).toLong())
        }
    }

    private fun isGameWindowFocused(): Boolean {
        if(Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU) || Keyboard.isKeyDown(Keyboard.KEY_LWIN) || Keyboard.isKeyDown(Keyboard.KEY_RWIN)) return false
        return Utils.mc.inGameHasFocus
    }

    // Function to simulate left click using Robot
    private fun performLeftClick() {
        try {
            val robot = Robot()
            robot.mousePress(InputEvent.BUTTON1_MASK)  // Left mouse button press
            robot.mouseRelease(InputEvent.BUTTON1_MASK)  // Left mouse button release
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to simulate right click using Robot
    private fun performRightClick() {
        try {
            val robot = Robot()
            robot.mousePress(InputEvent.BUTTON3_MASK)  // Right mouse button press
            robot.mouseRelease(InputEvent.BUTTON3_MASK)  // Right mouse button release
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getTargetEntity(range: Double): Entity? {
        val player: EntityPlayer = Utils.mc.thePlayer
        val start = Vec3(player.posX, player.posY + player.getEyeHeight(), player.posZ)
        val look = player.lookVec
        val end = start.addVector(look.xCoord * range, look.yCoord * range, look.zCoord * range)
        var hitEntity: Entity? = null
        var closestDistance = range
        for (entity in Utils.mc.theWorld.loadedEntityList) {
            if (entity === Utils.mc.thePlayer) continue
            if (entity.canBeCollidedWith()) {
                val collisionSize = entity.collisionBorderSize
                val boundingBox = entity.entityBoundingBox.expand(
                    collisionSize.toDouble(),
                    collisionSize.toDouble(),
                    collisionSize.toDouble()
                )
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

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (towerEntities.size == 0 || !RiftConfig.AutoTowerPuncherHighlight) return

        RenderUtils.drawLine(
            Utils.mc.thePlayer.getPositionEyes(event.partialTicks),
            towerEntities[0].positionVector.addVector(0.0,1.0,0.0),
            2,
            Color.GREEN,
            event.partialTicks
        )

        for(entity in towerEntities) {
            RenderUtils.drawSpecialBB(
                entity.position.up(),
                RiftConfig.AutoTowerPuncherHighlightColor,
                event.partialTicks
            )
        }
    }
}