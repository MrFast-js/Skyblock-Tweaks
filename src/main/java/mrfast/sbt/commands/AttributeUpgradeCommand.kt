package mrfast.sbt.commands

import com.google.common.collect.Lists
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getAttributes
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils.abbreviateNumber
import mrfast.sbt.utils.Utils.toTitleCase
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import kotlin.math.pow

@SkyblockTweaks.CommandComponent
class AttributeUpgradeCommand : CommandBase() {
    override fun getCommandName(): String {
        return "attributeupgrade"
    }

    override fun getCommandAliases(): List<String> {
        return Lists.newArrayList("au")
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/au"
    }

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isEmpty()) {
            handleNoArguments()
            return
        }
        if (args.size == 1) {
            handleOneArguments(args)
            return
        }
        if (args.size == 2) {
            var attributeToUpgrade = args[0]

            // Player did attribute abbreviation
            if (ItemUtils.attributeShortNames.containsValue(attributeToUpgrade.uppercase())) {
                attributeToUpgrade =
                    ItemUtils.attributeShortNames.entries.firstOrNull { it.value == attributeToUpgrade }?.key!!
            }

            // Ignore if attribute is not found
            if (!ItemUtils.attributeShortNames.containsKey(attributeToUpgrade)) {
                ChatUtils.sendClientMessage("§cUnable to find attribute: §e$attributeToUpgrade", shortPrefix = true)
                return
            }

            val attributeShortName = ItemUtils.attributeShortNames[attributeToUpgrade]!!

            // Check if the attribute is valid
            val targetTier = args[1].toIntOrNull()

            if (targetTier == null) {
                ChatUtils.sendClientMessage("§cInvalid tier! Must be a number.", shortPrefix = true)
                return
            }

            val heldItem = ItemUtils.getHeldItem()
            if (heldItem == null) {
                ChatUtils.sendClientMessage("§cYou must be holding an item to use this command!", shortPrefix = true)
                return
            }

            val heldItemAttributes = ItemUtils.getHeldItem()?.getAttributes()
            val attributeNameFormatted = (attributeToUpgrade).replace("_", " ").toTitleCase()

            if (heldItemAttributes.isNullOrEmpty() || !heldItemAttributes.containsKey(attributeToUpgrade)) {
                ChatUtils.sendClientMessage(
                    "§cYou must be holding an item with the attribute §e$attributeNameFormatted§c!",
                    shortPrefix = true
                )
                return
            }

            val currentTier = heldItemAttributes[attributeToUpgrade]!!

            if (currentTier >= targetTier) {
                ChatUtils.sendClientMessage(
                    "§cYou must be holding an item with a lower tier of the attribute §e$attributeNameFormatted§c!",
                    shortPrefix = true
                )
                return
            }

            ChatUtils.sendClientMessage(
                "§eUpgrading §c§l$attributeNameFormatted $currentTier §e➡ §a§l$attributeNameFormatted $targetTier...",
                shortPrefix = true
            )
            val heldAttributeType = getAttributeType(heldItem.getSkyblockId()!!)
            val itemMap = mutableMapOf<Int, MutableList<JsonObject>>()

            for (i in 10 downTo 1) {
                ItemApi.liveAuctionData.entrySet().forEach {
                    val itemID = it.key
                    val obj = it.value.asJsonObject

                    if (getAttributeType(itemID) != heldAttributeType && itemID != "ATTRIBUTE_SHARD") {
                        return@forEach
                    }

                    // Pricing data for the item
                    obj.entrySet().forEach { entry ->
                        val attribute = entry.key // AT:LL4,MF5
                        val attributeValue = entry.value.asJsonObject

                        if (attribute.contains("$attributeShortName$i")) {
                            attributeValue.addProperty("itemID", itemID)

                            if (itemMap[i] == null) itemMap[i] = mutableListOf()

                            itemMap[i]!!.add(attributeValue)
                        }
                    }
                }
            }

            val vectorMap = mutableMapOf<Int, MutableList<VectorObject>>()

            itemMap.forEach {
                val tier = it.key
                val items = it.value

                items.forEach { json ->
                    if (vectorMap[tier] == null) vectorMap[tier] = mutableListOf()

                    val vector = VectorObject(
                        json.get("auc_id").asString,
                        json.get("price").asLong,
                        tier
                    )
                    vector.itemID = json.get("itemID").asString
                    vectorMap[tier]!!.add(vector)
                }
            }

            val sortedList = vectorMap.values.flatten().sortedByDescending { it.value }
            val lowBound =
                (2.0).pow(targetTier - 1) - (2.0).pow(currentTier - 1) // Low bound is most efficient weighted path

            CoroutineScope(Dispatchers.IO).launch {
                val perfectPath1 = findMostEfficientPathDP(sortedList, lowBound)
                val perfectPath2 = findMostEfficientPathDP(sortedList, (2.0).pow(targetTier - 1))

                val perfectPath = when {
                    perfectPath1 != null && perfectPath2 != null ->
                        if (perfectPath1.sumOf { it.price } < perfectPath2.sumOf { it.price }) perfectPath1 else perfectPath2

                    perfectPath1 != null -> perfectPath1
                    perfectPath2 != null -> perfectPath2
                    else -> null
                }

                if (perfectPath == null) {
                    withContext(Dispatchers.Default) {
                        ChatUtils.sendClientMessage(
                            "§cUnable to find a path for the given tier! Not enough auctions!",
                            shortPrefix = true
                        )
                    }
                    return@launch
                }

                withContext(Dispatchers.Default) {
                    perfectPath.forEach {
                        val price = it.price
                        val tier = it.tier
                        val auctionID = it.aucID

                        if (auctionID != null) {
                            val itemDisplayName = ItemApi.getItemInfo(it.itemID!!)?.get("displayname")?.asString ?: "§fAttribute Shard"
                            val message = ChatComponentText("$itemDisplayName §d| §b$attributeNameFormatted $tier §d| §a${price.abbreviateNumber()}")
                            message.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction $auctionID")
                            message.chatStyle.setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText("§eClick to view auction")
                                )
                            )
                            ChatUtils.sendClientMessage(message, shortPrefix = true)
                        }
                    }

                    ChatUtils.sendClientMessage(
                        "§a§lTotal Price: ${perfectPath.sumOf { it.price }.abbreviateNumber()}",
                        shortPrefix = true
                    )
                }
            }
        }
    }

    private fun handleOneArguments(args: Array<out String>) {
        val heldItemAttributes = ItemUtils.getHeldItem()?.getAttributes()
        var attributeToUpgrade = args[0]

        // Player did attribute abbreviation
        if (ItemUtils.attributeShortNames.containsValue(attributeToUpgrade.uppercase())) {
            attributeToUpgrade =
                ItemUtils.attributeShortNames.entries.firstOrNull { it.value == attributeToUpgrade }?.key!!
        }

        // Ignore if attribute is not found
        if (!ItemUtils.attributeShortNames.containsKey(attributeToUpgrade)) {
            ChatUtils.sendClientMessage("§cUnable to find attribute: §e$attributeToUpgrade", shortPrefix = true)
            return
        }

        val message = ChatComponentText("")
        val attributeNameFormatted = (attributeToUpgrade).replace("_", " ").toTitleCase()
        val attributeShortName = ItemUtils.attributeShortNames[attributeToUpgrade]!!

        if (heldItemAttributes == null || !heldItemAttributes.containsKey(attributeToUpgrade)) {
            ChatUtils.sendClientMessage(
                "§cYou must be holding an item with the attribute §e$attributeNameFormatted§c!",
                shortPrefix = true
            )
            return
        }

        ChatUtils.sendClientMessage("§eWhat level would you like to upgrade it to?", shortPrefix = true)

        for (tier in (heldItemAttributes[attributeToUpgrade]!! + 1)..10) {
            val part = ChatComponentText("§6§l[$tier]")
            part.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/au $attributeShortName $tier")

            part.chatStyle.setChatHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ChatComponentText("§eClick to select")
                )
            )

            message.appendSibling(part)
            message.appendText(" ")
        }

        ChatUtils.sendClientMessage(message, shortPrefix = true)
    }

    private fun handleNoArguments() {
        val heldItemAttributes = ItemUtils.getHeldItem()?.getAttributes()
        val message = ChatComponentText("")
        if (heldItemAttributes.isNullOrEmpty()) {
            ChatUtils.sendClientMessage(
                "§cYou must be holding an item with attributes to use this command!",
                shortPrefix = true
            )
            return
        }

        ChatUtils.sendClientMessage("§eWhich attribute would you like to upgrade?", shortPrefix = true)

        for (attribute in heldItemAttributes.keys) {
            val attributeNameFormatted = (attribute).replace("_", " ").toTitleCase()
            val attributeShortName = ItemUtils.attributeShortNames[attribute]!!

            val part = ChatComponentText("§a§l[$attributeNameFormatted ${heldItemAttributes[attribute]}]")
            part.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/au $attributeShortName")

            part.chatStyle.setChatHoverEvent(
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ChatComponentText("§eClick to select")
                )
            )

            message.appendSibling(part)
            message.appendText(" ")
        }

        ChatUtils.sendClientMessage(message, shortPrefix = true)
    }


    // Top Down Solution, https://www.geeksforgeeks.org/0-1-knapsack-problem-dp-10/
    private fun findMostEfficientPathDP(
        numbers: List<VectorObject>,
        targetWeight: Double,
        precision: Int = 100
    ): List<VectorObject>? {
        val scale = precision.toInt()
        val intTarget = (targetWeight * scale).toInt()

        val dp = Array(intTarget + 1) { Long.MAX_VALUE to emptyList<VectorObject>() }
        dp[0] = 0L to emptyList()

        for (obj in numbers) {
            val w = (obj.weight * scale).toInt()
            val p = obj.price

            for (i in intTarget downTo w) {
                val (prevCost, prevPath) = dp[i - w]
                if (prevCost != Long.MAX_VALUE && prevCost + p < dp[i].first) {
                    dp[i] = prevCost + p to prevPath + obj
                }
            }
        }

        // Scan for the best valid result (weight ≤ target, minimal price)
        for (i in intTarget downTo 0) {
            val (cost, path) = dp[i]
            if (cost != Long.MAX_VALUE) {
                return path
            }
        }

        // If no valid path found
        return null
    }

    class VectorObject(
        var aucID: String?,
        var price: Long,
        var tier: Int
    ) {
        var itemID: String? = null
        var weight = (2.0).pow(tier - 1)
        val value = (weight / price) * 10_000_000
        var a: VectorObject? = null
        var b: VectorObject? = null
    }

    private fun getAttributeType(itemID: String): String? {
        when (itemID) {
            "MAGMA_ROD" -> return "MagmaRod"
            "TAURUS_HELMET" -> return "FishingHelmet"
            "FLAMING_CHESTPLATE" -> return "FishingChestplate"
            "MOOGMA_LEGGINGS" -> return "FishingLeggings"
            "SLUG_BOOTS" -> return "FishingBoots"
            "MOLTEN_BELT" -> return "MoltenBelt"
            "MOLTEN_BRACELET" -> return "MoltenBracelet"
            "MOLTEN_NECKLACE" -> return "MoltenNecklace"
            "MOLTEN_CLOAK" -> return "MoltenCloak"
            "LAVA_SHELL_NECKLACE" -> return "LavaShellNecklace"
            "SCOURGE_CLOAK" -> return "ScourgeCloak"
            "ATTRIBUTE_SHARD" -> return "Shard"
            "MAGMA_NECKLACE" -> return "MagmaNecklace"
            "VANQUISHED_MAGMA_NECKLACE" -> return "VanquishedMagmaNecklace"
            "BLAZE_BELT" -> return "BlazeBelt"
            "VANQUISHED_BLAZE_BELT" -> return "VanquishedBlazeBelt"
            "GLOWSTONE_GAUNTLET" -> return "GlowstoneGauntlet"
            "VANQUISHED_GLOWSTONE_GAUNTLET" -> return "VanquishedGlowstoneGauntlet"
            "GHAST_CLOAK" -> return "GhastCloak"
            "VANQUISHED_GHAST_CLOAK" -> return "VanquishedGhastCloak"
            "IMPLOSION_BELT" -> return "ImplosionBelt"
            "GAUNTLET_OF_CONTAGION" -> return "GauntletOfContagion"
        }
        if (!itemID.contains("CRIMSON") && !itemID.contains("AURORA") && !itemID.contains("TERROR") && !itemID.contains(
                "FERVOR"
            ) && !itemID.contains("HOLLOW")
        ) return null
        if (itemID.contains("HELMET")) return "Helmet"
        if (itemID.contains("CHESTPLATE")) return "Chestplate"
        if (itemID.contains("LEGGINGS")) return "Leggings"
        if (itemID.contains("BOOTS")) return "Boots"
        return null
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}