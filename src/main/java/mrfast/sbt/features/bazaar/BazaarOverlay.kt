package mrfast.sbt.features.bazaar

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.SignDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ChatComponentText
import net.minecraftforge.fml.common.eventhandler.Event
import java.awt.Color

@SkyblockTweaks.EventComponent
object BazaarOverlay {
    init {
        BazaarOverlayGui()
    }
    class BazaarOverlayGui : OverlayManager.Overlay() {
        init {
            this.x = 50.0
            this.y = 65.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.SIGN)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val totalWidth = 200f
            val elementWidth = 50
            val elementY = 5f

            val texts = listOf("§a§l71,680", "§e53,760", "§e35,840", "§c§l17,920").reversed()
            val lines = mutableListOf<GuiUtils.Element>()

            for (i in texts.indices) {
                val x = 5f + i * elementWidth // small left padding (2f)
                val lore = mutableListOf("${texts[i]}x")
                val value = texts[i].clean().replace(",", "").toInt()
                val stacks = value / 64

                if(i == texts.size - 1) {
                    lore.add("§7${stacks.formatNumber()} Stacks (Max)")
                } else {
                    lore.add("§7${stacks.formatNumber()} Stacks (${i+1}/4 Max)")
                }
                lore.add("§6§lClick to set!")

                lines.add(
                    GuiUtils.Element(
                        x,
                        elementY,
                        texts[i],
                        lore,
                        {
                            // Set the sign text and close the GUI
                            val sign = (event as SignDrawnEvent).sign
                            sign.signText[0] = ChatComponentText(texts[i].clean().replace(",", ""))
                            sign.markDirty()
                            GuiUtils.closeGui()
                        },
                        drawBackground = true
                    )
                )
            }

            val sr = ScaledResolution(Utils.mc)

            GlStateManager.translate(-150f, 120f, 0f)

            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                0f,
                0f,
                totalWidth,
                20f,
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, x.toInt() + (sr.scaledWidth / 2) - 150, y.toInt() + 120)
            }

            GlStateManager.translate(150f, -120f, 0f)
        }

        override fun isActive(event: Event): Boolean {
            if (!MiscellaneousConfig.bazaarBuyAmountOverlay || event !is SignDrawnEvent) return false
            if (event.sign.signText[2].unformattedText == "Enter amount") {
                return true
            }
            return false
        }
    }
}