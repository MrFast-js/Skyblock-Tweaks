package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.getInventory
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Mouse


@SkyblockTweaks.EventComponent
object ShenPuzzleHelper {
    private val basePos = BlockPos(-262, 20, -61)
    private val buttons = List(7) { i -> basePos.add(0, 0, -i) }
    private val blocks = buttons.map { it.add(-1, 0, 0) }

    private val buttonStrings = buttons.map { it.toString() }
    private val blockStrings = blocks.map { it.toString() }

    private val patterns = listOf(
        listOf(2, 3, 4, 5, 6, 7),
        listOf(2, 4, 6),
        listOf(3, 4, 5),
        listOf(3, 4, 7),
        listOf(3, 5, 6, 7)
    )

    private var current = 0
    private var clickedButtons = mutableListOf<BlockPos>()

    private val itemSlots = listOf(20, 21, 22, 23, 24)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (shouldReturn()) return

        // Looking at reset button
        val rayTraceResult = Utils.getPlayer()!!.rayTrace(4.0, event.partialTicks)
        if (rayTraceResult != null && rayTraceResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            if(rayTraceResult.blockPos == BlockPos(-261, 20, -60) || rayTraceResult.blockPos == BlockPos(-262, 20, -60)) {
                if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
                    clickedButtons.clear()
                }
            }
        }

        val selectedPattern = patterns[current]

        for ((i, button) in selectedPattern.withIndex()) {
            if (clickedButtons.map { it.toString() }
                    .contains(buttonStrings[button - 1]) || clickedButtons.map { it.toString() }
                    .contains(blockStrings[button - 1])) continue

            highLightButton(button, event.partialTicks)
        }
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load?) {
        clickedButtons.clear()
    }

    @SubscribeEvent
    fun onGuiScreen(event: GuiScreenEvent) {
        if ((event !is GuiScreenEvent.InitGuiEvent && event !is GuiScreenEvent.MouseInputEvent) || shouldReturn()) return

        val gui = event.gui

        if (gui !is GuiChest || !gui.chestName().contains("Shen's")) return

        if (event is GuiScreenEvent.InitGuiEvent) {
            clickedButtons.clear()
        }

        for (slot in itemSlots) {
            val item = gui.getInventory().getStackInSlot(slot) ?: continue

            for (line in item.getLore()) {
                if (!line.contains("SELECTED")) continue

                current = itemSlots.indexOf(slot)
                break
            }
        }
    }

    @SubscribeEvent
    fun onGuiClick(event: SlotClickedEvent) {
        if (shouldReturn() || event.gui !is GuiChest || !event.gui.chestName().contains("Shen's")) return

        if (event.slot.slotNumber in 20..24) {
            current = itemSlots.indexOf(event.slot.slotNumber)
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if (e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || shouldReturn()) return

        if (e.pos.toString() in buttons.toList().map { it.toString() } || e.pos.toString() in blocks.toList()
                .map { it.toString() }) {
            clickedButtons.add(e.pos)
        }
    }

    private fun shouldReturn(): Boolean {
        if (Utils.isWorldLoaded()) {
            if (Utils.getPlayer()!!.position.distanceSq(buttons[0]) > 400) return true
        }

        return !LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper
    }


    private fun highLightButton(index: Int, partialTicks: Float) {
        RenderUtils.drawSpecialBB(
            blocks[index - 1],
            RiftConfig.shenButtonColor.get(),
            partialTicks
        )

    }

}