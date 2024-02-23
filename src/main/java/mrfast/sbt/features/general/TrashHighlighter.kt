package mrfast.sbt.features.general

import mrfast.sbt.config.ConfigManager
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.config.categories.MiscellaneousConfig.trashHighlightType
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.LocationUtils
import net.minecraft.client.gui.Gui
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import java.awt.Desktop
import java.io.*
import java.nio.file.*
import java.nio.file.StandardWatchEventKinds.*
import java.util.*

object TrashHighlighter {
    private var trashFile: File? = null
    private val trashList: MutableList<String> = ArrayList()

    init {
        initTrashFile()
    }

    fun openTrashFile() {
        if (Desktop.isDesktopSupported() && trashFile != null) {
            try {
                Desktop.getDesktop().open(trashFile)
            } catch (e: IOException) {
                e.printStackTrace() // Handle the exception according to your needs
            }
        }
    }

    private fun initTrashFile() {
        trashFile = File(ConfigManager.modDirectoryPath, "trash.txt")
        if (!trashFile!!.exists()) {
            try {
                trashFile!!.createNewFile()
                // Default values
                writeTextToFile(
                    "CRYPT_DREADLORD_SWORD\nMACHINE_GUN_BOW\nHealing VIII\nDUNGEON_LORE_PAPER\nENCHANTED_BONE\nCRYPT_BOW\nZOMBIE_SOLDIER\nSKELETON_SOLDIER\nSKELETON_MASTER\nSUPER_HEAVY\nINFLATABLE_JERRY\nDUNGEON_TRAP\nSKELETOR\nPREMIUM_FLESH\nTRAINING\nCONJURING_SWORD\nFEL_PEARL\nZOMBIE_KNIGHT\nENCHANTED_ROTTEN_FLESH\n"
                )
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            refreshTrashList()
            watchFileForChanges()
        }
    }

    private fun refreshTrashList() {
        trashList.clear()
        try {
            BufferedReader(FileReader(trashFile)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    trashList.add(line!!)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun watchFileForChanges() {
        try {
            val path = Paths.get(trashFile!!.absolutePath).toAbsolutePath().parent
            val watchService = FileSystems.getDefault().newWatchService()
            path.register(watchService, ENTRY_MODIFY)

            val watchThread = Thread {
                try {
                    while (true) {
                        val key = watchService.take()
                        for (event in key.pollEvents()) {
                            if (event.kind() == ENTRY_MODIFY) {
                                // File modified, refresh the array
                                refreshTrashList()
                            }
                        }
                        key.reset()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            watchThread.isDaemon = true
            watchThread.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeTextToFile(text: String) {
        try {
            BufferedWriter(FileWriter(trashFile)).use { writer ->
                writer.write(text)
            }
        } catch (e: IOException) {
            e.printStackTrace() // Handle the exception according to your needs
        }
    }

    @SubscribeEvent
    fun onDrawSlots(event: SlotDrawnEvent.Post) {
        if (!LocationUtils.inSkyblock || !event.slot.hasStack) return
        val stack = event.slot.stack
        val x = event.slot.xDisplayPosition
        val y = event.slot.yDisplayPosition
        val n = stack.getSkyblockId()

        if (MiscellaneousConfig.highlightTrash && n != null) {
            var trash = false
            try {
                trash = trashList.any { s -> n.contains(s) }
            } catch (ignored: ConcurrentModificationException) {
            }
            if (trash) {
                if(trashHighlightType == "Slot") {
                    Gui.drawRect(x, y, x + 16, y + 16, Color(255, 0, 0, 100).rgb)
                }
                if(trashHighlightType == "Border") {
                    Gui.drawRect(x, y, x + 16, y + 1, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x, y, x + 1, y + 16, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x + 15, y, x + 16, y + 16, Color(255, 0, 0, 255).rgb)
                    Gui.drawRect(x, y + 15, x + 16, y + 16, Color(255, 0, 0, 255).rgb)
                }
            }
        }
    }
}