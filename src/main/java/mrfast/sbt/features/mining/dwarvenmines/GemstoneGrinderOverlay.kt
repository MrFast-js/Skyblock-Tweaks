package mrfast.sbt.features.mining.dwarvenmines

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.MiningConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getStringWidth
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.Event
import java.awt.Color

@SkyblockTweaks.EventComponent
object GemstoneGrinderOverlay {
    init {
        GemstoneOverlay()
    }

    class GemstoneOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            if (event !is GuiContainerBackgroundDrawnEvent) return

            val gemstones = mutableListOf<String>()
            for (i in 27..34) {
                val gemstone = event.gui!!.inventorySlots.getSlot(i)
                if (gemstone.stack != null) {
                    if (gemstone.stack.displayName.clean().trim()
                            .isEmpty() || gemstone.stack.displayName.clean() == "Gemstone Slot"
                    ) continue

                    gemstones.add(gemstone.stack.displayName)
                    val temp = mutableListOf<String>()

                    var isCostSection = false

                    // Already bought slot thats empty
                    if (gemstone.stack.getLore(false).size < 5) {
                        temp.add("§7Unused Slot")
                        temp.add(" ")
                    }

                    lore@ for (line in gemstone.stack.getLore(false)) {
                        if (line.clean().length < 2) continue

                        // Check if this line marks the start of the "Cost" section
                        if (line.contains("Cost", ignoreCase = true)) {
                            isCostSection = true
                            temp.add(line)
                            continue
                        }

                        // Collect lines within the "Cost" section
                        if (isCostSection) {
                            val cleanedLine = line.clean().trim()
                            if (cleanedLine.isEmpty() || line.contains("Click to unlock!", ignoreCase = true)) {
                                temp.add("")
                                break@lore // Exit if the line is empty or irrelevant
                            }
                            if (cleanedLine.contains("Coins")) {
                                temp.add(line)

                                if (temp[temp.size - 2].contains("Remove")) {
                                    temp.add("")
                                }
                            } else {
                                val split = cleanedLine.split("x")
                                val count = if (split.size > 1) split[1].trim().toInt() else 1
                                val itemId = ItemApi.getItemIdFromName(split[0].trim(), true) ?: continue
                                val price = ItemApi.getItemInfo(itemId)?.get("bazaarBuy")?.asDouble ?: continue
                                temp.add(line.replace(" Gemstone", "") + " §7- §6${(count * price).abbreviateNumber()}")
                            }
                        }
                    }

                    // Add only non-blank lines to the gemstones list
                    gemstones.addAll(temp)
                }
            }

            if (gemstones.isEmpty()) return

            val lines = mutableListOf<GuiUtils.Element>()

            gemstones.forEach {
                lines.add(GuiUtils.Element(5f, 5f + lines.size * 10, it, null, null))
            }


            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 200
                )

            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                (10 + 9.4 * lines.size).toFloat(),
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, (x + event.guiLeft + 180).toInt(), (y + event.guiTop).toInt())
            }
            GlStateManager.translate(0f, 0f, -52f)
        }

        override fun isActive(event: Event): Boolean {
            if (event !is GuiContainerBackgroundDrawnEvent) return false

            return (event.gui as GuiContainer).chestName() == "Gemstone Grinder" && MiningConfig.gemstoneOverlay
        }
    }
}