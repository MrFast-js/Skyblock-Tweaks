package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.config.ConfigGui
import net.minecraft.client.Minecraft
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.ChatComponentText

class ConfigCommand : CommandBase() {

    override fun getCommandName(): String {
        return "sbt"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/sbt"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        GuiUtil.open(ConfigGui())
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}