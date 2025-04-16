package mrfast.sbt.features.general

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.managers.TradeManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.getStringWidth
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object LowballOverlay {
    private var theirItemValues = mutableMapOf<ItemStack, Long>()

    @SubscribeEvent
    fun onPostSlotDrawn(event: SlotDrawnEvent.Post) {
        if (event.slot.slotNumber == 0 && TradeManager.inTradeMenu) {
            val new = mutableMapOf<ItemStack, Long>()
            TradeManager.theirSlotIDs.forEach {
                val slot = event.gui.inventorySlots.getSlot(it)
                if (!slot.hasStack) return@forEach

                val itemStack = slot.stack
                val price = ItemUtils.getSuggestListingPrice(itemStack)

                if (price != null) {
                    val value = price.get("price").asLong
                    new[itemStack] = value
                }
            }
            theirItemValues = new
        }
    }

    init {
        LowballingOverlay()
    }

    var offerIndex = 3
    var offerPercents = listOf(10, 30, 40, 50, 60, 75, 80)

    class LowballingOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val offerPercentage = GeneralConfig.lowballingOverlayOfferPercentage / 100.0
            // Cant store config as a long so we store it as a int and convert it to a long when needed
            val lines = mutableListOf(
                GuiUtils.Element(
                    5f, 2f, "§e§lLowball Overlay", null, null
                )
            )
            val button = GuiUtils.Element(
                0f + "§e§lLowball Overlay".getStringWidth(),
                2f,
                "§7${(offerPercentage * 100).toInt()}%",
                listOf("§7This is the percentage of the item value you are offering"),
                {
                    offerIndex = (offerIndex + 1) % offerPercents.size
                    GeneralConfig.lowballingOverlayOfferPercentage = offerPercents[offerIndex]
                },
                drawBackground = true,
            )
            button.width = 16
            lines.add(
                button
            )

            var first = true
            theirItemValues.forEach { (itemStack, price) ->
                val itemValue = price.abbreviateNumber()
                val itemOffer = (price * offerPercentage).abbreviateNumber()

                val yOffset = if (first) 4f else 2f
                val y = GuiUtils.getLowestY(lines) + yOffset
                lines.addAll(
                    listOf(
                        GuiUtils.Element(
                            7f + 11f,
                            y,
                            "§bOffer: $itemOffer §aSell: $itemValue",
                            null,
                            null
                        ),
                        GuiUtils.ItemStackElement(
                            itemStack,
                            3f,
                            y - 1f,
                            12,
                            12,
                            itemStack.getLore()
                        )
                    )
                )
                first = false
            }
            if (theirItemValues.isEmpty()) {
                lines.add(
                    GuiUtils.Element(
                        5f,
                        GuiUtils.getLowestY(lines) + 3f,
                        "§cWaiting for items..",
                        null,
                        null
                    )
                )
            } else {
                lines.add(
                    GuiUtils.Element(
                        5f,
                        GuiUtils.getLowestY(lines) + 3f,
                        "§6§lTotal Offer: ${theirItemValues.values.sumOf { (it * offerPercentage).toLong() }.abbreviateNumber()}",
                        null,
                        null
                    )
                )
            }

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    112, 250
                )

            val chestEvent = (event as GuiContainerBackgroundDrawnEvent)

            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                GuiUtils.getLowestY(lines) + 2f,
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, (x + chestEvent.guiLeft + 180).toInt(), (y + chestEvent.guiTop).toInt())
            }
            GlStateManager.translate(0f, 0f, -52f)
        }

        override fun isActive(event: Event): Boolean {
            if (event !is GuiContainerBackgroundDrawnEvent || event.gui == null || !GeneralConfig.lowballingOverlay || !TradeManager.inTradeMenu) return false

            return true
        }
    }
}