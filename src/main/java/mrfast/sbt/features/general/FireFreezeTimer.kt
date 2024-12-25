package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.config.categories.MiscellaneousConfig.fireFreezeVisualColor
import mrfast.sbt.customevents.UseItemAbilityEvent
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.toFormattedSeconds
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


@SkyblockTweaks.EventComponent
object FireFreezeTimer {
    private var activatedPos: Vec3? = null
    private var activatedAt = 0L

    @SubscribeEvent
    fun onItemUse(event: UseItemAbilityEvent) {
        if (!MiscellaneousConfig.fireFreezeVisual) return

        if (event.ability.itemId == "FIRE_FREEZE_STAFF") {
            activatedAt = System.currentTimeMillis()
            activatedPos = Utils.mc.thePlayer.positionVector
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!MiscellaneousConfig.fireFreezeVisual || activatedPos == null) return

        val pos = activatedPos!!

        val remainingTime = (5000 - (System.currentTimeMillis() - activatedAt))
        if (remainingTime < 0) return

        val seconds = remainingTime.toFormattedSeconds()

        RenderUtils.drawFilledCircleWithBorder(
            pos,
            5f,
            72,
            fireFreezeVisualColor.get(),
            fireFreezeVisualColor.get(),
            event.partialTicks
        )

        RenderUtils.draw3DString(
            "§e$seconds",
            pos.addVector(0.0, 1.0, 0.0),
            event.partialTicks
        )
    }
}

