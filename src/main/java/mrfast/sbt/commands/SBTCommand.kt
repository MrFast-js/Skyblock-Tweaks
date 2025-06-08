package mrfast.sbt.commands

import com.google.common.collect.Lists
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.AuctionHouseConfig
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DungeonConfig
import mrfast.sbt.guis.ConfigGui
import mrfast.sbt.guis.GuiEditor
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.managers.VersionManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils
import net.minecraft.command.CommandBase
import net.minecraft.command.ICommandSender
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.FMLCommonHandler

@SkyblockTweaks.CommandComponent
class SBTCommand : CommandBase() {

    override fun getCommandName(): String = "skyblocktweaks"

    override fun getCommandUsage(sender: ICommandSender?): String = "/skyblocktweaks"

    override fun getCommandAliases(): List<String> = Lists.newArrayList("sbt")

    override fun processCommand(sender: ICommandSender, args: Array<out String>) {
        if (args.isNotEmpty()) {
            when {
                args[0] == "edit" -> GuiManager.displayScreen(GuiEditor())
                args[0] == "update" -> {
                    if (args.size < 2) {
                        ChatUtils.sendClientMessage("§cInvalid Usage! §e/sbt update check|pre|full")
                        return
                    }
                    when {
                        args[1] == "close" -> {
                            FMLCommonHandler.instance().exitJava(0, false)
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
                args[0] == "blacklist" -> {
                    AuctionHouseConfig.AF_blackList.run()
                }
                args[0] == "trash" -> {
                    DungeonConfig.editTrash.run()
                }
                args[0] == "help" -> {
                    ChatUtils.sendClientMessage(
                        "§6===== §9§lSkyblock Tweaks Commands§r§6 =====§r\n" +
                                " §b• /sbt §3edit §f➡ §7Edit GUI locations\n" +
                                " §b• /sbt §3update §echeck | pre | full §f➡ §7Check for updates\n" +
                                " §b• /sbt §3blacklist | trash §f➡ §7Opens item filter menus\n" +
                                " §b• /path §f➡ §7Create custom recorded paths to save and replay!\n" +
                                " §b• /pft §f➡ §7Opens profit tracker, tracking every item gained/loss\n" +
                                " §b• /th §f➡ §7Opens trade history, which logs every trade\n" +
                                " §b• /au §f➡ §7Attribute Upgrade Path Command\n" +
                                " §b• /sbtdebug §f➡ §7Debugging commands"
                    )
                }

                else -> {
                    ChatUtils.sendClientMessage("§cInvalid subcommand, §7/sbt help§c for help")
                }
            }
        } else {
            GuiManager.displayScreen(ConfigGui())
        }
    }

    override fun canCommandSenderUseCommand(sender: ICommandSender?): Boolean = true

    override fun addTabCompletionOptions(
        sender: ICommandSender,
        args: Array<out String>,
        pos: BlockPos?
    ): List<String> {
        val subCommands = listOf("edit", "update", "blacklist", "trash", "help")
        if (args.size == 1) {
            return getListOfStringsMatchingLastWord(args, subCommands)
        }
        if (args.size == 2 && args[0] == "update") {
            return getListOfStringsMatchingLastWord(args, listOf("check", "pre", "full"))
        }
        return emptyList()
    }
}