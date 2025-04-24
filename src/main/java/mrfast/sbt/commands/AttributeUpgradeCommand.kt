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
            for(i in currentTier until targetTier) {
                if(i == 0) continue
                attributeTiersNeeded[i] = 1
            }

            for(i in (targetTier-1) downTo currentTier) {
                // Find the cheapest auctions for the attribute
                var matching = mutableListOf<JsonObject>()

                ItemApi.liveAuctionData.get(heldItem.getSkyblockId()).asJsonObject.entrySet().forEach {
                    if(it.key.contains("$attributeShortName$i")) {
                        matching.add(it.value.asJsonObject)
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
                    continue
                }

                // Sort the auctions by price, 0 being cheapest
                matching.sortBy { it.asJsonObject.get("price").asLong }

                // Check if we have enough auctions for the attribute, if not, add to the next lowest tier
                if(attributeTiersNeeded[i]!! > matching.size) {
                    val difference = attributeTiersNeeded[i]!! - matching.size

                    // If we have gone down to current tier, call it quits
                    if(attributeTiersNeeded[i-1] == null) {
                        ChatUtils.sendClientMessage("§c§lNot enough auctions for $attributeNameFormatted $targetTier", shortPrefix = true)
                        return
                    }

                    // Add the difference to the next tier below
                    attributeTiersNeeded[i-1] = attributeTiersNeeded[i-1]!! + 2*difference

                    ChatUtils.sendClientMessage("§cUnable to find $attributeNameFormatted $i", shortPrefix = true)
                    continue
                }

                // Cut the list to the amount we need
                matching = matching.subList(0, attributeTiersNeeded[i]!!)

                for(matchingAuction in matching) {
                    val price = matchingAuction.get("price").asLong
                    val cheapestId = matchingAuction.get("auc_id").asString
                    val itemDisplayName = ItemApi.getItemInfo(heldItem.getSkyblockId()!!)?.asJsonObject?.get("displayname")?.asString
                    val message = ChatComponentText("$itemDisplayName §d| §b${attributeNameFormatted} $i §d| §a${price.abbreviateNumber()}")
                    message.chatStyle.chatClickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewauction $cheapestId")
                    ChatUtils.sendClientMessage(message, shortPrefix = true)

                    totalPrice += price
                }
            }

            ChatUtils.sendClientMessage("§a§lTotal Price: ${totalPrice.abbreviateNumber()}", shortPrefix = true)
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}