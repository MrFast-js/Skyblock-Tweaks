package mrfast.sbt.commands

import com.google.common.collect.Lists
import gg.essential.api.utils.GuiUtil
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.features.partyfinder.DungeonPartyGui
import mrfast.sbt.managers.PartyManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

@SkyblockTweaks.CommandComponent
class DungeonPartyCommand : CommandBase() {

    override fun getCommandName(): String {
        return "dungeonparty"
    }

    override fun getCommandAliases(): List<String> {
        return Lists.newArrayList("dp","pm") // pm -> "party manager"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/dp"
    }

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (PartyManager.partyMembers.size <= 1) {
            ChatUtils.sendClientMessage(
                "§cYou must be in a party with players to use the Dungeon Party Gui!",
                prefix = true
            )
            GuiUtils.closeGui()
            return
        }
        GuiUtil.open(DungeonPartyGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}