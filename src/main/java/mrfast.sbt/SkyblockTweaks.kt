package mrfast.sbt

import mrfast.sbt.SkyblockTweaks
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@Mod(modid = SkyblockTweaks.MOD_ID, name = SkyblockTweaks.MOD_NAME, version = SkyblockTweaks.MOD_VERSION)
class SkyblockTweaks {
    companion object {
        const val MOD_ID = "skyblocktweaks"
        const val MOD_NAME = "Skyblock Tweaks"
        const val MOD_VERSION = "1.0.0"
    }

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        // Your pre-init code here
        MinecraftForge.EVENT_BUS.register(this)
    }

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent?) {
        // Your init code here
    }

    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent?) {
        // Your post-init code here
    }
}
