package mrfast.sbt.commands

import gg.essential.api.utils.GuiUtil
import mrfast.sbt.managers.VersionManager
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.ConfigGui
import mrfast.sbt.config.GuiEditor
import mrfast.sbt.utils.ChatUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraftforge.fml.common.FMLCommonHandler

class ConfigCommand : CommandBase() {

    override fun getCommandName(): String {
        return "sbt"
    }

    override fun getCommandUsage(sender: ICommandSender?): String {
        return "/sbt"
    }

    override fun processCommand(sender: ICommandSender?, args: Array<out String>?) {
        if (args != null) {
            if(args.isNotEmpty()) {
                when {
                    args[0] == "edit" -> GuiUtil.open(GuiEditor())
                    args[0] == "update" -> {
                        if(args.size<2) {
                            ChatUtils.sendClientMessage("§cInvalid Usage! §e/sbt update check|pre|full")
                            return
                        }
                        when {
                            args[1] == "close" -> {
                                FMLCommonHandler.instance().exitJava(0,false)
                            }
                            args[1] == "check" -> {
                                VersionManager.checkIfNeedUpdate()
                            }
                            args[1] == "pre" || args[1] == "beta" -> {
                                CustomizationConfig.updateCheckType = "Pre (Beta Releases)"
                                VersionManager.doUpdate()
                            }
                            args[1] == "full" || args[1] == "latest" -> {
                                CustomizationConfig.updateCheckType = "Full (Full Releases)"
                                VersionManager.doUpdate()
                            }
                        }
                    }
                }
            } else {
                GuiUtil.open(ConfigGui())
            }
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean {
        return true
    }
}