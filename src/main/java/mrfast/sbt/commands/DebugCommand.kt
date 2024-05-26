package mrfast.sbt.commands

import com.mojang.realmsclient.gui.ChatFormatting
import mrfast.sbt.utils.*
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*
import java.util.stream.Collectors


class DebugCommand : CommandBase() {
    override fun getCommandName(): String {
        return "sbtdebug"
    }

    override fun getCommandUsage(sender: ICommandSender): String {
        return "/sbtdebug"
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
        var dist = 5
        try {
            if (args.size > 1) {
                dist = args[1].replace("[^0-9]".toRegex(), "").toInt()
            }
        } catch (ignored: Exception) { }

        when (args[0]) {
            "mobs" -> getMobData(mobs = true, distance = dist)
            "tiles" -> getMobData(tileEntities = true, distance = dist)
            "location", "loc" -> ChatUtils.sendClientMessage(ChatFormatting.GRAY.toString() + "Area:'" + LocationUtils.currentArea + "' Map:" + LocationUtils.currentIsland + " Skyblock:" + LocationUtils.inSkyblock + "'")
            "entities" -> getMobData(tileEntities = true, mobs = true, distance = dist)
            "item" -> {
                val heldItem = ItemUtils.getHeldItem()
                if (heldItem != null) {
                    getItemData(heldItem)
                } else {
                    ChatUtils.sendClientMessage(ChatFormatting.RED.toString() + "You must be holding an item!")
                }
            }
            "sidebar" -> sidebarData
            "log" -> copyLog()
            "tablist", "tab" -> tablistData
            else -> invalidUsage()
        }
    }

    companion object {
        var arguments: List<String> = mutableListOf("mobs", "tiles", "entities", "item", "sidebar", "tab")
        fun invalidUsage() {
            val usage =
                StringBuilder(ChatFormatting.RED.toString() + "Invalid Usage! " + ChatFormatting.YELLOW + "/debug ")
            for (arg in arguments) {
                usage.append(arg).append(" ")
            }
            ChatUtils.sendClientMessage(usage.toString())
        }

        val sidebarData: Unit
            get() {
                val output = StringBuilder()
                val lines: MutableList<String> = ScoreboardUtils.getSidebarLines(true).toMutableList()
                lines.add("==== Raw ====")
                lines.addAll(ScoreboardUtils.getSidebarLines(false))
                for (line in lines) {
                    output.append(line).append("\n")
                }
                ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied sidebar to clipboard!")
                Utils.copyToClipboard(output.toString())
            }

        val tablistData: Unit
            get() {
                val output = StringBuilder()
                var count = 0
                for (playerName in ScoreboardUtils.getTabListEntries()) {
                    count++
                    output.append(count).append(": ").append(playerName)
                        .append("\n")
                }
                ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied tablist to clipboard!")
                Utils.copyToClipboard(output.toString())
            }

        fun copyLog() {
            val log = File(File(Utils.mc.mcDataDir, "logs"), "latest.log")
            try {
                val lines = Files.readAllLines(log.toPath(), StandardCharsets.UTF_8)
                ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied latest.log to clipboard!")

                Utils.copyToClipboard(lines.stream().collect(Collectors.joining(System.lineSeparator())))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun getItemData(item: ItemStack) {
            ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied item nbt data to clipboard!")
            Utils.copyToClipboard(DevUtils.prettyPrintNBTtoString(item.serializeNBT()))
        }

        fun getMobData(tileEntities: Boolean = false, mobs: Boolean = false, distance: Int) {
            val player: EntityPlayerSP = Utils.mc.thePlayer
            val stringBuilder = StringBuilder()
            if (mobs) {
                stringBuilder.append(copyMobEntities(player, distance))
            }
            if (tileEntities) {
                stringBuilder.append(copyTileEntities(player, distance))
            }
            ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied nearby entity data to clipboard!")
            Utils.copyToClipboard(stringBuilder.toString())
        }

        fun copyMobEntities(player: EntityPlayerSP, distance: Int): String {
            val stringBuilder = StringBuilder()
            val loadedEntitiesCopy: MutableList<Entity> = LinkedList<Entity>(Utils.mc.theWorld.loadedEntityList)
            val loadedEntitiesCopyIterator: ListIterator<Entity>
            loadedEntitiesCopy.removeIf { entity: Entity ->
                entity.getDistanceToEntity(
                    player
                ) > distance
            }
            loadedEntitiesCopyIterator = loadedEntitiesCopy.listIterator()

            // Copy the NBT data from the loaded entities.
            while (loadedEntitiesCopyIterator.hasNext()) {
                val entity = loadedEntitiesCopyIterator.next()
                val entityData = NBTTagCompound()
                if (entity == player) continue
                stringBuilder.append("Class: ").append(entity.javaClass.getSimpleName()).append(System.lineSeparator())
                stringBuilder.append("ID: ").append(entity.entityId).append(System.lineSeparator())
                if (entity.hasCustomName() || EntityPlayer::class.java.isAssignableFrom(entity.javaClass)) {
                    stringBuilder.append("Name: ").append(entity.name).append(System.lineSeparator())
                }
                stringBuilder.append("NBT Data:").append(System.lineSeparator())
                entity.writeToNBT(entityData)
                stringBuilder.append(DevUtils.prettyPrintNBTtoString(entityData))

                // Add spacing if necessary.
                if (loadedEntitiesCopyIterator.hasNext()) {
                    stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
                }
            }
            return stringBuilder.toString()
        }

        fun copyTileEntities(player: EntityPlayerSP, distance: Int): String {
            val stringBuilder = StringBuilder()
            val loadedTileEntitiesCopy: MutableList<TileEntity> =
                LinkedList<TileEntity>(Utils.mc.theWorld.loadedTileEntityList)
            val loadedTileEntitiesCopyIterator: ListIterator<TileEntity>
            loadedTileEntitiesCopy.removeIf { entity: TileEntity ->
                player.position.distanceSq(entity.pos) > distance
            }
            loadedTileEntitiesCopyIterator = loadedTileEntitiesCopy.listIterator()

            // Copy the NBT data from the loaded entities.
            while (loadedTileEntitiesCopyIterator.hasNext()) {
                val entity = loadedTileEntitiesCopyIterator.next()
                val entityData = NBTTagCompound()
                stringBuilder.append("Class: ").append(entity.javaClass.getSimpleName()).append(System.lineSeparator())
                stringBuilder.append("NBT Data:").append(System.lineSeparator())
                entity.writeToNBT(entityData)
                stringBuilder.append(DevUtils.prettyPrintNBTtoString(entityData))

                // Add spacing if necessary.
                if (loadedTileEntitiesCopyIterator.hasNext()) {
                    stringBuilder.append(System.lineSeparator()).append(System.lineSeparator())
                }
            }
            return stringBuilder.toString()
        }
    }
}

