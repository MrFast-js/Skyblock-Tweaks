package mrfast.sbt.features.dungeons

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent


// Shows when to use fire freeze on F2,F3,M2,M3 in order to freeze boss perfectly
/*
 @TODO: Only show when having a fire freeze in inventory
 */
@SkyblockTweaks.EventComponent
object FireFreezeHelper {
    private val textScale = 2.0
    private val triggersAndTimings = mapOf(
        "[BOSS] The Professor: Oh? You found my Guardians' one weakness?" to 8, // Floor 3
        "[BOSS] Scarf: Those toys are not strong enough I see." to 9 // Floor 2
    )
    private var shouldFireFreeze = false
    private var currentDisplayText = ""

    @SubscribeEvent(receiveCanceled = true)
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (!LocationManager.inDungeons || event.type.toInt() == 2 || !DungeonConfig.fireFreezeTimer) return

        val text = event.message.unformattedText

        if (triggersAndTimings.contains(text)) {
            val time = triggersAndTimings[text] ?: -1
            for (i in 1..time) {
                val threshold = time - 2
                val count =
                    if (i >= threshold) "§aFire Freeze Now!" else "§cFire Freeze in " + (threshold - i) + " seconds"

                Utils.setTimeout({
                    currentDisplayText = count

                    if (i == threshold) shouldFireFreeze = true
                    if (i == time) currentDisplayText = ""
                }, (i * 880).toLong())
            }
        }
    }

    @SubscribeEvent
    fun onLoad(event: WorldEvent.Load?) {
        currentDisplayText = ""
        shouldFireFreeze = false
    }

    init {
        FireFreezeTimer()
    }

    class FireFreezeTimer : GuiManager.Element() {
        init {
            this.relativeX = 0.373
            this.relativeY = 0.522
            this.elementName = "Fire Freeze Timer"
            this.needsExample = true
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT * 2 + 2
            this.width = Utils.mc.fontRendererObj.getStringWidth("Fire Freeze in 5 seconds!") * 2 + 2
            this.addToList()
        }

        override fun draw() {
            GlStateManager.scale(textScale, textScale, 1.0)
            val centerX = this.width / 4f

            GuiUtils.drawText(
                currentDisplayText, centerX, 0f, GuiUtils.TextStyle.DROP_SHADOW,
                GeneralConfig.effectiveHealthNumberColor.get(), centered = true
            )
            GlStateManager.scale(1 / textScale, 1 / textScale, 1.0)
        }

        override fun drawExample() {
            GlStateManager.scale(textScale, textScale, 1.0)
            val centerX = this.width / 4f

            GuiUtils.drawText(
                "§cFire Freeze in 5 seconds!", centerX, 0f, GuiUtils.TextStyle.DROP_SHADOW,
                GeneralConfig.effectiveHealthNumberColor.get(), centered = true
            )
            GlStateManager.scale(1 / textScale, 1 / textScale, 1.0)
        }

        override fun isActive(): Boolean {
            return DungeonConfig.fireFreezeTimer
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}