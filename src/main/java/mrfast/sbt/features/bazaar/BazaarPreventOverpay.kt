package mrfast.sbt.features.bazaar

import com.google.gson.JsonObject
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.LevelingUtils.roundToTwoDecimalPlaces
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getInventory
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.item.ItemStack
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

object BazaarPreventOverpay {
    private var inBuyItNowMenu = false
    private var viewedProductPriceInfo: JsonObject? = null
    private var viewedProductPPU: Double? = null
    private var requiredClicks = mutableMapOf<ItemStack, Int>()

    val threshold = 1 + ((MiscellaneousConfig.bazaarMaxOverpayPercent) / 100)

    @SubscribeEvent
    fun onSlotDrawn(event: SlotDrawnEvent.Post) {
        if (MiscellaneousConfig.bazaarManipulationProtection && inBuyItNowMenu) {
            val normalBuySlots = listOf(10, 12, 14, 13)
            if (normalBuySlots.contains(event.slot.slotIndex) && event.slot.hasStack) {
                val averageBuyPricePerUnit = viewedProductPriceInfo!!.get("avg_buyPricePerUnit").asDouble
                val itemOptionPPU = getStackPPU(event.slot.stack)

                if (itemOptionPPU > averageBuyPricePerUnit * threshold && event.slot.yDisplayPosition < 80) {
                    GuiUtils.highlightSlot(event.slot, Color(255, 0, 0, 100))
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (Utils.mc.thePlayer?.openContainer == null && inBuyItNowMenu) {
            resetData()
        }
    }

    fun getStackPPU(item: ItemStack): Double {
        var ppu = "0.0"
        for (line in item.getLore()) {
            if (line.clean().startsWith("Per unit: ")) {
                ppu = line.clean().split(" ")[2]
                break
            }
            if (line.clean().startsWith("Price: ")) {
                ppu = line.clean().split(" ")[1]
                break
            }
            if (line.clean().startsWith("Price per unit: ")) {
                ppu = line.clean().split(" ")[3]
                break
            }
        }

        return ppu.replace(",", "").toDouble()
    }

    @SubscribeEvent
    fun onTooltip(event: ItemTooltipEvent) {
        val itemName = event.itemStack.displayName.clean()

        if (MiscellaneousConfig.bazaarManipulationProtection && inBuyItNowMenu) {
            if (itemName.startsWith("Buy") || itemName.equals("Fill my inventory!") || itemName.equals("Custom Amount")) {
                val optionPPU = getStackPPU(event.itemStack)
                val averageBuyPricePerUnit = viewedProductPriceInfo!!.get("avg_buyPricePerUnit").asDouble

                // Add buy options that are more expensive, set required clicks to 3
                if (optionPPU > averageBuyPricePerUnit * threshold) {
                    if (!requiredClicks.contains(event.itemStack)) requiredClicks[event.itemStack] = 0

                    event.toolTip.add(
                        1,
                        "§c§lThis option is §e§l${(((optionPPU / averageBuyPricePerUnit) - 1) * 100).roundToTwoDecimalPlaces()}%§c§l overpriced!"
                    )
                    event.toolTip.add(2, "§c§lClick §e§l${3 - requiredClicks[event.itemStack]!!}x§c§l to buy anyway")
                }
            }
        }
        if (MiscellaneousConfig.bazaarAveragePPU && itemName == "Buy Instantly") {
            val averageBuyPricePerUnit = viewedProductPriceInfo!!.get("avg_buyPricePerUnit").asDouble
            val numberFormat = NumberFormat.getNumberInstance(Locale.US) as DecimalFormat
            numberFormat.applyPattern("#,##0.0")
            val avgPricePerUnitString = numberFormat.format(averageBuyPricePerUnit)

            event.toolTip.add(4, "§7Avg. Per Unit: §6$avgPricePerUnitString coins")
        }
    }

    /*
     * Prevent Clicks on overpriced stacks, 3 clicks possible on each option
     */
    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if (MiscellaneousConfig.bazaarManipulationProtection && inBuyItNowMenu) {
            if (requiredClicks.contains(event.slot.stack)) {
                val blocked = requiredClicks[event.slot.stack]!!
                requiredClicks[event.slot.stack] = blocked + 1

                // Reset Other Required Clicks
                for (itemStack in requiredClicks.keys) {
                    if (itemStack != event.slot.stack) requiredClicks[itemStack] = 0
                }

                if (blocked < 2) {
                    event.isCanceled = true
                    Utils.playSound("mob.villager.no", 1.0)
                } else {
                    requiredClicks[event.slot.stack] = 0
                }
            }
        }
    }

    @SubscribeEvent
    fun onContainerDrawn(event: GuiContainerBackgroundDrawnEvent) {
        if (event.gui !is GuiChest || !MiscellaneousConfig.bazaarManipulationProtection) return
        val containerName = (event.gui as GuiContainer).chestName()
        val inventory = (event.gui as GuiChest).getInventory()
        val inBazaar =
            (containerName.contains(" ➜ ") && (inventory.getStackInSlot(10)?.displayName?.clean() == "Buy Instantly" || inventory.getStackInSlot(
                10
            )?.displayName?.clean() == "Buy only one!")) || containerName == "Confirm Instant Buy"

        // Set and get the products info when viewing it in the bazaar
        if (inBazaar) {
            viewedProductPPU = 0.0
            inBuyItNowMenu =
                inventory.getStackInSlot(10)?.displayName?.clean() == "Buy only one!" || containerName == "Confirm Instant Buy"

            val buyItNowStack = inventory.getStackInSlot(10) ?: return
            val itemStack = inventory.getStackInSlot(13) ?: return

            val itemId = itemStack.getSkyblockId() ?: buyItNowStack.getSkyblockId()

            viewedProductPriceInfo = ItemApi.getItemPriceInfo(itemId!!)
            if (viewedProductPriceInfo == null) {
                resetData()
                return
            }

            viewedProductPPU = getStackPPU(itemStack)
        }
    }

    private fun resetData() {
        inBuyItNowMenu = false
        viewedProductPriceInfo = null
        viewedProductPPU = null
        requiredClicks.clear()
    }
}