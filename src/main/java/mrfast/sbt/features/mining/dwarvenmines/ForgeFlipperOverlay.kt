package mrfast.sbt.features.mining.dwarvenmines

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.MiningConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getInventory
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.getStringWidth
import mrfast.sbt.utils.Utils.toFormattedDuration
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.floor
import kotlin.math.max

@SkyblockTweaks.EventComponent
object ForgeFlipperOverlay {
    private val forgeFlips = mutableMapOf<String, ForgeFlip>()
    const val maxFlipsShown = 20
    private var statusMessage = ""

    class ForgeFlip {
        var output = ""
        var outputValue = 0.0

        var secondsDuration = 0
        var requiredItems = mutableMapOf<String, Int>()
        var itemCost = 0.0
        var totalCost = 0.0
        var profit = 0
        var coinsPerHour = 0.0
        var hotmRequired = 0
    }

    private var itemsInMenu = mutableListOf<String>()
    private val forgeMenuNames = listOf(
        "Refining",
        "Gear",
        "Perfect Gemstones",
        "Forging",
        "Reforge Stones",
        "Pets",
        "Tools",
        "Drill Parts",
        "Other"
    )

    @SubscribeEvent
    fun onGuiDraw(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) return
        if (!MiningConfig.forgeFlipperOverlay || LocationManager.currentIsland != "Dwarven Mines") return

        val gui = Utils.mc.currentScreen
        if (gui !is GuiChest) return

        itemsInMenu.clear()
        val inventory = gui.getInventory()
        for (i in 0..inventory.sizeInventory) {
            val stack = inventory.getStackInSlot(i)
            if (stack != null) {
                itemsInMenu.add(stack.displayName.clean())
            }
        }
    }

    fun findForgeFlips() {
        statusMessage = "Getting Skyblock Forge Recipes.."
        ItemApi.getSkyblockItems().entrySet().forEach {
            statusMessage = "Checking ${it.key}.."
            if (!it.value.asJsonObject.has("recipes")) return@forEach

            val recipes = it.value.asJsonObject.getAsJsonArray("recipes")
            if (recipes.size() == 0) return@forEach

            val recipe = recipes[0].asJsonObject
            if (recipe.get("type").asString != "forge") return@forEach

            val forgeFlip = ForgeFlip()
            forgeFlip.output = recipe.get("overrideOutputId").asString
            forgeFlip.secondsDuration = recipe.get("duration").asInt

            if (it.value.asJsonObject.has("crafttext")) {
                val craftText = it.value.asJsonObject.get("crafttext").asString
                if (craftText.matches("""Requires: HotM (.*)""".toRegex())) {
                    val hotmLevel =
                        craftText.getRegexGroups("""Requires: HotM (.*)""".toRegex())?.get(1)?.value?.toInt()
                    if (hotmLevel != null) {
                        forgeFlip.hotmRequired = hotmLevel
                    }
                }
            }

            var requiresSoulboundItem = false
            recipe.get("inputs").asJsonArray.forEach { item ->
                val item = item.asString
                val itemID = item.split(":")[0]
                val count = item.split(":")[1].toDouble().toInt()

                if (ItemUtils.getItemBasePrice(itemID) != -1.0) {
                    forgeFlip.requiredItems[itemID] = count
                    forgeFlip.itemCost += (ItemUtils.getItemBasePrice(itemID, false) * count).toInt()
                } else {
                    requiresSoulboundItem = true
                }
            }
            // Dont show the pet if its upgrade requires a soulbound item
            if (requiresSoulboundItem) return@forEach

            // Add the cost of the items to the total cost
            forgeFlip.totalCost += forgeFlip.itemCost

            val outputValue = ItemUtils.getItemBasePrice(forgeFlip.output)

            // Dont show if no price data was found for the input pet or the output pet
            if (outputValue == -1.0) return@forEach
            forgeFlip.outputValue = outputValue

            if (forgeFlip.totalCost < outputValue) {
                forgeFlip.profit = (outputValue - forgeFlip.totalCost).toInt()
            }
            if(forgeFlip.profit <= 0) return@forEach

            val minutes = forgeFlip.secondsDuration.toDouble() / 60.0
            val hours = minutes / 60.0
            forgeFlip.coinsPerHour = forgeFlip.profit / hours

            val displayName = it.value.asJsonObject.get("displayname").asString.clean()
            forgeFlips[displayName] = forgeFlip
            statusMessage = "Done"
        }
    }

    init {
        ForgeOverlay()
    }

    class ForgeOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        // 0 = most coins/hr
        // 1 = most profit
        private var sortingType = 0
        private var hotmMax = MiningConfig.forgeFlipperMaxHotm
        private var scrollOffset = 0
        private var maxPriceLong = 10_000_000L

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            // Cant store config as a long so we store it as a int and convert it to a long when needed
            maxPriceLong = MiningConfig.forgeFlipperMaxPrice * 1_000_000L
            val lines = mutableListOf(
                GuiUtils.Element(
                    5f, 5f, "§e§lForge Flips", null, null
                ),
                GuiUtils.Element(
                    80f, 5f, "§c" + if (sortingType == 0) "Coins/hr" else "Profit",
                    listOf("§cClick to change sorting type"),
                    {
                        sortingType = if (sortingType == 0) 1 else 0
                        scrollOffset = 0
                    },
                    drawBackground = true,
                )
            )
            // Base the X position off the previous element width included
            lines.add(
                GuiUtils.Element(
                    lines[1].x + lines[1].width + 7f, 5f, "§dHOTM $hotmMax",
                    listOf("§cHides Flips that require a HOTM level above this value"),
                    {
                        hotmMax = (hotmMax % 10) + 1
                        MiningConfig.forgeFlipperMaxHotm = hotmMax

                        SkyblockTweaks.config.saveConfig()

                        // Reset scroll offset
                        scrollOffset = 0
                    },
                    drawBackground = true,
                )
            )
            // Base the X position off the previous element width included
            lines.add(
                GuiUtils.Element(
                    lines[2].x + lines[2].width + 7f,
                    5f,
                    "§6${if (MiningConfig.forgeFlipperMaxPrice == 0) "Any" else maxPriceLong.abbreviateNumber()}",
                    listOf("§cHides Flips that require a cost above this value"),
                    {
                        // Double the limit each click
                        MiningConfig.forgeFlipperMaxPrice *= 2

                        // Reset back to 10m if no limit
                        if (MiningConfig.forgeFlipperMaxPrice == 0) MiningConfig.forgeFlipperMaxPrice = 10
                        // Set limit to no limit if over 1b
                        if (MiningConfig.forgeFlipperMaxPrice > 1000) MiningConfig.forgeFlipperMaxPrice = 0

                        SkyblockTweaks.config.saveConfig()
                        // Reset scroll offset
                        scrollOffset = 0
                    },
                    drawBackground = true,
                )
            )

            if (forgeFlips.isEmpty()) {
                statusMessage = "§cFailed to get Forge Flips.."
                lines.add(
                    GuiUtils.Element(
                        5f, 20f, statusMessage, null, null
                    )
                )
            }

            if (forgeFlips.isNotEmpty()) {
                var sorted = forgeFlips.entries.sortedByDescending {
                    if (sortingType == 0) it.value.coinsPerHour else it.value.profit.toDouble()
                }
                sorted = sorted.filter { it.value.hotmRequired <= hotmMax }
                if (maxPriceLong != 0L) {
                    sorted = sorted.filter { it.value.totalCost.toLong() <= maxPriceLong }
                }
                val guiName = (event as GuiContainerBackgroundDrawnEvent).gui!!.chestName()
                if (forgeMenuNames.contains(guiName)) {
                    sorted = sorted.filter { it.key in itemsInMenu }
                }
                // Scrolling Logic
                val scrollAmount = Mouse.getDWheel()
                if (scrollAmount != 0) {
                    scrollOffset += -floor(scrollAmount / 120.0).toInt()

                    scrollOffset = scrollOffset.coerceIn(0, max(sorted.size - maxFlipsShown,0))
                }

                if (sorted.isNotEmpty()) {
                    val endIndex = (scrollOffset + maxFlipsShown).coerceAtMost(sorted.size) // Prevent out-of-bounds error
                    val startIndex = scrollOffset
                    sorted = sorted.subList(startIndex, endIndex)
                }

                val holdingShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                var first = true
                sorted.forEach {
                    val outputItem = ItemApi.getItemInfo(it.value.output)
                    val outputName = outputItem?.get("displayname")?.asString

                    val hoverText = mutableListOf(
                        "$outputName",
                        "§cRequires HOTM ${it.value.hotmRequired}",
                        "§bCoins/hr: §2${it.value.coinsPerHour.abbreviateNumber()}  §bProfit: §2+${it.value.profit.abbreviateNumber()}",
                        "§bCost: §3${(it.value.totalCost).abbreviateNumber()} §bSell: §3${it.value.outputValue.abbreviateNumber()}" + (if (!holdingShift) " §8[HOLD SHIFT]" else ""),
                    )

                    if (holdingShift) {
                        it.value.requiredItems.forEach { (itemID, count) ->
                            val item = ItemApi.getItemInfo(itemID)
                            val itemName = item?.get("displayname")?.asString
                            val cost = (ItemUtils.getItemBasePrice(itemID, false) * count)

                            hoverText.add(" §8• $itemName §7x$count §f➡ §e${cost.abbreviateNumber()}")
                        }
                    }

                    val fancyDuration = (it.value.secondsDuration * 1000L).toFormattedDuration(false)

                    hoverText.addAll(
                        listOf(
                            "§bDuration: §e${fancyDuration}",
                        )
                    )

                    val yOffset = if (first) 4f else 2f
                    val y = GuiUtils.getLowestY(lines) + yOffset
                    val itemStack = ItemApi.createItemStack(it.value.output) ?: ItemStack(Items.apple)
                    lines.addAll(
                        listOf(
                            GuiUtils.Element(
                                7f + 9f,
                                y,
                                "$outputName §2+" + if (sortingType == 0) "${it.value.coinsPerHour.abbreviateNumber()}/hr" else "${it.value.profit.abbreviateNumber()}§7 ┃ §e${fancyDuration}",
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
            if (event !is GuiContainerBackgroundDrawnEvent || event.gui == null || !MiningConfig.forgeFlipperOverlay || LocationManager.currentIsland != "Dwarven Mines") return false

            if (forgeMenuNames.any {
                    event.gui!!.chestName().contains(it)
                } || event.gui!!.chestName() == "The Forge" || event.gui!!.chestName().contains("Select Process")) {
                if (forgeFlips.isEmpty()) {
                    findForgeFlips()
                }
                return true
            }
            return false
        }
    }
}