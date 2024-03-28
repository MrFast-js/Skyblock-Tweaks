package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.features.generalProfitTracker.ProfitTrackerGui
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

class GPTCommand : CommandBase() {

    override fun getCommandName(): String {
        return "gpt"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/gpt"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        GuiUtil.open(ProfitTrackerGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}