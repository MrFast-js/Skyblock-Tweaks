package mrfast.sbt.features.accessoryBag

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.AccessoryApi
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.Event
import org.lwjgl.input.Keyboard
import java.awt.Color
import java.awt.Desktop
import kotlin.math.ceil

@SkyblockTweaks.EventComponent
object AccessoryOverlay {
    init {
        AccessoryBagOverlay()
    }
    var onlyShowHighestLevel = false

    class AccessoryBagOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val lines = mutableListOf(
                GuiUtils.Element(5f, 5f, "§c§lMissing Talismans", null, null),
                GuiUtils.IconElement(
                    if(onlyShowHighestLevel) "§6§l✦" else "§c§l✧",
                    7f + 116f,
                    3f,
                    listOf(
                        if(onlyShowHighestLevel) "§eOnly Showing the Highest tier" else "§eShowing all tiers",
                        "§7Click to toggle"
                    ),
                    {
                        onlyShowHighestLevel = !onlyShowHighestLevel
                    },
                    backgroundColor = if(onlyShowHighestLevel) Color(255, 255, 85) else Color(255, 85, 85),
                    width = 8
                )
            )
            val sorted = (if(onlyShowHighestLevel) AccessoryApi.missingMax else AccessoryApi.missing).sortedBy {
                val key = it.asString

                if (ItemApi.liveAuctionData.has(key)) {
                    val data = ItemApi.liveAuctionData.get(key)!!
                    val item = data.asJsonObject?.get("default")?.asJsonObject ?: return@sortedBy null
                    val price = item.get("price").asLong
                    val magicPower = AccessoryApi.getMagicPowerValue(key)

                    return@sortedBy price / magicPower
                } else return@sortedBy null
            }

            var pricedItems = 0
            sorted.forEach {
                val key = it.asString
                val itemStack = ItemApi.createItemStack(key) ?: return@forEach
                val hoverText = mutableListOf<String>()

                val element = GuiUtils.ItemStackElement(
                    itemStack,
                    (pricedItems % 8f) * 16f + 4f,
                    (pricedItems / 8) * 16f + 15f,
                    16,
                    16,
                    onLeftClick = {
                        println("Clicked on $it")
                    },
                    hoverText = listOf(
                        itemStack.displayName
                    )
                )

                if (ItemApi.liveAuctionData.has(key)) {
                    val data = ItemApi.liveAuctionData.get(key)!!
                    val item = data.asJsonObject?.get("default")?.asJsonObject ?: return@forEach
                    val itemData = ItemApi.getItemInfo(key) ?: return@forEach
                    var price = item.get("price").asLong
                    val aucID = item.get("auc_id").asString
                    val magicPower = AccessoryApi.getMagicPowerValue(key)
                    val craftCost = ItemApi.getCraftCost(key)
                    val isShiftHeld = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)

                    element.onLeftClick = Runnable {
                        ChatUtils.sendPlayerMessage("/viewauction $aucID")
                    }
                    element.onRightClick = Runnable {
                        GuiUtils.closeGui()
                        ChatUtils.sendPlayerMessage("/ahs ${itemData.asJsonObject.get("displayname").asString.clean()}")
                    }

                    hoverText.add(itemStack.displayName)

                    if(itemData.get("crafttext")?.asString?.isNotEmpty() == true) {
                        hoverText.add("§c${itemData.get("crafttext").asString}")
                    }

                    if(isShiftHeld && craftCost != -1L) {
                        hoverText.add("§7Craft Cost: §6${craftCost.formatNumber()} coins")
                        price = craftCost
                    } else {
                        hoverText.add("§7Price: §6${price.formatNumber()} coins")
                    }

                    hoverText.add("§d§l$magicPower Magic Power")
                    hoverText.add("§3${(price / magicPower).abbreviateNumber()} §bCoins/MP")

                    if (isShiftHeld && craftCost != -1L) {
                        hoverText.add("§e§l[Click] §r§7to view recipe")

                        element.onLeftClick = Runnable {
                            ChatUtils.sendPlayerMessage("/viewrecipe ${it.asString}")
                            GuiUtils.closeGui()
                        }
                        element.onRightClick = null
                    } else {
                        hoverText.add("§a§l[Left-Click] §r§7to open BIN")
                        hoverText.add("§b§l[Right-Click] §r§7to search AH")
                        if (craftCost != -1L) hoverText.add("§7(Hold §e§lShift§7 for more info)")
                    }
                } else return@forEach

                element.hoverText = hoverText

                lines.add(element)
                pricedItems++
            }
            lines.add(
                GuiUtils.Element(
                    5f,
                    GuiUtils.getLowestY(lines) + 5f,
                    "§c§lUnpriced Talismans",
                    null,
                    null
                )
            )

            var unpricedItems = 0
            val newY = GuiUtils.getLowestY(lines) + 5f

            sorted.forEach {
                val key = it.asString
                val itemStack = ItemApi.createItemStack(key) ?: return@forEach
                val hoverText = mutableListOf<String>()

                val element = GuiUtils.ItemStackElement(
                    itemStack,
                    (unpricedItems % 8f) * 16f + 4f,
                    (unpricedItems / 8) * 16f + newY,
                    16,
                    16
                )

                if (!ItemApi.liveAuctionData.has(itemStack.getSkyblockId())) {
                    val magicPower = AccessoryApi.getMagicPowerValue(itemStack.getSkyblockId()!!)
                    val item = ItemApi.getItemInfo(key) ?: return@forEach

                    hoverText.add(itemStack.displayName)
                    if(item.get("crafttext")?.asString?.isNotEmpty() == true) {
                        hoverText.add("§c${item.get("crafttext").asString}")
                    }
                    hoverText.add("§d§l$magicPower Magic Power")
                } else return@forEach

                val craftCost = ItemApi.getCraftCost(key)

                if (craftCost != -1L) {
                    hoverText.add("§6Craft Cost: §f${ItemApi.getCraftCost(key).formatNumber()}")
                    hoverText.add("§e§l[Click] §r§7to view recipe")

                    element.onLeftClick = Runnable {
                        ChatUtils.sendPlayerMessage("/viewrecipe ${it.asString}")
                    }
                } else {
                    hoverText.add("§e§l[Click] §r§7to open wiki")
                    element.onLeftClick = Runnable {
                        Desktop.getDesktop().browse(java.net.URI.create("https://wiki.hypixel.net/${it.asString}"))
                    }
                }

                element.hoverText = hoverText

                lines.add(element)
                unpricedItems++
            }

            val width = 8 * 16f + 6f

            val chestEvent = (event as GuiContainerBackgroundDrawnEvent)
            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width,
                10 + 25 + ceil(pricedItems / 8f) * 16f + ceil(unpricedItems / 8f) * 16f,
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
            if (event !is GuiContainerBackgroundDrawnEvent || !MiscellaneousConfig.accessoryBagOverlay) return false

            return AccessoryApi.isAccessoryBagOpen()
        }
    }
}