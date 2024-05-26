package mrfast.sbt.commands

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbt.features.mining.PathTracer
import mrfast.sbt.utils.*
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.BlockPos
import net.minecraft.util.ChatComponentText
import net.minecraft.util.EnumChatFormatting
import net.minecraft.util.Vec3
import java.util.*


class PathCommand : CommandBase() {
    override fun getCommandName(): String {
        return "path"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/path [action]"
    }

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos): List<String> {
        return arguments
    }

    override fun getRequiredPermissionLevel(): Int {
        return 0
    }

    @Throws(CommandException::class)
    override fun processCommand(arg0: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            invalidUsage()
            return
        }

        when (args[0]) {
            "start" -> {
                PathTracer.pathPoints.clear();
                PathTracer.creatingPath = true;
                ChatUtils.sendClientMessage(
                    "§3Path creation started. Use §a/path §erecord start§3 to record your movement",
                    true
                );
            }

            "add" -> {
                if (!PathTracer.creatingPath) {
                    ChatUtils.sendClientMessage("§cNo path being created. Make one with §a/path §estart", true)
                    return
                }
                if (args.size >= 4) {
                    try {
                        val x = args[1].toFloat()
                        val y = args[2].toFloat()
                        val z = args[3].toFloat()
                        ChatUtils.sendClientMessage("§3Added Point §a($x,$y,$z)§3 to your current path", true)
                        PathTracer.pathPoints.add(Vec3(x.toDouble(), y.toDouble(), z.toDouble()))
                    } catch (e: Exception) {
                        ChatUtils.sendClientMessage("§cIncorrect Command Usage:§a /path §eadd <x> <y> <z>", true)
                    }
                }
            }

            "record" -> {
                if (!PathTracer.creatingPath) {
                    ChatUtils.sendClientMessage("§cNo path being created. Make one with §a/path §estart", true);
                    return;
                }
                if (args.size >= 2) {
                    if (args[1].contains("start")) {
                        PathTracer.pathPoints.clear();
                        PathTracer.recordingMovement = true;
                        ChatUtils.sendClientMessage(
                            "§3Movement recording started started. Use §a/path §erecord stop§3 to stop",
                            true
                        );
                    }
                    if (args[1].contains("stop")) {
                        PathTracer.recordingMovement = false;
                        ChatUtils.sendClientMessage(
                            "§3Movement recording stopped. Use §a/path §esave <name>§3 to save your path",
                            true
                        );
                    }
                } else {
                    ChatUtils.sendClientMessage("§cIncorrect Command Usage:§a /path §erecord <start|stop>", true);
                }
            }

            "load" -> {
                if (args.size >= 2) {
                    PathTracer.loadPath(args[1]);
                } else {
                    ChatUtils.sendClientMessage("§cYou didnt specify the name of path.§a /path §eload <name>", true);
                }
            }

            "unload" -> {
                PathTracer.pathPoints.clear();
                ChatUtils.sendClientMessage("§cCurrent path unloaded", true);
            }

            "list" -> {
                ChatUtils.sendClientMessage("§9§l➜ Saved Paths:",true)
                for ((pathName,value) in PathTracer.pathsAndPoints.entrySet()) {
                    val message = ChatComponentText(" §3${pathName}")
                    message.setChatStyle(
                        message.chatStyle
                            .setChatClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, "/path load $pathName"))
                            .setChatHoverEvent(
                                HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    ChatComponentText(EnumChatFormatting.GREEN.toString() + "/path load " + pathName)
                                )
                            )
                    )
                    ChatUtils.sendClientMessage(message)
                }
                if (PathTracer.pathsAndPoints.entrySet().size == 0) {
                    ChatUtils.sendClientMessage("§cNo paths found.", true)
                }
            }

            "save" -> {
                if (args.size >= 2) {
                    PathTracer.savePath(args[1]);
                    PathTracer.creatingPath = false;
                    PathTracer.recordingMovement = false;
                } else {
                    ChatUtils.sendClientMessage("§cYou didnt specify the name of path.§a /path §esave <name>", true);
                }
            }

            "delete" -> {
                if (args.size >= 2) {
                    if (PathTracer.pathsAndPoints.has(args[1])) {
                        PathTracer.pathsAndPoints.remove(args[1]);
                        ChatUtils.sendClientMessage("§cDeleted Path §a" + args[1], true);
                        PathTracer.savePaths();
                    }
                } else {
                    ChatUtils.sendClientMessage("§cYou didnt specify the name of path.§a /path §edelete <name>", true);
                }
            }

            else -> invalidUsage()
        }
    }

    companion object {
        var arguments: List<String> =
            mutableListOf("start", "add", "record", "load", "unload", "list", "save", "delete")

        fun invalidUsage() {
            val usage = ChatFormatting.RED.toString() + "Invalid Usage!\n" +
                    " §e/path §3start §l➡ §bStarts the path creating process" + "\n" +
                    " §e/path §3add <x> <y> <z> §l➡ §bAdds a point to the path your creating" + "\n" +
                    " §e/path §3record <start|stop> §l➡ §bStarts/stops recording your movement on the path your creating" + "\n" +
                    " §e/path §3load <name>§l➡ §bLoads a saved path" + "\n" +
                    " §e/path §3unload §l➡ §bUnloads your current path" + "\n" +
                    " §e/path §3list §l➡ §bLists your saved paths" + "\n" +
                    " §e/path §3save <name>§l➡ §bSaves created path as file" + "\n" +
                    " §e/path §3delete <name>§l➡ §bDeletes a saved path"
            ChatUtils.sendClientMessage(usage)
        }
    }
}

