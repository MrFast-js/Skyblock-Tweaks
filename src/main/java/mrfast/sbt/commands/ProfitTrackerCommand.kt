package mrfast.sbt.commands

import com.google.common.collect.Lists
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.features.profitTracking.ProfitTrackerGui
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

@SkyblockTweaks.CommandComponent
class ProfitTrackerCommand : CommandBase() {

    override fun getCommandName(): String = "profittracker"

    override fun getCommandAliases(): List<String> = Lists.newArrayList("pft")

    override fun getCommandUsage(sender: ICommandSender?): String = "/profittracker"

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        GuiManager.displayScreen(ProfitTrackerGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}