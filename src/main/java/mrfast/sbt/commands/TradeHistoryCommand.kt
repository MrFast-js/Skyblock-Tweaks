package mrfast.sbt.commands

import com.google.common.collect.Lists
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.guis.TradeHistoryGui
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.utils.GuiUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender

@SkyblockTweaks.CommandComponent
class TradeHistoryCommand : CommandBase() {
    override fun getCommandName(): String = "tradehistory"

    override fun getCommandAliases(): List<String> = Lists.newArrayList("tradelogs", "tradelog", "th", "tl")

    override fun getCommandUsage(sender: ICommandSender?): String = "/tradehistory"

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        GuiManager.displayScreen(TradeHistoryGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}