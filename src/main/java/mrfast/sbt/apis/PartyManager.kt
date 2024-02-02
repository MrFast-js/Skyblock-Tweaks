package mrfast.sbt.apis

import mrfast.sbt.config.Categories.GeneralConfig
import mrfast.sbt.config.GuiManager
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.setTimeout
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object PartyManager {
    private var partyMembers = mutableMapOf<String, PartyMember>()

    class PartyMember(var name: String) {
        var leader = false
        var className = ""
        var classLvl = ""
    }

    private fun parsePlayerName(message: String): String {
        val clean = message.clean()
        var noRankName = clean
        // Remove rank
        if (clean.contains("]")) {
            noRankName = clean.split("] ")[1]
        }
        // Just get name
        noRankName = noRankName.split(" ")[0]
        return noRankName
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val clean = event.message.unformattedText.clean()

        handleVanillaParty(clean)
        handleDungeonParty(clean)
    }

    private var hadProblemJoiningParty = false

    @SubscribeEvent
    fun slotClick(event: SlotClickedEvent) {
        if (event.gui is GuiChest && event.gui.chestName().startsWith("Party Finder")) {
            val stackName = event.slot.stack?.displayName?.clean() ?: return
            if (stackName.endsWith("'s Party")) {
                val regex = "^(\\w+):\\s(\\w+)\\s\\((\\d+)\\)$"
                val pattern = Pattern.compile(regex)
                hadProblemJoiningParty = false
                setTimeout({
                    if(hadProblemJoiningParty) return@setTimeout
                    // Clear all old party members but self
                    partyMembers.entries.removeIf { it.key != Utils.mc.thePlayer.name }

                    for (line in event.slot.stack.getLore()) {
                        val matcher = pattern.matcher(line.clean().trim())
                        if (matcher.find()) {
                            // Extract information using groups
                            val playerName = matcher.group(1)
                            val className = matcher.group(2)
                            val classLvl = matcher.group(3)

                            val pm = PartyMember(playerName)
                            pm.className = className
                            pm.classLvl = classLvl

                            partyMembers[pm.name] = pm
                        }
                    }
                    val leaderName = stackName.clean().split("'")[0]
                    partyMembers[leaderName]?.leader = true
                },1000)
            }
        }
    }

    private fun handleVanillaParty(clean: String) {
        // Other players join party
        if (clean.endsWith("joined the party.")) {
            val pm = PartyMember(parsePlayerName(clean))
            partyMembers[pm.name] = pm
        }

        // Other players leave party
        if (clean.endsWith("has left the party.")) {
            partyMembers.values.removeIf {
                it.name == parsePlayerName(clean)
            }
        }

        // /p kick people
        if (clean.endsWith(" has been removed from the party.")) {
            val pm = PartyMember(parsePlayerName(clean))
            partyMembers[pm.name] = pm
        }

        // /p kick people
        if (clean.startsWith("You have been kicked from the party")) {
            partyMembers.clear()
        }

        // /p transfer
        if (clean.endsWith("The party was transferred to ")) {
            val startIdx = clean.indexOf("The party was transferred to ") + "The party was transferred to ".length
            val endIdx = if (clean.contains(" because ")) clean.indexOf("because") else clean.indexOf("of")
            partyMembers.values.forEach {
                it.leader = false
            }
            if (startIdx >= 0 && endIdx >= 0) {
                val playerName = clean.substring(startIdx, endIdx).trim()
                partyMembers[playerName]?.leader = true
            }
            if (clean.endsWith("left")) {
                val playerName = clean.split(" ")[clean.split(" ").size - 1]
                partyMembers.remove(playerName)
            }
        }

        // no more party 👋
        if (clean.startsWith("The party was disbanded because all invites expired and the party was empty.") ||
            clean.endsWith(" has disbanded the party!") ||
            clean.startsWith("You left the party.") ||
            clean.startsWith("You have been kicked from the party") ||
            clean.startsWith("You are not in a party right now.")
        ) {
            partyMembers.clear()
        }

        // You join party
        if (clean.startsWith("You have joined") && clean.endsWith("'s party!")) {
            partyMembers.clear()
            val inviter = clean.substring(15, clean.indexOf("'"))
            val pm = PartyMember(parsePlayerName(inviter))

            pm.leader = true
            partyMembers[pm.name] = pm
        }

        // Joining existing parties
        if (clean.startsWith("You'll be partying with: ")) {
            val membersLine = clean.split("You'll be partying with:")[1].trim()
            println("$membersLine MEMBERS LINE")
            for (member in membersLine.split(", ")) {
                val pm = PartyMember(parsePlayerName(member))
                partyMembers[pm.name] = pm
            }
        }
    }

    private fun handleDungeonParty(clean: String) {
        if (clean.startsWith("Party Finder > ") && clean.contains(" joined the dungeon group! (")) {
            val playerName = clean.split(" ")[3]
            val pm = PartyMember(playerName)
            val lastArg = clean.split("(")[1]
            val className = lastArg.split(" ")[0]
            val classLvl = lastArg.split(" ")[2].substringBefore(")")
            pm.className = className
            pm.classLvl = classLvl

            partyMembers[pm.name] = pm
        }
        if(clean.startsWith("You need to have a class at level") ||
            clean.startsWith("You need to have Catacombs level") ||
            clean.startsWith("Party Finder > This group has been de-listed") ||
            clean.startsWith("Party Finder > You are already in a party!") ||
            clean.startsWith("You have just sent a join request recently!")) {
            hadProblemJoiningParty = true
        }
    }

    init {
        PartyDisplay()
    }

    class PartyDisplay : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Party Display"
            this.addToList()
            this.height = Utils.mc.fontRendererObj.FONT_HEIGHT
        }

        override fun draw() {
            var display = "§9Party Display\n"
            for (partyMember in partyMembers.values) {
                display += "${partyMember.name} ${partyMember.className} ${partyMember.classLvl} LEADER:${partyMember.leader}\n"
            }
            for ((index, s) in display.split("\n").withIndex()) {
                GuiUtils.drawText(s, 0f, (index * 10).toFloat(), GuiUtils.TextStyle.BLACK_OUTLINE)
            }
            this.width = Utils.mc.fontRendererObj.getStringWidth(display) + 1
        }

        override fun isActive(): Boolean {
            return GeneralConfig.manaDisplay && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            return true
        }
    }
}