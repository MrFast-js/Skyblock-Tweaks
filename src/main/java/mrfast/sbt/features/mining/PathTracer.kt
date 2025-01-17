package mrfast.sbt.features.mining

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RenderingConfig
import mrfast.sbt.config.categories.RenderingConfig.pathRenderRange
import mrfast.sbt.config.categories.RenderingConfig.pathThroughWalls
import mrfast.sbt.managers.DataManager
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object PathTracer {
    var pathPoints = mutableListOf<Vec3>()
    var pathsAndPoints = JsonObject()
    var recordingMovement = false
    var creatingPath = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || Utils.mc.thePlayer == null || !recordingMovement) return

        if(TickManager.tickCount % 4 != 0) return

        val pos = Utils.mc.thePlayer.positionVector.add(Vec3(0.0, 0.2, 0.0))
        if (!pathPoints.contains(pos)) {
            pathPoints.add(pos)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (Utils.mc.thePlayer != null) {
            GlStateManager.pushMatrix()
            if (pathThroughWalls) {
                GlStateManager.disableDepth()
            }
            var previousPoint: Vec3? = null
            for (point in pathPoints) {
                if (Utils.mc.thePlayer.positionVector.distanceTo(point) > pathRenderRange) {
                    previousPoint = null
                    continue
                }
                if (previousPoint == null) {
                    previousPoint = point
                    continue
                } else {
                    if (previousPoint.distanceTo(point) > 3) {
                        RenderUtils.drawLine(
                            previousPoint,
                            point,
                            2,
                            RenderingConfig.pathTracingColor2.get(),
                            event.partialTicks
                        )
                    } else {
                        RenderUtils.drawLine(
                            previousPoint,
                            point,
                            2,
                            RenderingConfig.pathTracingColor1.get(),
                            event.partialTicks
                        )
                    }
                    previousPoint = point
                }
            }
            if (pathThroughWalls) {
                GlStateManager.enableDepth()
            }
            GlStateManager.popMatrix()
        }
    }

    init {
        pathsAndPoints = DataManager.getDataDefault("paths", JsonObject()) as JsonObject
    }

    fun savePath(pathName: String) {
        pathPoints.let {
            val jsonArray = JsonArray()
            it.forEach { vec ->
                val jsonObject = JsonObject().apply {
                    addProperty("x", vec.xCoord)
                    addProperty("y", vec.yCoord)
                    addProperty("z", vec.zCoord)
                }
                jsonArray.add(jsonObject)
            }
            pathsAndPoints.add(pathName, jsonArray)
            ChatUtils.sendClientMessage("§3Saved path §a$pathName", true)
            savePaths()
        }
    }

    fun loadPath(pathName: String) {
        val jsonArray = pathsAndPoints.getAsJsonArray(pathName) ?: return
        val list = mutableListOf<Vec3>()
        jsonArray.forEach { element ->
            val obj = element.asJsonObject
            val vec = Vec3(
                obj["x"].asDouble,
                obj["y"].asDouble,
                obj["z"].asDouble
            )
            list.add(vec)
        }
        pathPoints = list
        ChatUtils.sendClientMessage("§3Loaded path §a$pathName", true)
    }

    fun savePaths() {
        DataManager.saveData("paths", pathsAndPoints)
    }
}