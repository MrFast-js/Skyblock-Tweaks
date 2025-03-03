package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import mrfast.sbt.utils.Utils.setTimeout
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object PartyManager {
    var partyMembers = mutableMapOf<String, PartyMember>()
    private var playerInParty = false

    class PartyMember(var name: String) {
        var leader = false
        var online = false
        var className = ""
        var classLvl = ""
    }

    private var hidePartyList = false
    private var gotPartyStart = false
    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val clean = event.message.unformattedText.clean()

        if(hidePartyList && gotPartyStart) event.isCanceled = true

        // Hide party start marker and messages after it
        if(clean == "-----------------------------------------------------" && hidePartyList) {
            if(gotPartyStart) {
                // Second marker
                event.isCanceled = true
                hidePartyList = false
                gotPartyStart = false
            } else {
                gotPartyStart = true
            }
            event.isCanceled = true
        }

        handleVanillaParty(clean)
        handleDungeonPartyFinder(clean)
    }

    private var hadProblemJoiningParty = false

    @SubscribeEvent
    fun slotClick(event: SlotClickedEvent) {
        if (event.gui is GuiChest && event.gui.chestName().startsWith("Party Finder")) {
            val stackName = event.slot.stack?.displayName?.clean() ?: return
            if (stackName.endsWith("'s Party")) {
                hadProblemJoiningParty = false

                for (line in event.slot.stack.getLore()) {
                    if (line.clean().startsWith("Requires")) return
                }

                setTimeout({
                    if (hadProblemJoiningParty) return@setTimeout

                    // Clear all old party members but self
                    partyMembers.entries.removeIf { it.key != Utils.mc.thePlayer.name }
                    addSelfToParty(false)

                    for (line in event.slot.stack.getLore()) {
                        val regex = """^(\w+):\s(\w+)\s\((\d+)\)$""".toRegex()
                        val clean = line.clean().trim()
                        if (clean.matches(regex)) {
                            val matcher = clean.getRegexGroups(regex) ?: return@setTimeout
                            val playerName = matcher[1]!!.value
                            val className = matcher[2]!!.value
                            val classLvl = matcher[3]!!.value

                            val pm = PartyMember(playerName)
                            pm.className = className
                            pm.classLvl = classLvl

                            partyMembers[pm.name] = pm
                        }
                    }
                    val leaderName = stackName.clean().split("'")[0]
                    partyMembers[leaderName]?.leader = true
                    playerInParty = true
                }, 1000)
            }
        }
    }

    private fun addSelfToParty(selfLeader: Boolean) {
        // Add self to party
        playerInParty = true
        if (partyMembers.containsKey(Utils.mc.thePlayer.name)) return
        val pm = PartyMember(Utils.mc.thePlayer.name)
        pm.leader = selfLeader
        partyMembers[pm.name] = pm

        if (GeneralConfig.autoPartyChat) {
            ChatUtils.sendPlayerMessage("/chat p")
        }
    }

    private fun handleVanillaParty(clean: String) {
        // Other players join party
        val JOINED_PARTY_REGEX = """^(?:\[[^\]]+\]\s*)?([^ ]+) joined the party\.""".toRegex()
        if (clean.matches(JOINED_PARTY_REGEX)) {
            val pm = PartyMember(clean.getRegexGroups(JOINED_PARTY_REGEX)!![1]!!.value)
            partyMembers[pm.name] = pm
            addSelfToParty(true)
            ChatUtils.sendPlayerMessage("/p list")
            hidePartyList = true
        }

        // Other players leave party
        val REMOVED_PARTY_REGEX = """^(?:\[[^\]]+\]\s*)?([^ ]+) has (?:left|been removed from) the party\.""".toRegex()
        if (clean.matches(REMOVED_PARTY_REGEX)) {
            partyMembers.values.removeIf {
                it.name == clean.getRegexGroups(REMOVED_PARTY_REGEX)!![1]!!.value
            }
            playerInParty = true
        }

        // /p kick people
        if (clean == "You have been kicked from the party") {
            partyMembers.clear()
            playerInParty = false
            if (GeneralConfig.autoPartyChat) {
                ChatUtils.sendPlayerMessage("/chat a")
            }
        }

        // /p transfer
        val TRANSFER_PARTY_REGEX = """^The party was transferred to (?:\[[^\]]+\]\s*)?(?<newLeader>[^ ]+)(?:(?: because (?<leavingPlayer>[^ ]+) left)|(?: by .*))?""".toRegex()
        if (clean.matches(TRANSFER_PARTY_REGEX)) {
            partyMembers.values.forEach { it.leader = false }
            val groups = clean.getRegexGroups(TRANSFER_PARTY_REGEX)!!
            val newLeader = groups["newLeader"]?.value
            partyMembers[newLeader]?.leader = true

            if (groups["leavingPlayer"] != null) {
                val playerName = groups["leavingPlayer"]!!.value
                partyMembers.remove(playerName)
            }

            playerInParty = true
        }

        // no more party 👋
        val PARTY_DESTRUCTION_REGEX = """^The party was disbanded because all invites expired and the party was empty\.|[^ ]+ has disbanded the party!|You (?:left|have left|have been kicked from|were kicked from) the party\.|You (?:are not in a party right now|are not in a party|are not currently in a party\.)""".toRegex()
        if (clean.matches(PARTY_DESTRUCTION_REGEX)) {
            partyMembers.clear()
            if (GeneralConfig.autoPartyChat && playerInParty) {
                ChatUtils.sendPlayerMessage("/chat a")
            }
            playerInParty = false
        }

        // You join party
        val JOIN_PARTY_REGX = """^You have joined (?:\[[^\]]+\]\s*)?(?<partyLeader>[^ ]+)'s party!""".toRegex()
        if (clean.matches(JOIN_PARTY_REGX)) {
            partyMembers.clear()
            val newLeader = clean.getRegexGroups(JOIN_PARTY_REGX)!!["partyLeader"]?.value!!
            val pm = PartyMember(newLeader)

            pm.leader = true
            partyMembers[pm.name] = pm
            addSelfToParty(false)
        }

        // /p list
        val PLIST_REGEX = """^Party (?:Leader|Moderators|Members): .*""".toRegex()
        if (clean.matches(PLIST_REGEX)) {
            val newCleaned = clean.replace("""((?:Party (?:Leader|Members|Moderators)):|\[[^]]+]\s*| ●)""".toRegex(),"").trim()
            val lineNames = newCleaned.split(" ")

            lineNames.forEach {
                if(partyMembers.containsKey(it)) {
                    partyMembers[it]!!.leader = false
                } else {
                    partyMembers[it] = PartyMember(it)
                }

                if(clean.startsWith("Party Leader:")) {
                    partyMembers[it]!!.leader = true
                }
            }

            playerInParty = true
        }

        // Party Leader Disconnected
        val disconnectRegex = """^(?:The party leader, )?(?:\[[^]]+] )?(?<partyLeader>.*?) has disconnected""".toRegex()
        if (clean.matches(disconnectRegex)) {
            val partyLeader = clean.getRegexGroups(disconnectRegex)!!["partyLeader"]!!.value
            partyMembers.values.forEach { it.leader = false }
            partyMembers[partyLeader]?.online = false
            partyMembers[partyLeader]?.leader = true
        }

        // Joining existing parties, Disabled because this can exceed 5 party members
        if (clean.startsWith("You'll be partying with: ")) {
            val membersLine = clean.split("You'll be partying with:")[1].trim()
            for (member in membersLine.split(", ")) {
//                val pm = PartyMember(parsePlayerName(member))
//                partyMembers[pm.name] = pm
            }
        }
    }

    private fun handleDungeonPartyFinder(clean: String) {
        val regex = """^Party Finder > (?<playerName>[^\s]+) joined the dungeon group! \((?<classType>.*) Level (?<classLvl>\d+)\)""".toRegex()
        if (clean.matches(regex)) {
            val groups = clean.getRegexGroups(regex) ?: return
            val pm = PartyMember(groups["playerName"]!!.value)

            pm.className = groups["classType"]!!.value
            pm.classLvl = groups["classLvl"]!!.value

            partyMembers[pm.name] = pm

            addSelfToParty(false)
            ChatUtils.sendPlayerMessage("/p list")
            hidePartyList = true
            if (clean.contains(Utils.mc.thePlayer.name)) {
                partyMembers[Utils.mc.thePlayer.name]?.leader = false
            }
        }

        if (clean.startsWith("Party Finder > This group has been de-listed") ||
            clean.startsWith("Party Finder > You are already in a party!") ||
            clean.startsWith("You have just sent a join request recently!")
        ) {
            hadProblemJoiningParty = true
        }
    }
}