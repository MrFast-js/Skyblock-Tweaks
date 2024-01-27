package mrfast.sbt

import mrfast.sbt.apis.PlayerStats
import mrfast.sbt.commands.ConfigCommand
import mrfast.sbt.config.Config
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

@Mod(modid = SkyblockTweaks.MOD_ID, name = SkyblockTweaks.MOD_NAME, version = SkyblockTweaks.MOD_VERSION)
class SkyblockTweaks {
    companion object {
        const val MOD_ID = "skyblocktweaks"
        const val MOD_NAME = "Skyblock Tweaks"
        const val MOD_VERSION = "1.0.0"
        val config = Config
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        println("Loading config for SBT")
        config.load();
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        // Your init code here
        MinecraftForge.EVENT_BUS.register(this)
        // Api's
        MinecraftForge.EVENT_BUS.register(PlayerStats)
        ClientCommandHandler.instance.registerCommand(ConfigCommand())
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent?) {
        // Your post-init code here
        println("Saving config for SBT")
        config.save();
    }
}
