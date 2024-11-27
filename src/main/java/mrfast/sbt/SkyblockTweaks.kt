package mrfast.sbt

import mrfast.sbt.config.Config
import mrfast.sbt.guis.ConfigGui
import mrfast.sbt.managers.GuiManager
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.utils.Utils
import net.minecraft.command.ICommand
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.Loader
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.reflections.Reflections
import kotlin.reflect.full.createInstance

@Mod(modid = SkyblockTweaks.MOD_ID, name = SkyblockTweaks.MOD_NAME)
class SkyblockTweaks {
    companion object {
        const val MOD_ID = "skyblocktweaks"
        const val MOD_NAME = "Skyblock Tweaks"
        var MOD_VERSION = "1.0.0"
        val config = Config()
    }


    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent?) {
        config.loadConfig()
    }

    @Target(AnnotationTarget.CLASS)
    annotation class EventComponent

    @Target(AnnotationTarget.CLASS)
    annotation class CommandComponent

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        MinecraftForge.EVENT_BUS.register(this)

        val reflections = Reflections("mrfast.sbt")

        // Use Kotlin reflection to register each file
        val filesToRegister = reflections.getTypesAnnotatedWith(EventComponent::class.java)
        filesToRegister.forEach {
            try {
                MinecraftForge.EVENT_BUS.register(it.kotlin.objectInstance)
            } catch (_: NullPointerException) {
                throw Error("Failed to register event component: ${it.simpleName} - Make sure it's an object not a class")
            }
        }

        // Commands
        val commandsToRegister = reflections.getTypesAnnotatedWith(CommandComponent::class.java)
        commandsToRegister.forEach { ClientCommandHandler.instance.registerCommand(it.kotlin.createInstance() as ICommand?) }

        // Checks mod folder for version of Skyblock Tweaks your using
        MOD_VERSION = Loader.instance().modList.find { it.modId == MOD_ID }!!.displayVersion
    }

    // Save and load data to ensure no problems during startup
    @Mod.EventHandler
    fun postInit(event: FMLPostInitializationEvent?) {
        config.saveConfig()
        GuiManager.loadGuiElements()
        GuiManager.saveGuiElements()
    }

    // Stop the ESC key from closing config when keybind listening
    @SubscribeEvent
    fun onGuiKeyEvent(event: KeyboardInputEvent.Pre) {
        if (ConfigGui.listeningForKeybind && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            event.setCanceled(true)
        }
    }

    // Sends custom event for the world loading, only once, as WorldEvent.Load is called twice
    private var alreadySent = false

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        if (alreadySent) return
        alreadySent = true

        MinecraftForge.EVENT_BUS.post(WorldLoadEvent())

        Utils.setTimeout({
            alreadySent = false
        }, 2000)
    }
}
