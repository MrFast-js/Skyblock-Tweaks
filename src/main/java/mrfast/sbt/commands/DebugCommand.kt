package mrfast.sbt.commands

import com.google.common.collect.Lists
import com.mojang.realmsclient.gui.ChatFormatting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.managers.ConfigManager
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.PaidPriceManager
import mrfast.sbt.utils.*
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.toDateTimestamp
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.command.CommandBase
import net.minecraft.command.CommandException
import net.minecraft.command.ICommandSender
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import java.awt.Desktop
import java.io.File
import java.util.*

@SkyblockTweaks.CommandComponent
class DebugCommand : CommandBase() {
    private var subcommands: List<Pair<String, String>> =
        mutableListOf(
            Pair("mobs", "Get nearby mob data"),
            Pair("dev", "Toggle developer mode"),
            Pair("tiles", "Get nearby tile entities"),
            Pair("entities", "Get all nearby entities"),
            Pair("item", "Get held item data"),
            Pair("sidebar", "Get data from Hypixel sidebar"),
            Pair("tab", "Get data from Hypixel tab-list"),
            Pair("pricepaid", "Get data about the Paid Price Map"),
            Pair("location", "Get SBT location data"),
            Pair("stats", "Get SBT player stats"),
            Pair("log §7<-copy>?", "Open or copy latest.log"),
            Pair("read/write <location> <value>", "Read/Write data to variables"),
            Pair("config §7<-copy>?", "Open or copy config.json"),
            Pair("gui §7<-copy>?", "Open or copy guiConfig.json"),
            Pair("data §7<-copy>?", "Open or copy profilesData.json"),
            Pair("folder", "Open SBT mod folder"),
        )

    override fun getCommandName(): String = "sbtdebug"

    override fun getCommandUsage(sender: ICommandSender): String = "/sbtdebug"

    override fun getCommandAliases(): List<String> = Lists.newArrayList("sbtd")

    override fun getRequiredPermissionLevel(): Int = 0

    @Throws(CommandException::class)
    override fun processCommand(arg0: ICommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            invalidUsage()
            return
        }
        var dist = 5
        try { if (args.size > 1) { dist = args[1].replace("[^0-9]".toRegex(), "").toInt() } } catch (ignored: Exception) { }
        var copyToClipboard = false
        if (args.size > 1) {
            if (args[1] == "-copy") copyToClipboard = true
        }

        when (args[0]) {
            "dev" -> {
                if(CustomizationConfig.developerMode) {
                    CustomizationConfig.developerMode = false
                    ChatUtils.sendClientMessage("§cDeveloper mode disabled!", shortPrefix = true)
                } else {
                    CustomizationConfig.developerMode = true
                    ChatUtils.sendClientMessage("§aDeveloper mode enabled!", shortPrefix = true)
                }
            }
            "sounds" -> {
                val soundList = DevUtils.getRecentSounds()
                val soundListString = StringBuilder()

                for (sound in soundList) {
                    soundListString.append("§7${sound.name} §aPitch: ${sound.pitch} §6Vol: ${sound.volume} §6${sound.time.toDateTimestamp(true)}").append("\n")
                }

                ChatUtils.sendClientMessage(soundListString.toString(), shortPrefix = true)
                ChatUtils.sendClientMessage("§aCopied sounds to clipboard!", shortPrefix = true)
                Utils.copyToClipboard(soundListString.toString().clean())
            }
            "mobs" -> getMobData(mobs = true, distance = dist)
            "tiles" -> getMobData(tileEntities = true, distance = dist)
            "location", "loc" -> {
                ChatUtils.sendClientMessage(
                    "§6Area: '${LocationManager.currentArea}'\n" +
                            "§bIsland: '${LocationManager.currentIsland}'\n" +
                            "§cDungeons: '${LocationManager.inDungeons}'\n" +
                            "§eMaster Mode: '${LocationManager.inMasterMode}'\n" +
                            "§aDungeon Floor: '${LocationManager.dungeonFloor}'\n" +
                            "§dSkyblock: '${LocationManager.inSkyblock}'", shortPrefix = true
                )
            }

            "stats", "stat" -> {
                ChatUtils.sendClientMessage(
                    "§cHealth: ${PlayerStats.health} | Max: ${PlayerStats.health} | §6Absorb: ${PlayerStats.absorption}\n" +
                            "§9Mana: ${PlayerStats.mana} | Max: ${PlayerStats.maxMana} | §3Overflow: ${PlayerStats.overflowMana}\n" +
                            "§dRift Time: ${PlayerStats.riftTimeSeconds} | Max: ${PlayerStats.maxRiftTime}\n" +
                            "§aDefense: ${PlayerStats.defense} | Effective: ${PlayerStats.effectiveHealth} | Effective Max: ${PlayerStats.maxEffectiveHealth}", shortPrefix = true
                )
            }

            "refresh", "reload" -> {
                CoroutineScope(Dispatchers.Default).launch {
                    ChatUtils.sendClientMessage("§7Reconnecting to SBT Websocket..", shortPrefix = true)
                    SocketUtils.setupSocket()

                    while (!SocketUtils.socketConnected) {
                        delay(300)
                    }

                    ChatUtils.sendClientMessage("§aConnected to SBT Websocket!", shortPrefix = true)
                    Utils.playSound("random.orb", 0.5)

                    delay(300)

                    ChatUtils.sendClientMessage("§7Reloading Skyblock Item Data..", shortPrefix = true)
                    ItemApi.updateSkyblockItemData(logging = true, force = true)

                    while (ItemApi.getSkyblockItems().entrySet().isEmpty()) {
                        delay(300)
                    }

                    ChatUtils.sendClientMessage("§aLoaded Skyblock Item Data! §7(${ItemApi.getSkyblockItems().entrySet().size} items)", shortPrefix = true)
                    Utils.playSound("random.orb", 0.1)
                }
            }

            "entities" -> getMobData(tileEntities = true, mobs = true, dist)
            "item" -> getItemData()
            "sidebar" -> getSidebarData()
            "write", "read" -> {
                reflect(args)
            }

            "folder" -> {
                val folder = ConfigManager.modDirectoryPath
                openFile(folder, false)
            }

            "test" -> {

            }

            "pricepaid","pp" -> {
                if(args.size > 1) {
                    if(args[1] == "clear") {
                        PaidPriceManager.clearPricePaid()
                        ChatUtils.sendClientMessage("§aCleared price paid data!", shortPrefix = true)
                        return
                    }
                } else {
                    ChatUtils.sendClientMessage("§aThere is ${PaidPriceManager.pricePaidItemCount()} items in the price paid data!", shortPrefix = true)
                }
            }

            "log" -> {
                val log = File(File(Utils.mc.mcDataDir, "logs"), "latest.log")
                openFile(log, copyToClipboard)
            }

            "data" -> {
                val data = File(ConfigManager.modDirectoryPath, "data/profilesData.json")
                openFile(data, copyToClipboard)
            }

            "config" -> {
                val data = File(ConfigManager.modDirectoryPath, "config.json")
                openFile(data, copyToClipboard)
            }

            "gui" -> {
                val data = File(ConfigManager.modDirectoryPath, "guiConfig.json")
                openFile(data, copyToClipboard)
            }

            "tablist", "tab" -> getTablistData()
            else -> invalidUsage()
        }
    }

    private fun invalidUsage() {
        ChatUtils.sendClientMessage("§cInvalid Usage!")
        subcommands.forEach {
            ChatUtils.sendClientMessage(" §b• /sbtdebug §3${it.first} §f➡ §7${it.second}", shortPrefix = true)
        }
    }

    private fun reflect(args: Array<String>) {
        if (args.size < 2) {
            ChatUtils.sendClientMessage("§cInvalid Usage! §e/sbtdebug write/read <loc> <value>", shortPrefix = true)
            return
        }
        val location = args[1].split(".").dropLast(1).joinToString(".")
        val variableName = args[1].split(".").last()
        runCatching {
            val clazz = Class.forName(location)
            val field = clazz.getDeclaredField(variableName).apply { isAccessible = true }

            if (args[0] == "write") {
                val valueStr = args[2]
                val value: Any? = when {
                    valueStr.equals("true", ignoreCase = true) || valueStr.equals(
                        "false",
                        ignoreCase = true
                    ) -> valueStr.toBoolean()

                    valueStr.contains('.') -> valueStr.toDoubleOrNull()
                    else -> valueStr.toIntOrNull()
                }
                if (value != null) {
                    field.set(clazz.kotlin.objectInstance, value)
                }
            }
            if (args[0] == "read") {
                ChatUtils.sendClientMessage(field.get(clazz.kotlin.objectInstance).toString())
            }
        }.onFailure { e ->
            when (e) {
                is ClassNotFoundException -> ChatUtils.sendClientMessage("Class not found: $location")
                is NoSuchFieldException -> ChatUtils.sendClientMessage("Field not found: $variableName")
                else -> ChatUtils.sendClientMessage("An error occurred: ${e.message}")
            }
        }
    }

    private fun getSidebarData() {
        val output = StringBuilder()
        val lines: MutableList<String> = ScoreboardUtils.getSidebarLines(true).toMutableList()
        lines.add("==== Raw ====")
        lines.addAll(ScoreboardUtils.getSidebarLines(false))
        for (line in lines) {
            output.append(line).append("\n")
        }
        ChatUtils.sendClientMessage("§aCopied sidebar to clipboard!", shortPrefix = true)
        Utils.copyToClipboard(output.toString())
    }

    private fun getTablistData() {
        val output = StringBuilder()
        var count = 0
        for (playerName in ScoreboardUtils.getTabListEntries()) {
            count++
            output.append(count).append(": ").append(playerName)
                .append("\n")
        }
        ChatUtils.sendClientMessage("§aCopied tablist to clipboard!", shortPrefix = true)
        Utils.copyToClipboard(output.toString())
    }

    private fun getItemData() {
        val heldItem = ItemUtils.getHeldItem()
        if (heldItem != null) {
            ChatUtils.sendClientMessage("§aCopied item nbt data to clipboard!", shortPrefix = true)
            Utils.copyToClipboard(DevUtils.prettyPrintNBTtoString(heldItem.serializeNBT()))
        } else {
            ChatUtils.sendClientMessage("§cYou must be holding an item!", shortPrefix = true)
        }
    }

    private fun openFile(file: File, copyContents: Boolean) {
        try {
            if (copyContents) {
                Utils.copyToClipboard(file.readText())
                ChatUtils.sendClientMessage("§aCopied ${file.name} to clipboard!", shortPrefix = true)
                return
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file)

            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle the exception according to your needs
        }
    }

    private fun getMobData(tileEntities: Boolean = false, mobs: Boolean = false, distance: Int) {
        val player: EntityPlayerSP = Utils.mc.thePlayer
        val stringBuilder = StringBuilder()
        if (mobs) {
            stringBuilder.append(copyMobEntities(player, distance))
        }
        if (tileEntities) {
            stringBuilder.append(copyTileEntities(player, distance))
        }
        ChatUtils.sendClientMessage("${ChatFormatting.GREEN}Copied nearby entity data to clipboard!", shortPrefix = true)
        Utils.copyToClipboard(stringBuilder.toString())
    }

    private fun copyMobEntities(player: EntityPlayerSP, distance: Int): String {
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

    private fun copyTileEntities(player: EntityPlayerSP, distance: Int): String {
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

    override fun addTabCompletionOptions(sender: ICommandSender, args: Array<String>, pos: BlockPos?): List<String> {
        val subCommands = subcommands.map { it.first.split(" ")[0] }
        return when (args.size) {
            1 -> getListOfStringsMatchingLastWord(args, subCommands)
            2 -> when (args[0]) {
                "log", "config", "gui", "data" -> getListOfStringsMatchingLastWord(args, listOf("-copy"))
                "read", "write" -> getListOfStringsMatchingLastWord(args, listOf("<location>"))
                else -> emptyList()
            }

            else -> emptyList()
        }
    }
}

