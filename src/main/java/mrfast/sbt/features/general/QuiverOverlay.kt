package mrfast.sbt.features.general

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.config.categories.MiscellaneousConfig.quiverOverlay
import mrfast.sbt.config.categories.MiscellaneousConfig.quiverOverlayType
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

@SkyblockTweaks.EventComponent
object QuiverOverlay {
    private var currentArrow = ""
    private var currentArrowCount = 0
    private var currentArrowId = ""
    val QUIVER_REGEX = """^§8Quiver.*""".toRegex()
    val ACTIVE_ARROW_REGEX = """§7Active Arrow: (.+?) §7\(§e(\d+)§7\)""".toRegex()

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationManager.inSkyblock || Utils.mc.theWorld == null) return

        for (itemStack in Utils.mc.thePlayer.inventory.mainInventory) {
            if (itemStack == null || !itemStack.hasDisplayName() || !itemStack.displayName.matches(QUIVER_REGEX)) continue

            for (line in itemStack.getLore()) {
                if (!line.matches(ACTIVE_ARROW_REGEX)) continue

                val match = line.getRegexGroups(ACTIVE_ARROW_REGEX) ?: continue
                currentArrowCount = match[2]!!.value.toInt()
                if (currentArrow != match[1]!!.value) {
                    currentArrow = match[1]!!.value
                    currentArrowId = ItemApi.getItemIdFromName(currentArrow) ?: "UNKNOWN"
                }
            }
        }
    }

    init {
        QuiverOverlay()
    }

    class QuiverOverlay : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Quiver Overlay"
            this.addToList()
            this.height = 16
        }

        override fun draw() {
            GuiUtils.renderItemStackOnScreen(ItemApi.createItemStack(currentArrowId), 0f, 0f, 16f, 16f)
            var display = (if (quiverOverlayType) "$currentArrow " else "") + "§r§7x${currentArrowCount.formatNumber()}"
            if (currentArrow == "") display = ""

            GuiUtils.drawText(display, 17f, 3f, GuiUtils.TextStyle.DROP_SHADOW)
            this.width = Utils.mc.fontRendererObj.getStringWidth(display.clean()) + 17
        }

        override fun isActive(): Boolean {
            return quiverOverlay && LocationManager.inSkyblock
        }

        override fun isVisible(): Boolean {
            // Stop from showing if no held bow
            if (MiscellaneousConfig.quiverOverlayOnlyBow) {
                val held: ItemStack = Utils.mc.thePlayer.heldItem ?: return false
                if (held.item !is ItemBow) {
                    return false
                }
            }

            return true
        }
    }
}