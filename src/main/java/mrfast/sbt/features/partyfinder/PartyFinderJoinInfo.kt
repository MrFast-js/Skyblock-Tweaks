package mrfast.sbt.features.partyfinder

import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.LevelingUtils
import mrfast.sbt.utils.LevelingUtils.roundToTwoDecimalPlaces
import mrfast.sbt.utils.NetworkUtils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object PartyFinderJoinInfo {
    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (!DungeonConfig.partyfinderJoinInfo) return
        if (event.type.toInt() == 2) return

        val msg = event.message.unformattedText.clean()
        val regex = "^Party Finder > ([^\\s]+) joined the dungeon group! \\(([^ ]+) Level (\\d+)\\)\$"
        if (msg.matches(regex)) {
            val matcher = msg.getRegexGroups(regex) ?: return
            val playerName = matcher.group(1)
            sendPlayerInfo(playerName)
        }
    }

    private fun sendPlayerInfo(playerName: String) {
        Thread {
            val playerUuid = NetworkUtils.getUUID(playerName) ?: return@Thread
            val profileInfo = NetworkUtils.getActiveProfile(playerUuid) ?: return@Thread
            val playerProfileInfo = profileInfo.asJsonObject["members"].asJsonObject[playerUuid].asJsonObject
            val hypixelPlayerData =
                NetworkUtils.apiRequestAndParse("https://api.hypixel.net/player?uuid=$playerUuid#PartyFinderJoinMsg")
            val secrets = hypixelPlayerData.getAsJsonObject("player")
                ?.getAsJsonObject("achievements")
                ?.get("skyblock_treasure_hunter")
                ?.asInt ?: 0
            val magicPower = playerProfileInfo.getAsJsonObject("accessory_bag_storage")
                ?.get("highest_magical_power")
                ?.asInt ?: 0

            val output = ChatComponentText("\n§c§m§l--------§r §e$playerName §c§m§l--------\n")
            val titleLength = "-------- $playerName --------".length

            val cataXp = playerProfileInfo?.getAsJsonObject("dungeons")
                ?.getAsJsonObject("dungeon_types")
                ?.getAsJsonObject("catacombs")
                ?.get("experience")
                ?.asDouble ?: 0.0
            val cataLvl = LevelingUtils.calculateDungeonsLevel(cataXp)

            val statBar =
                ChatComponentText("§a • §7Cata: §6$cataLvl§a  • §7Secrets: §3${secrets.formatNumber()}§a  • §7MP: §b${magicPower.formatNumber()}\n")

            output.appendSibling(statBar)

            val invArmorBase64 = playerProfileInfo["inv_armor"]?.asJsonObject?.get("data")?.asString
            val invArmor = ItemUtils.decodeBase64Inventory(invArmorBase64)

            if (invArmor.isEmpty()) {
                output.appendText("\n§cNo armor found or their API is disabled.\n")
            } else {
                for (itemStack in invArmor) {
                    val lore = if (itemStack != null) itemStack.displayName + "\n" + itemStack.getLore().joinToString("\n") else "§cNone"
                    val armorComponent = ChatComponentText(itemStack?.displayName + "\n")

                    armorComponent.chatStyle.chatHoverEvent =
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText(lore)
                        )

                    output.appendSibling(armorComponent)
                }
            }

            output.appendText("\n")

            if (DungeonConfig.partyfinderJoinInfo_showEquipment) {
                val equipBase64 = playerProfileInfo["equippment_contents"]?.asJsonObject?.get("data")?.asString
                val equipment = ItemUtils.decodeBase64Inventory(equipBase64)

                if (equipment.isEmpty()) {
                    output.appendText("§cNo equipment found or their API is disabled.\n")
                } else {
                    for (itemStack in equipment) {
                        val lore = if (itemStack != null) itemStack.displayName + "\n" + itemStack.getLore().joinToString("\n") else "§cNone"

                        val equipComponent = ChatComponentText(itemStack?.displayName + "\n")

                        equipComponent.chatStyle.chatHoverEvent =
                            HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                ChatComponentText(lore)
                            )

                        output.appendSibling(equipComponent)
                    }
                }
                output.appendText("\n")
            }


            val inventoryBase64 = playerProfileInfo["inv_contents"]?.asJsonObject?.get("data")?.asString
            val inventory = ItemUtils.decodeBase64Inventory(inventoryBase64)
            val items = StringBuilder()

            val hype = inventory.any { it?.getSkyblockId() == "HYPERION" }
            val term = inventory.any { it?.getSkyblockId() == "TERMINATOR" }
            val clay = inventory.any { it?.getSkyblockId() == "DARK_CLAYMORE" }

            items.append((if (hype) "§aHype ✔" else "§cHype ✘") + "   ")
            items.append((if (term) "§aTerm ✔" else "§cTerm ✘") + "   ")
            items.append((if (clay) "§aClay ✔" else "§cClay ✘") + "   ")

            if(DungeonConfig.partyfinderJoinInfo_showHotbar) {
                val hotbar = ChatComponentText("")

                for ((index, itemStack) in inventory.withIndex().take(9)) {
                    val lore = if (itemStack != null) itemStack.displayName + "\n" + itemStack.getLore()
                        .joinToString("\n") else "§cNone"

                    val hotbarPart = ChatComponentText("${if (itemStack == null) "§7" else "§6"}§l[${index + 1}]")
                    hotbarPart.chatStyle.chatHoverEvent =
                        HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            ChatComponentText(lore)
                        )
                    hotbar.appendSibling(hotbarPart)
                    hotbar.appendText(" ")
                }
                output.appendSibling(hotbar)
                output.appendText("\n")
            } else {
                output.appendText(items.toString() + "\n")
            }

            val completions = ChatComponentText("§e§l[Completions]")
            val completionsHover = mutableListOf<String>()
            val completionsData = playerProfileInfo?.getAsJsonObject("dungeons")
                ?.getAsJsonObject("dungeon_types")
                ?.getAsJsonObject("catacombs")
                ?.getAsJsonObject("times_played")
            var totalFloor = 0
            if (completionsData != null) {
                for (floorCompletion in completionsData.entrySet()) {
                    var floor = "Floor " + floorCompletion.key
                    if (floorCompletion.key == "0") floor = "Entrance"
                    if (floorCompletion.key == "total") {
                        floor = "Total"
                        totalFloor = floorCompletion.value.asInt
                    }

                    completionsHover.add("§7$floor: §3${floorCompletion.value.asInt.formatNumber()}")
                }
                completions.chatStyle.chatHoverEvent =
                    HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        ChatComponentText(completionsHover.joinToString("\n"))
                    )
            }

            val kick = ChatComponentText("§4§l[Kick]")
            kick.chatStyle.chatClickEvent =
                ClickEvent(
                    ClickEvent.Action.RUN_COMMAND,
                    "/p kick $playerName"
                )
            val avgSecrets = (secrets / totalFloor.toDouble()).roundToTwoDecimalPlaces()
            statBar.chatStyle.chatHoverEvent =
                HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    ChatComponentText("§7Average Secrets: §3$avgSecrets")
                )

            output.appendSibling(completions)
            output.appendText("       ")
            output.appendSibling(kick)
            output.appendText("\n")

            output.appendText("§c§m§l${"-".repeat(titleLength - 4)}")

            ChatUtils.sendClientMessage(
                output
            )
        }.start()
    }
}