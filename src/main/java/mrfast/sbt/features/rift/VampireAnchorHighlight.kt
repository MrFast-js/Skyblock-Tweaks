package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object VampireAnchorHighlight {

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.highlightVampireAnchors) return

        Utils.mc.theWorld.loadedEntityList.forEach {
            if (it is EntityArmorStand) {
                if (it.getCurrentArmor(3) != null) {
                    val id = it.getCurrentArmor(3).serializeNBT().getCompoundTag("tag").getCompoundTag("SkullOwner")
                        .getCompoundTag("Properties").getTagList("textures", 10).getCompoundTagAt(0).getString("Value")

                    if (id == "eyJ0aW1lc3RhbXAiOjE1ODM1OTc3NjczMDMsInByb2ZpbGVJZCI6IjIyZmQ2N2IxN2U2NzQ1ZmI5MmU3NDc3MTIzNDMwNTE4IiwicHJvZmlsZU5hbWUiOiJyYWtldGVuYmVuIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80OTJmMDhjMTI5NDgyOWQ0NzFhOGUwMTA5YTA2ZmI2YWU3MTdlNWZhZjNlMDgwODQwOGE2NmQ4ODkyMjdkYWM3IiwibWV0YWRhdGEiOnsibW9kZWwiOiJzbGltIn19fX0=") {
                        val skullPos = it.positionVector.add(Vec3(0.0, it.eyeHeight.toDouble(), 0.0))
                        val playerPos = Utils.mc.thePlayer.getPositionEyes(event.partialTicks)
                        GlStateManager.disableDepth()
                        if (RiftConfig.highlightVampireAnchorsTracer) {
                            RenderUtils.drawLine(
                                playerPos,
                                skullPos,
                                2,
                                RiftConfig.highlightVampireAnchorColor.get(),
                                event.partialTicks
                            )
                        }
                        val bb = it.entityBoundingBox.offset(0.0, 1.5, 0.0).expand(0.2, 0.0, 0.2)
                        RenderUtils.drawSpecialBB(bb, RiftConfig.highlightVampireAnchorColor.get(), event.partialTicks)
                        GlStateManager.enableDepth()
                    }
                }
            }
        }
    }
}