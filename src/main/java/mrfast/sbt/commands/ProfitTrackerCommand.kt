package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.features.profit_tracking.ProfitTrackerGui
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class ProfitTrackerCommand : CommandBase() {

    override fun getCommandName(): String {
        return "profittracker"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/profittracker"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        GuiUtil.open(ProfitTrackerGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}