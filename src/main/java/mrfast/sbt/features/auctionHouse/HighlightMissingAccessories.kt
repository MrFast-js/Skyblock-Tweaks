package mrfast.sbt.features.auctionHouse

import com.google.gson.JsonPrimitive
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.AccessoryApi
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object HighlightMissingAccessories {
    var show = false

    @SubscribeEvent
    fun onSlotDrawPost(event: SlotDrawnEvent.Post) {
        show = event.gui.chestName() == "Auctions Browser" || event.gui.chestName().contains("Auction View") || event.gui.chestName().contains("Auctions:")

        if (!MiscellaneousConfig.highlightMissingTalismans || !show) return

        if (event.slot.hasStack) {
            val id = event.slot.stack.getSkyblockId() ?: return

            if(AccessoryApi.missing.contains(JsonPrimitive(id))) {
                val baseColor = MiscellaneousConfig.highlightMissingTalismansColor.get()
                val color = Color(baseColor.red, baseColor.green, baseColor.blue, 100)
                GuiUtils.highlightSlot(event.slot, color)
            }
        }
    }

    @SubscribeEvent
    fun onTooltipDrawPost(event: ItemTooltipEvent) {
        if (!MiscellaneousConfig.highlightMissingTalismans || !show) return

        if (event.itemStack != null) {
            val id = event.itemStack.getSkyblockId()?.clean() ?: return

            if(AccessoryApi.missing.contains(JsonPrimitive(id))) {
                event.toolTip.add(1,"§c§lYou are missing this accessory!")
            }
        }
    }
}