package mrfast.sbt.commands

import com.google.common.collect.Lists
import com.google.gson.JsonObject
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
        if (args.size == 2) {
            var attributeToUpgrade = args[0]

            // Player did attribute abbreviation
            if (ItemUtils.attributeShortNames.containsValue(attributeToUpgrade)) {
                attributeToUpgrade = ItemUtils.attributeShortNames.entries.firstOrNull { it.value == attributeToUpgrade }?.key!!
            }

            // Ignore if attribute is not found
            if (!ItemUtils.attributeShortNames.containsKey(attributeToUpgrade)) {
                ChatUtils.sendClientMessage("§cUnable to find attribute: §e$attributeToUpgrade", shortPrefix = true)
                return
            }

            val attributeShortName = ItemUtils.attributeShortNames[attributeToUpgrade]!!

            // Check if the attribute is valid
            val targetTier = args[1].toIntOrNull()

            if(targetTier == null) {
                ChatUtils.sendClientMessage("§cInvalid tier! Must be a number.", shortPrefix = true)
                return
            }

            val heldItem = ItemUtils.getHeldItem()
            if (heldItem == null) {
                ChatUtils.sendClientMessage("§cYou must be holding an item to use this command!", shortPrefix = true)
                return
            }

            val heldItemAttributes = ItemUtils.getHeldItem()?.getAttributes()
            val attributeNameFormatted = (attributeToUpgrade).replace("_"," ").toTitleCase()

            if (heldItemAttributes == null || !heldItemAttributes.containsKey(attributeToUpgrade)) {
                ChatUtils.sendClientMessage("§cYou must be holding an item with the attribute §e$attributeNameFormatted§c!", shortPrefix = true)
                return
            }

            val currentTier = heldItemAttributes[attributeToUpgrade]!!

            if (currentTier >= targetTier) {
                ChatUtils.sendClientMessage("§cYou must be holding an item with a lower tier of the attribute §e$attributeNameFormatted§c!", shortPrefix = true)
                return
            }

            ChatUtils.sendClientMessage("§eUpgrading §c§l$attributeNameFormatted $currentTier §e➡ §a§l$attributeNameFormatted $targetTier", shortPrefix = true)
            var totalPrice = 0L

            val attributeTiersNeeded = mutableMapOf<Int,Int>()
            for(i in 1 until targetTier) {
                if(i < currentTier) attributeTiersNeeded[i] = 0
                else attributeTiersNeeded[i] = 1
            }

            val heldAttributeType = getAttributeType(heldItem.getSkyblockId()!!)

            for(i in (targetTier-1) downTo 1) {
                if(attributeTiersNeeded[i] == 0) {
                    continue
                }
                // Find the cheapest auctions for the attribute
                var matching = mutableListOf<JsonObject>()

                ItemApi.liveAuctionData.entrySet().forEach {
                    // Item
                    val itemID = it.key
                    val obj = it.value.asJsonObject

                    if(getAttributeType(itemID) != heldAttributeType && itemID != "ATTRIBUTE_SHARD") {
                        return@forEach
                    }

                    // Pricing data for the item
                    obj.entrySet().forEach { entry ->
                        val attribute = entry.key
                        val attributeValue = entry.value.asJsonObject

                        if(attribute.contains("$attributeShortName$i")) {
                            attributeValue.addProperty("itemID", itemID)

                            matching.add(attributeValue)
                        }
                    }
                }

                // Check if we have any auctions for the attribute, if not add two per required, to the next tier
                if (matching.isEmpty()) {
                    if(attributeTiersNeeded[i-1] == null) {
                        ChatUtils.sendClientMessage("§c§lNot enough auctions for $attributeNameFormatted $targetTier", shortPrefix = true)
                        return
                    }
                    ChatUtils.sendClientMessage("§8Unable to find ${attributeTiersNeeded[i]}x $attributeNameFormatted $i, Getting ${2*attributeTiersNeeded[i]!!}x $attributeNameFormatted ${i-1}", shortPrefix = true)

                    attributeTiersNeeded[i-1] = attributeTiersNeeded[i-1]!! + 2*attributeTiersNeeded[i]!!
                    println("ATTRIBUTE TIER: $attributeTiersNeeded")
                    continue
                }

                // Sort the auctions by price
                matching.sortBy { it.get("price").asLong }

                // Check if we have enough auctions for the attribute, if not, add to the next lowest tier
                if(attributeTiersNeeded[i]!! > matching.size) {
                    val difference = attributeTiersNeeded[i]!! - matching.size

                    // If we have gone down to current tier, call it quits
                    if(attributeTiersNeeded[i-1] == null) {
                        ChatUtils.sendClientMessage("§c§lNot enough auctions for $attributeNameFormatted $targetTier", shortPrefix = true)
                        return
                    }

                    attributeTiersNeeded[i] = matching.size

                    // Add the difference to the next tier below
                    attributeTiersNeeded[i-1] = attributeTiersNeeded[i-1]!! + 2*difference

                    ChatUtils.sendClientMessage("§8Unable to find ${difference}x $attributeNameFormatted $i, Getting ${attributeTiersNeeded[i-1]!! + 2*difference}x $attributeNameFormatted ${i-1}", shortPrefix = true)
                }

                // Cut the list to the amount we need
                matching = matching.subList(0, attributeTiersNeeded[i]!!)

                for(matchingAuction in matching) {
                    val price = matchingAuction.get("price").asLong
                    val cheapestId = matchingAuction.get("auc_id").asString
                    val itemID = matchingAuction.get("itemID").asString
                    val itemDisplayName = ItemApi.getItemInfo(itemID)?.asJsonObject?.get("displayname")?.asString?: "§fAttribute Shard"

                    val message = ChatComponentText("$itemDisplayName §d| §b${attributeNameFormatted} $i §d| §a${price.abbreviateNumber()}")
                    message.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction $cheapestId")

                    message.chatStyle.setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("§eClick to view auction")))

                    ChatUtils.sendClientMessage(message, shortPrefix = true)

                    totalPrice += price
                }
            }

            ChatUtils.sendClientMessage("§a§lTotal Price: ${totalPrice.abbreviateNumber()}", shortPrefix = true)
        }
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
            "BLAZE_BELT" -> return "BlazeBelt"
            "GLOWSTONE_GAUNTLET" -> return "GlowstoneGauntlet"
            "GHAST_CLOAK" -> return "GhastCloak"
            "IMPLOSION_BELT" -> return "ImplosionBelt"
            "GAUNTLET_OF_CONTAGION" -> return "GauntletOfContagion"
        }
        if (!itemID.contains("CRIMSON") && !itemID.contains("AURORA") && !itemID.contains("TERROR") && !itemID.contains("FERVOR") && !itemID.contains("HOLLOW")) return null
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