package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import net.minecraftforge.fml.common.Loader

/*
This file to going to be used to ensure compatability across various mods
 */
@SkyblockTweaks.EventComponent
object CompatabilityManager {
    private var loadedMods = mutableListOf<String>()
    var usingSkyhanni = false

    init {
        Loader.instance().modList.forEach {
            loadedMods.add(it.modId)
        }
        usingSkyhanni = isModPresent("skyhanni")
    }

    fun isModPresent(modId: String): Boolean {
        return loadedMods.contains(modId)
    }
}