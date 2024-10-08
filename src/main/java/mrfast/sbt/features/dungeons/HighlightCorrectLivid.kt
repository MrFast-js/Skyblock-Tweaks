package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DungeonConfig.highlightCorrectLivid
import mrfast.sbt.config.categories.DungeonConfig.highlightCorrectLividColor
import mrfast.sbt.customevents.RenderEntityOutlineEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import net.minecraft.block.BlockStainedGlass
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.init.Blocks
import net.minecraft.item.EnumDyeColor
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object HighlightCorrectLivid {
    private var lividEntity: Entity? = null;

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (!LocationManager.inDungeons || LocationManager.dungeonFloor != 5 || !highlightCorrectLivid || event.phase != TickEvent.Phase.START) return

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
    fun onRenderEntityOutlines(event: RenderEntityOutlineEvent.Normal) {
        if (!LocationManager.inDungeons || LocationManager.dungeonFloor != 5 || !highlightCorrectLivid) return

        if (lividEntity != null) {
            event.queueEntityToOutline(lividEntity, highlightCorrectLividColor)
        }
    }
}