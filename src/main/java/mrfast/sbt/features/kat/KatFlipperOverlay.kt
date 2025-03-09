package mrfast.sbt.features.kat

import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.guis.components.OutlinedRoundedRectangle
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.getStringWidth
import mrfast.sbt.utils.Utils.toFormattedDuration
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.eventhandler.Event
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.awt.Color
import kotlin.math.floor

@SkyblockTweaks.EventComponent
object KatFlipperOverlay {
    private val katFlips = mutableMapOf<String, KatFlip>()
    const val maxFlipsShown = 20
    private var statusMessage = ""

    class KatFlip {
        var input = ""
        var output = ""
        var inputValue = 0.0
        var outputValue = 0.0

        var secondsDuration = 0
        var coinCost = 0
        var requiredItems = mutableMapOf<String, Int>()
        var itemCost = 0.0
        var totalCost = 0.0
        var profit = 0
        var coinsPerHour = 0.0
    }

    fun findKatFlips() {
        statusMessage = "Getting Skyblock Pet Recipes.."
        ItemApi.getSkyblockItems().entrySet().forEach {
            statusMessage = "Checking ${it.key}.."
            if (!it.value.asJsonObject.has("recipes")) return@forEach

            val recipes = it.value.asJsonObject.getAsJsonArray("recipes")
            if (recipes.size() == 0) return@forEach

            val recipe = recipes[0].asJsonObject
            if (recipe.get("type").asString != "katgrade") return@forEach

            val katFlip = KatFlip()
            val input = recipe.get("input").asString
            val output = recipe.get("output").asString

            katFlip.input = ItemUtils.convertNeuPetID(input)
            katFlip.output = ItemUtils.convertNeuPetID(output)
            katFlip.secondsDuration = recipe.get("time").asInt
            katFlip.coinCost = recipe.get("coins").asInt
            katFlip.totalCost += katFlip.coinCost

            var requiresSoulboundItem = false
            recipe.get("items").asJsonArray.forEach { item ->
                val item = item.asString
                val itemID = item.split(":")[0]
                val count = Integer.parseInt(item.split(":")[1])

                if (ItemUtils.getItemBasePrice(itemID) != -1.0) {
                    katFlip.requiredItems[itemID] = count
                    katFlip.itemCost += (ItemUtils.getItemBasePrice(itemID, false) * count).toInt()
                } else {
                    requiresSoulboundItem = true
                }
            }
            // Dont show the pet if its upgrade requires a soulbound item
            if (requiresSoulboundItem) return@forEach

            // Add the cost of the items to the total cost
            katFlip.totalCost += katFlip.itemCost

            val outputValue = ItemUtils.getItemBasePrice(katFlip.output)
            val inputValue = ItemUtils.getItemBasePrice(katFlip.input)

            // Dont show if no price data was found for the input pet or the output pet
            if (outputValue == -1.0 || inputValue == -1.0) return@forEach

            val inputItem = ItemApi.getItemInfo(katFlip.input)
            val outputItem = ItemApi.getItemInfo(katFlip.output)

            // Dont show if there is no active bin data for the input or output pet
            if (inputItem != null && inputItem.has("activeBin")) {
                if (inputItem.get("activeBin").asInt <= 1) {
                    return@forEach
                }
            }
            if (outputItem != null && outputItem.has("activeBin")) {
                if (outputItem.get("activeBin").asInt <= 1) {
                    return@forEach
                }
            }

            katFlip.outputValue = outputValue
            katFlip.inputValue = inputValue

            katFlip.totalCost += inputValue.toInt().toDouble()

            if (katFlip.totalCost < outputValue) {
                katFlip.profit = (outputValue - katFlip.totalCost).toInt()
            }
            // Hide if profit is less than 100k
            if (katFlip.profit < 100_000) return@forEach

            val minutes = katFlip.secondsDuration.toDouble() / 60.0
            val hours = minutes / 60.0
            katFlip.coinsPerHour = katFlip.profit / hours

            katFlips[katFlip.input] = katFlip
            statusMessage = "Done"
        }
    }

    init {
        KatOverlay()
    }

    class KatOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        // 0 = most coins/hr
        // 1 = most profit
        private var sortingType = 0
        private var scrollOffset = 0

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val lines = mutableListOf(
                GuiUtils.Element(
                    5f, 5f, "§e§lKat Flips", null, null
                ),
                GuiUtils.Element(
                    65f, 5f, "§cSorting By " + if (sortingType == 0) "Coins/hr" else "Profit",
                    listOf("§cClick to change sorting type"),
                    {
                        sortingType = if (sortingType == 0) 1 else 0
                        scrollOffset = 0
                    },
                    drawBackground = true,
                )
            )
            if(katFlips.isEmpty()) {
                statusMessage = "§cFailed to get Kat Flips.."
                lines.add(
                    GuiUtils.Element(
                        5f, 20f, statusMessage, null, null
                    )
                )
            }

            if(katFlips.isNotEmpty()) {
                var sorted = katFlips.entries.sortedByDescending {
                    if (sortingType == 0) it.value.coinsPerHour else it.value.profit.toDouble()
                }
                // Scrolling Logic
                val scrollAmount = Mouse.getDWheel()
                if (scrollAmount != 0) {
                    scrollOffset += -floor(scrollAmount / 120.0).toInt()
                    scrollOffset = scrollOffset.coerceIn(0, katFlips.size - (maxFlipsShown + 1))
                }
                if (sorted.size > maxFlipsShown + scrollOffset) sorted =
                    sorted.subList(scrollOffset, scrollOffset + maxFlipsShown)

                val holdingShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)
                var first = true
                sorted.forEach {
                    val inputItem = ItemApi.getItemInfo(it.value.input)
                    val inputName = inputItem?.get("displayname")?.asString?.replace("[Lvl {LVL}] ", "")

                    val outputItem = ItemApi.getItemInfo(it.value.output)
                    val outputName = outputItem?.get("displayname")?.asString?.replace("[Lvl {LVL}] ", "")

                    val hoverText = mutableListOf(
                        "$inputName §8-> $outputName",
                        "§bCoins/hr: §2${it.value.coinsPerHour.abbreviateNumber()}  §bProfit: §2+${it.value.profit.abbreviateNumber()}",
                        "§bCost: §3${(it.value.totalCost).abbreviateNumber()} §bSell: §3${it.value.outputValue.abbreviateNumber()}" + (if (!holdingShift) " §8[HOLD SHIFT]" else ""),
                    )

                    if (holdingShift) {
                        hoverText.add(" §8• $inputName §f➡ §e${it.value.inputValue.abbreviateNumber()}")

                        it.value.requiredItems.forEach { (itemID, count) ->
                            val item = ItemApi.getItemInfo(itemID)
                            val itemName = item?.get("displayname")?.asString
                            val cost = (ItemUtils.getItemBasePrice(itemID, false) * count)

                            hoverText.add(" §8• $itemName §7x$count §f➡ §e${cost.abbreviateNumber()}")
                        }
                        hoverText.add(" §8• §6Coin Cost §f➡ §e${it.value.coinCost.abbreviateNumber()}")
                    }

                    val fancyDuration = (it.value.secondsDuration * 1000L).toFormattedDuration(false)

                    hoverText.addAll(
                        listOf(
                            "§bDuration: §e${fancyDuration}",
                        )
                    )

                    val yOffset = if (first) 4f else 2f
                    val y = GuiUtils.getLowestY(lines) + yOffset
                    lines.addAll(
                        listOf(
                            GuiUtils.Element(
                                7f + 9f,
                                y,
                                "$inputName §8-> $outputName §2+" + if (sortingType == 0) "${it.value.coinsPerHour.abbreviateNumber()}/hr" else "${it.value.profit.abbreviateNumber()}§7 ┃ §e${fancyDuration}",
                                hoverText,
                                null
                            ),
                            GuiUtils.ItemStackElement(
                                ItemApi.createItemStack(it.value.input)!!,
                                1f,
                                y - 3f,
                                16,
                                16
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
            if (event !is GuiContainerBackgroundDrawnEvent || event.gui == null || !GeneralConfig.katFlipperOverlay) return false

            if (event.gui!!.chestName() == "Pet Sitter") {
                if (katFlips.isEmpty()) {
                    findKatFlips()
                }
                return true
            }
            return false
        }
    }
}