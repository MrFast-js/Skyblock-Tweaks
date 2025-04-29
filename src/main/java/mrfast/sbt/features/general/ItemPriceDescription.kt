package mrfast.sbt.features.general

import com.google.gson.JsonObject
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.managers.PaidPriceManager
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getDataString
import mrfast.sbt.utils.ItemUtils.getItemUUID
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

@SkyblockTweaks.EventComponent
object ItemPriceDescription {

    @SubscribeEvent
    fun onToolTip(event: ItemTooltipEvent) {
        if (!MiscellaneousConfig.showItemPricingData) return

        val stack = event.itemStack
        val menuName = (Utils.mc.currentScreen as? GuiChest)?.chestName() ?: ""
        val pricingData = ItemApi.getItemInfo(stack) ?: return

        if (MiscellaneousConfig.showActiveAuctionStat) {
            pricingData.takeIf { it.has("activeBin") || it.has("activeAuc") }?.let {
                val activeBinNum = if (it.has("activeBin")) it.get("activeBin").asInt else -1
                val activeAucNum = if (it.has("activeAuc")) it.get("activeAuc").asInt else -1
                val activeBin = if (activeBinNum != -1) activeBinNum.formatNumber() else "§4Unknown" // Red for missing
                val activeAuc = if (activeAucNum != -1) activeAucNum.formatNumber() else "§4Unknown"
                event.toolTip.add("§3Active: §a$activeBin BIN §7| §b$activeAuc AUC")
            }
        }

        if (MiscellaneousConfig.showSalesStat) {
            pricingData.takeIf { it.has("binSold") || it.has("aucSold") }?.let {
                val soldBinNum = if (it.has("binSold")) it.get("binSold").asInt else -1
                val soldAucNum = if (it.has("aucSold")) it.get("aucSold").asInt else -1
                val soldBin = if (soldBinNum != -1) soldBinNum.formatNumber() else "§4Unknown"
                val soldAuc = if (soldAucNum != -1) soldAucNum.formatNumber() else "§4Unknown"
                event.toolTip.add("§3Sales/day: §a$soldBin BIN §7| §b$soldAuc AUC")
            }
        }

        if (MiscellaneousConfig.showBinPricingStat) {
            pricingData.takeIf { it.has("avgLowestBin") && it.has("lowestBin") }?.let {
                val avgLowestBin = it.get("avgLowestBin").asLong.formatNumber()
                val lowestBin = it.get("lowestBin").asLong.formatNumber()
                event.toolTip.add("§3BIN: §7AVG §e$avgLowestBin §8| §7LOW §e$lowestBin")
            }
        }

        if (MiscellaneousConfig.showAuctionPricingStat) {
            pricingData.takeIf { it.has("avgAucPrice") && it.has("aucPrice") }?.let {
                val avgAucPrice = it.get("avgAucPrice").asLong.formatNumber()
                val aucPrice = it.get("aucPrice").asLong.formatNumber()
                event.toolTip.add("§3AUC: §7AVG §e$avgAucPrice §8| §7SOON §e$aucPrice")
            }
        }

        if (MiscellaneousConfig.showPricePaidStat) {
            if (stack.getItemUUID() != null) {
                val pricePaid = PaidPriceManager.getPricePaid(stack.getItemUUID()!!)
                if (pricePaid != null) {
                    val pricePaidFormatted = pricePaid.formatNumber()
                    event.toolTip.add("§3Price Paid: §d§l$pricePaidFormatted")
                }
            }
        }

        if (MiscellaneousConfig.showBazaarStat) {
            pricingData.takeIf { it.has("bazaarBuy") || it.has("bazaarSell") }?.let {
                val multiplier = if (!menuName.contains("Experimentation")) stack.stackSize else 1

                val bazaarBuy = it.takeIf { it.has("bazaarBuy") }
                    ?.get("bazaarBuy")
                    ?.asLong
                    ?.times(multiplier)
                    ?.formatNumber() ?: "§4Unknown"

                val bazaarSell = it.takeIf { it.has("bazaarSell") }
                    ?.get("bazaarSell")
                    ?.asLong
                    ?.times(multiplier)
                    ?.formatNumber() ?: "§4Unknown"

                event.toolTip.add("§3Bazaar: §a$bazaarBuy Buy §7| §b$bazaarSell Sell")
            }
        }

        val dataString = stack.getDataString()
        val id = stack.getSkyblockId()

        if (CustomizationConfig.developerMode) event.toolTip.add("§8SBTID: ${id ?: "§4Unknown"}")

        if (MiscellaneousConfig.showPriceMatchingStat) {
            if (dataString != "" && ItemApi.liveAuctionData.has(id)) {
                if (CustomizationConfig.developerMode) event.toolTip.add("§3Data String: §7$dataString")
                val itemArray = ItemApi.liveAuctionData.get(id).asJsonObject

                val best = findBestMatch(itemArray, dataString, id!!) ?: return
                val priceMatch = best.first
                val percentMatch = best.second

                event.toolTip.add("§a§lMATCHED PRICE: §7${priceMatch.formatNumber()} §8${percentMatch.abbreviateNumber()}%")
            }
        }
    }

    private val weights = mapOf(
        "RecombMatch" to 5.0,
        "DefaultEnchantMatch" to 4.0,
        "AttributeMatch" to 15.0,
        "HotPotatoMatchFull" to 3.0
    )

    private var debugBestMatch = "AT:BL2,BR1"
    fun findBestMatch(itemArray: JsonObject, targetAttributes: String, itemId: String): Pair<Long, Double>? {
        var bestItem: String? = null
        var bestScore = Double.MIN_VALUE
        var bestPercent = 0.0

        val targetAttributesList = splitString(targetAttributes) // Split the target attributes to match

        val maxScore = getMatchScore(targetAttributesList, targetAttributesList)

        // Evaluate each data string in the itemArray
        for (itemEntry in itemArray.entrySet()) {
            val itemAttributes = splitString(itemEntry.key) // Split attributes of the current item
            val matchScore = getMatchScore(itemAttributes, targetAttributesList)

            // Calculate match percentage based on the available fields
            val matchPercentage = if (maxScore > 0) {
                (matchScore / maxScore) * 100
            } else {
                0.0 // In case no matching fields exist in the target
            }

            // Update the best match if this item's score is higher
            if (matchScore >= bestScore) {
                val itemData = ItemApi.liveAuctionData.get(itemId).asJsonObject

                val bestData = itemData?.get(bestItem)?.asJsonObject
                val bestPrice = bestData?.get("price")?.asLong ?: 0
                val thisData = itemData?.get(itemEntry.key)?.asJsonObject
                val thisPrice = thisData?.get("price")?.asLong ?: 0

                // If the match score is equal, compare the prices, otherwise, update the best match
                if (bestItem == null || (matchScore == bestScore && thisPrice < bestPrice) || matchScore > bestScore) {
                    bestScore = matchScore
                    bestPercent = matchPercentage
                    bestItem = itemEntry.key
                }
            }
        }


        if (bestPercent == 0.0 || bestItem == null) return null
        val itemData = itemArray.get(bestItem).asJsonObject
        val price = itemData.get("price").asLong

        return (price to bestPercent)
    }

    private fun getMatchScore(itemStats: List<String>, targetStats: List<String>): Double {
        var totalMatchScore = 0.0
        var out = ""
        val dataString = itemStats.joinToString("+")

        val hasAttributes = itemStats.any { it.startsWith("AT:") }
        val hasEnchants = itemStats.any { it.startsWith("E:") }
        val hasRecomb = itemStats.any { it == "R" }
        val hasStars = itemStats.any { it.endsWith("S") }
        val hasHotPotato = itemStats.any { it.endsWith("HP") }

        for (stat in itemStats) {
            when {
                // Attributes: Format AT:<name><level>, e.g., AT:BL2,BR1
                stat.startsWith("AT:") && hasAttributes -> {
                    // At this point, we are working in the attribute section of the current item
                    var totalWeight = 0.0

                    val targetAttributes = targetStats.find { it.startsWith("AT:") }?.substring(3)?.split(",") ?: continue // Look for the target attributes
                    val itemAttributes = stat.substring(3).split(",")

                    // Exact match of type of attributes
                    val weightPerAttribute = weights["AttributeMatch"]!! / itemAttributes.size

                    // Going through each attribute of the item
                    for (attributeString in itemAttributes) {
                        val attributeName = attributeString.substring(0, 2) // Extract name, eg. AT:BL
                        val itemLevel = attributeString.last().digitToIntOrNull() ?: 0 // Extract level of the attribute

                        // Find the same attribute on the target
                        val targetAttribute = targetAttributes.find { it.startsWith(attributeName) }
                        val targetLevel = targetAttribute?.last()?.digitToIntOrNull() ?: 0 // Extract target level

                        if (targetAttribute == null) continue

                        if (itemLevel == targetLevel) {
                            totalWeight += weightPerAttribute
                        } else {
                            // if has attribute but not matching level
                            var weight = weightPerAttribute

                            // sameTypeLevel = 5, itemLevel = 7
                            if (itemLevel > targetLevel) {
                                // Having a higher level attribute than the target
                                for (lvl in itemLevel downTo targetLevel) {
                                    weight *= 0.95
                                }
                            } else {
                                // Having a higher level attribute than the target
                                for (lvl in itemLevel until targetLevel) {
                                    weight *= 0.75
                                }
                            }

                            totalWeight += weight
                        }
                    }
                    totalMatchScore += totalWeight

                }

                // Enchantments: Format E:<name><level>, e.g., E:soul_eater5
                stat.startsWith("E:") && hasEnchants-> {
                    val enchantName = stat.substring(2, stat.length - 1) // Extract name
                    val itemLevel = stat.last().digitToIntOrNull() ?: 0 // Extract level as Int

                    // Find a matching enchantment in the target
                    val targetEnchant = targetStats.find { it.startsWith("E:$enchantName") }
                    val targetLevel = targetEnchant?.last()?.digitToIntOrNull() ?: 0 // Extract target enchantment level

                    if (targetEnchant != null) {
                        if (itemLevel == targetLevel) {
                            val weight = getEnchantWeight(enchantName, itemLevel, targetLevel)
                            totalMatchScore += weight
                        } else {
                            // If has enchant but not matching level
                            val weight = getEnchantWeight(enchantName, itemLevel, targetLevel)
                            totalMatchScore += weight
                        }
                    } else {
                        // If no enchantment match in the target, apply a penalty
                        val penalty = -1.0 // Apply a penalty for missing enchantment in the target
                        totalMatchScore += penalty
                    }
                }

                // Recomb: Format R
                stat == "R" && hasRecomb -> {
                    val targetRecomb = targetStats.find { it == "R" }
                    if (targetRecomb != null) {
                        val weight = weights["RecombMatch"]!! ?: 0.0
                        totalMatchScore += weight
                        if (debugBestMatch == dataString) out += "Recomb ($weight)"
                    } else {
                        // If no match in target for recomb, apply penalty
                        val weight = -weights["RecombMatch"]!! ?: 0.0
                        totalMatchScore += weight
                        if (debugBestMatch == dataString) out += "Missing Recomb ($weight)"
                    }
                }

                // Stars: Format <number>S, e.g., 5S
                stat.endsWith("S") && hasStars -> {
                    val itemStars = stat.removeSuffix("S").toIntOrNull() ?: 0
                    val targetStars =
                        targetStats.filter { it.endsWith("S") }.sumOf { it.removeSuffix("S").toIntOrNull() ?: 0 }

                    val starWeights = arrayOf(0.0, 1.0, 2.1, 3.3, 4.6, 6.0, 8.0, 11.0, 14.5, 18.5, 23.5)
                    if (itemStars == targetStars && itemStars in starWeights.indices) {
                        val weight = starWeights[itemStars]
                        totalMatchScore += weight
                        if (debugBestMatch == dataString) out += "Stars ($itemStars | $targetStars | $weight)"
                    } else {
                        // Penalize if itemStars exceed targetStars
                        val weight = -((itemStars - targetStars) * 0.5)
                        totalMatchScore += weight
                        if (debugBestMatch == dataString) out += "Stars ($itemStars | $targetStars | $weight)"
                    }
                }

                // Hot Potato Books: Format <number>HP, e.g., 10HP
                stat.endsWith("HP") && hasHotPotato -> {
                    val itemHP = stat.removeSuffix("HP").toIntOrNull() ?: 0
                    val targetHP =
                        targetStats.filter { it.endsWith("HP") }.sumOf { it.removeSuffix("HP").toIntOrNull() ?: 0 }

                    // If target doesn't have HP, treat it as 0 and penalize if item has HP
                    totalMatchScore += if (targetHP == 0) {
                        val weight = (-itemHP.toDouble() / 15.0) * 3.0 // Penalize for having HP if target is missing it
                        if (debugBestMatch == dataString) out += "NOHP ($itemHP | $targetHP | $weight)"
                        weight
                    } else {
                        // If target has HP, reward or penalize accordingly
                        val weight = ((15 - abs(itemHP - targetHP)) / 15.0) * 3.0
                        if (debugBestMatch == dataString) out += "HP ($itemHP | $targetHP | $weight)"
                        weight
                    }
                }
            }
        }
        return totalMatchScore
    }

    private fun getEnchantWeight(enchant: String, itemEnchantLevel: Int, targetLevel: Int): Double {
        val baseWeight = when (enchant) {
            "legion" -> 7.0
            "last_stand" -> 6.0
            "wisdom" -> 5.0
            "refrigerate" -> 5.0
            else -> weights["DefaultEnchantMatch"]!!
        }

        // If the item enchantment level is lower than the query level, apply an exponential penalty
        return if (itemEnchantLevel < targetLevel) {
            // Exponential penalty: 1 / (levelDifference ^ 2)
            val levelDifference = abs(targetLevel - itemEnchantLevel)

            (baseWeight * (1.0 / (levelDifference.toDouble() * levelDifference.toDouble())))
        } else {
            // No penalty if the item level matches or exceeds the query level
            baseWeight
        }
    }

    // Example inputs
    // E:wise5+R+Re:aote_stone+10HP
    // E:soul_eater5+R+Re:precise+10HP+5S
    private fun splitString(data: String): List<String> {
        return data.split("+")
    }

}