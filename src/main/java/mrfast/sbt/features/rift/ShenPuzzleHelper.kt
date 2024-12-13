package mrfast.sbt.features.rift

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.RiftConfig
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.getInventory
import mrfast.sbt.utils.Utils.toString
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object ShenPuzzleHelper {
    private val buttons = listOf(BlockPos(-262, 20, -61), BlockPos(-262, 20, -62), BlockPos(-262, 20, -63), BlockPos(-262, 20, -64), BlockPos(-262, 20, -65), BlockPos(-262, 20, -66), BlockPos(-262, 20, -67))
    private val blocks = listOf(BlockPos(-263, 20, -61), BlockPos(-263, 20, -62), BlockPos(-263, 20, -63), BlockPos(-263, 20, -64), BlockPos(-263, 20, -65), BlockPos(-263, 20, -66), BlockPos(-263, 20, -67))
    val buttonStrings = buttons.map { it.toString() }
    val blockStrings = blocks.map { it.toString() }
    private val patterns =
        listOf(listOf(2, 3, 4, 5, 6, 7), listOf(2, 4, 6), listOf(3, 4, 5), listOf(3, 4, 7), listOf(3, 5, 6, 7))
    private var current = 0
    private var clickedButtons = mutableListOf<BlockPos>()


    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper) return
        val player = Utils.mc.thePlayer
        if (player.position.distanceSq(buttons[0]) > 400) return
        val selectedPattern = patterns[current]

        for ((i, button) in selectedPattern.withIndex()) {
            if (clickedButtons.map{it.toString()}.contains(buttonStrings[button-1]) || clickedButtons.map{it.toString()}.contains(blockStrings[button-1])) continue
            highLightButton(button, event.partialTicks)
            RenderUtils.draw3DString(
                "${i + 1}",
                buttons[button-1],
                event.partialTicks,
                true,
                true
            )
        }

    }

    @SubscribeEvent
    fun onGuiScreen(e: GuiScreenEvent) {
        if (e !is GuiScreenEvent.InitGuiEvent && e !is GuiScreenEvent.MouseInputEvent) return

        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper) return

        val player = Utils.mc.thePlayer ?: return
        if (player.position.distanceSq(buttons[0]) > 400) return

        val gui = e.gui ?: return
        if (gui !is GuiChest) return
        if(e is GuiScreenEvent.InitGuiEvent){
            clickedButtons.clear()
        }
        if (!gui.chestName().contains("Shen's")) return
        val chestInventory = gui.getInventory()
        val slots = listOf(20, 21, 22, 23, 24)
        for (slot in slots) {
            val item = chestInventory.getStackInSlot(slot) ?: continue
            for (line in item.getLore()) {
                if (line.contains("SELECTED")) {
                    current = slot - 20
                    //ChatUtils.sendClientMessage("Current pattern: $current")
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    fun onGuiClick(e: SlotClickedEvent){
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper) return
        if(e.gui !is GuiChest) return

        val player = Utils.mc.thePlayer
        if (player.position.distanceSq(buttons[0]) > 400) return

        //ChatUtils.sendClientMessage("Slot clicked: ${e.gui.chestName()}, slot {${e.slot.slotNumber}}")
        if (!e.gui.chestName().contains("Shen's")) return
        val chestInventory = e.gui.getInventory()
        if (e.slot.slotNumber in 20..24) {
            val item = chestInventory.getStackInSlot(e.slot.slotNumber) ?: return
            current = e.slot.slotNumber - 20
            //ChatUtils.sendClientMessage("Current pattern: $current")
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(e: PlayerInteractEvent) {
        if(e.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) return
        if (!LocationManager.inSkyblock || LocationManager.currentIsland != "The Rift" || !RiftConfig.shenPuzzleHelper) return

        val player = Utils.mc.thePlayer
        if (player.position.distanceSq(buttons[0]) > 400) return

        if (e.pos.toString() in buttons.map { it.toString() } || e.pos.toString() in blocks.map { it.toString() }) {
            //ChatUtils.sendClientMessage("Button clicked" )
            clickedButtons.add(e.pos)
        }



    }


    private fun highLightButton(index: Int, partialTicks: Float) {
        RenderUtils.drawSpecialBB(
            blocks[index-1],
            RiftConfig.shenButtonColor.get(),
            partialTicks
        )

    }

}