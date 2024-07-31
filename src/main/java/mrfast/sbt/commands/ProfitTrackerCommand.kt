package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.features.profitTracking.ProfitTrackerGui
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import com.google.common.collect.Lists
import mrfast.sbt.SkyblockTweaks

@SkyblockTweaks.CommandComponent
class ProfitTrackerCommand : CommandBase() {

    override fun getCommandName(): String {
        return "profittracker"
    }

    override fun getCommandAliases(): List<String> {
        return Lists.newArrayList("pft")
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/profittracker"
    }

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        GuiUtil.open(ProfitTrackerGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}