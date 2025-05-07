package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DungeonConfig.highlightCorrectLivid
import mrfast.sbt.config.categories.DungeonConfig.highlightCorrectLividColor
import mrfast.sbt.config.categories.DungeonConfig.highlightCorrectLividLine
import mrfast.sbt.customevents.RenderEntityModelEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.OutlineUtils
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object HighlightCorrectLivid {
    private var lividEntity: Entity? = null;

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!LocationManager.inDungeons || LocationManager.dungeonFloor != 5 || !highlightCorrectLivid || event.phase != TickEvent.Phase.START || Utils.mc.theWorld == null) return

        if(TickManager.tickCount % 20 != 0) return

        val state: IBlockState = Utils.mc.theWorld.getBlockState(BlockPos(5, 108, 42))
        if (state.block != Blocks.stained_glass) return

        val color = state.getValue(BlockStainedGlass.COLOR)
        val lividType = when (color) {
            EnumDyeColor.WHITE -> "Vendetta"
            EnumDyeColor.MAGENTA, EnumDyeColor.PINK -> "Crossed"
            EnumDyeColor.RED -> "Hockey"
            EnumDyeColor.SILVER, EnumDyeColor.GRAY -> "Doctor"
            EnumDyeColor.GREEN -> "Frog"
            EnumDyeColor.LIME -> "Smile"
            EnumDyeColor.BLUE -> "Scream"
            EnumDyeColor.PURPLE -> "Purple"
            EnumDyeColor.YELLOW -> "Arcade"
            else -> return
        }

        for (mob in Utils.mc.theWorld.playerEntities) {
            if (mob.name.contains(lividType)) {
                lividEntity = mob
                break
            }
        }
    }

    @SubscribeEvent
    fun onRenderEntityModel(event: RenderEntityModelEvent) {
        if (!LocationManager.inDungeons || LocationManager.dungeonFloor != 5 || !highlightCorrectLivid) return

        if (lividEntity != null && lividEntity == event.entity) {
            OutlineUtils.outlineEntity(event, highlightCorrectLividColor.get())
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationManager.inDungeons || LocationManager.dungeonFloor != 5 || !highlightCorrectLivid || !highlightCorrectLividLine) return

        if (lividEntity != null && lividEntity!!.isEntityAlive && Utils.mc.thePlayer.canEntityBeSeen(lividEntity)) {
            val skullPos = lividEntity!!.positionVector.add(Vec3(0.0, lividEntity!!.eyeHeight.toDouble(), 0.0))
            val playerPos = Utils.mc.thePlayer.getPositionEyes(event.partialTicks)
            GlStateManager.disableDepth()
            RenderUtils.drawLine(
                playerPos,
                skullPos,
                2,
                highlightCorrectLividColor.get(),
                event.partialTicks
            )
            GlStateManager.enableDepth()
        }
    }
}