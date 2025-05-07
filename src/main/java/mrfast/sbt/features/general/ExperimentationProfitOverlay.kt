package mrfast.sbt.features.general

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getInventory
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.getStringWidth
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.floor
import kotlin.math.max

@SkyblockTweaks.EventComponent
object ExperimentationProfitOverlay {
    private val itemValues = mutableMapOf<String, EnchantingItem>()
    const val maxFlipsShown = 20
    private var statusMessage = ""

    class EnchantingItem {
        var itemID = ""
        var itemName = ""
        var itemValue = 0.0
        var rngRequired = 0
        var coinsPerRng = 0.0
    }

    private var itemsInMenu = mutableListOf<ItemStack>()
    private val XP_REGEX = """Experimental XP: (?<current>[\d,]+)\/(?<required>[\d,]+)""".toRegex()
    private val SELECTED_XP_REGEX = """(?<current>[\d,]+)\/(?<required>[\d,k]+)""".toRegex()

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START || !GeneralConfig.experimentationOverlay || LocationManager.currentArea != "Your Island") return

        if (Utils.mc.currentScreen == null) {
            itemsInMenu.clear()
            return
        }

        val gui = Utils.mc.currentScreen
        if (gui !is GuiChest || !gui.chestName().contains("Experimentation Table RNG")) return

        val inventory = gui.getInventory()
        for (i in 0..inventory.sizeInventory) {
            val stack = inventory.getStackInSlot(i)
            if (stack == null || stack.displayName.clean().trim().isEmpty()) continue

            if (!itemsInMenu.contains(stack)) {
                itemsInMenu.add(stack)
            }
        }
        getItemValues()
    }

    private fun getItemValues() {
        itemsInMenu.forEach {
            statusMessage = "Checking ${it.displayName}.."

            val itemID = it.getSkyblockId()

            val enchantingItem = EnchantingItem()
            enchantingItem.itemID = itemID ?: return@forEach
            enchantingItem.itemName = it.displayName
            enchantingItem.itemValue = ItemUtils.getItemBasePrice(enchantingItem.itemID)

            it.getLore(true).forEach { line ->
                if (line.matches(XP_REGEX)) {
                    val rngRequired = line.getRegexGroups(XP_REGEX)?.get("required")?.value?.replace(",", "")?.toInt()
                    if (rngRequired != null) {
                        enchantingItem.rngRequired = rngRequired
                    }
                }
                if (line.trim().matches(SELECTED_XP_REGEX)) {
                    val rngRequired =
                        line.trim().getRegexGroups(SELECTED_XP_REGEX)?.get("required")?.value?.replace(",", "")
                            ?.replace("k", "000")?.toInt()
                    if (rngRequired != null) {
                        enchantingItem.rngRequired = rngRequired
                    }
                }
            }

            if (enchantingItem.rngRequired == 0 || enchantingItem.itemValue == 0.0) return@forEach

            enchantingItem.coinsPerRng = (enchantingItem.itemValue / enchantingItem.rngRequired)

            itemValues[enchantingItem.itemID] = enchantingItem
        }
    }

    init {
        ExperimentationOverlay()
    }

    class ExperimentationOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        private var scrollOffset = 0
        private var menuName = ""

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            // Cant store config as a long so we store it as a int and convert it to a long when needed
            val lines = mutableListOf(
                GuiUtils.Element(
                    5f, 5f, "§e§lExperimentation Coins/RNG", null, null
                )
            )

            if (itemValues.isEmpty()) {
                statusMessage = "§cFailed to get Forge Flips.."
                lines.add(
                    GuiUtils.Element(
                        5f, 20f, statusMessage, null, null
                    )
                )
            }

            if (itemValues.isNotEmpty()) {
                var sorted = itemValues.entries.sortedByDescending {
                    it.value.coinsPerRng
                }

                // Scrolling Logic
                val scrollAmount = Mouse.getDWheel()
                if (scrollAmount != 0) {
                    scrollOffset += -floor(scrollAmount / 120.0).toInt()

                    scrollOffset = scrollOffset.coerceIn(0, max(sorted.size - maxFlipsShown, 0))
                }

                if (sorted.isNotEmpty()) {
                    val endIndex =
                        (scrollOffset + maxFlipsShown).coerceAtMost(sorted.size) // Prevent out-of-bounds error
                    val startIndex = scrollOffset
                    sorted = sorted.subList(startIndex, endIndex)
                }

                var first = true
                sorted.forEach {
                    val itemName = it.value.itemName

                    val hoverText = mutableListOf(
                        itemName,
                        "§6Value: §e${it.value.itemValue.abbreviateNumber()}",
                        "§dRNG Required: §e${it.value.rngRequired.abbreviateNumber()}",
                        "§bCoins/RNG: §2${it.value.coinsPerRng.abbreviateNumber()}",
                    )

                    val yOffset = if (first) 4f else 2f
                    val y = GuiUtils.getLowestY(lines) + yOffset
                    val itemStack = ItemApi.createItemStack(it.value.itemID) ?: ItemStack(Items.apple)
                    lines.addAll(
                        listOf(
                            GuiUtils.Element(
                                7f + 9f,
                                y,
                                "$itemName§7 ┃ §2+${it.value.coinsPerRng.abbreviateNumber()}/RNG",
                                hoverText,
                                null
                            ),
                            GuiUtils.ItemStackElement(
                                itemStack,
                                3f,
                                y - 1f,
                                12,
                                12
                            )
                        )
                    )
                    first = false
                }
            }

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 250
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
            if (event !is GuiContainerBackgroundDrawnEvent || event.gui == null || !GeneralConfig.experimentationOverlay || LocationManager.currentArea != "Your Island") return false

            if (menuName != event.gui?.chestName()) scrollOffset = 0
            menuName = event.gui?.chestName()!!

            return event.gui!!.chestName().contains("Experimentation Table RNG")
        }
    }
}