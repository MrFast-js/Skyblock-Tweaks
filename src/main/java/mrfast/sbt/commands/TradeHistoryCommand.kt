package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.features.profitTracking.ProfitTrackerGui
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import com.google.common.collect.Lists
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.guis.TradeHistoryGui

@SkyblockTweaks.CommandComponent
class TradeHistoryCommand : CommandBase() {
    override fun getCommandName(): String = "tradehistory"

    override fun getCommandAliases(): List<String> = Lists.newArrayList("tradelogs","th","tl")

    override fun getCommandUsage(sender: ICommandSender?): String = "/tradehistory"

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        GuiUtil.open(TradeHistoryGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true
}