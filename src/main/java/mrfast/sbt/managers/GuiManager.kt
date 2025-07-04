package mrfast.sbt.managers

import com.google.gson.GsonBuilder
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.guis.ConfigGui
import mrfast.sbt.guis.GuiEditor
import mrfast.sbt.utils.Utils
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.GuiScreenEvent
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import org.lwjgl.input.Keyboard
import java.io.FileReader
import java.io.FileWriter

@SkyblockTweaks.EventComponent
object GuiManager {
    var guiElements = mutableListOf<Element>()
    private val guiConfigFilePath = ConfigManager.modDirectoryPath.resolve("guiConfig.json")
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private var showall = false

    private var guiToOpen: GuiScreen? = null

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == TickEvent.Phase.START) return

        if(guiToOpen != null) {
            Utils.mc.displayGuiScreen(guiToOpen)
            guiToOpen = null
        }
    }

    fun displayScreen(gui: GuiScreen?) {
        if (gui == null) return

        guiToOpen = gui
    }

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (Utils.getCurrentScreen() is GuiEditor) return
            val scaledResolution = Utils.getScaledResolution()
            val screenWidth = scaledResolution.scaledWidth
            val screenHeight = scaledResolution.scaledHeight

            for (guiElement in guiElements) {
                if (guiElement.isActive()) {
                    if (!showall && !guiElement.isVisible()) continue

                    val x = guiElement.relativeX * screenWidth
                    val y = guiElement.relativeY * screenHeight

                    GlStateManager.pushMatrix()
                    GlStateManager.translate(x, y, 0.0)
                    GlStateManager.scale(guiElement.scale, guiElement.scale, 1.0)
                    guiElement.draw()
                    GlStateManager.popMatrix()
                }
            }
        }
    }

    // Stop the ESC key from closing config when keybind listening
    @SubscribeEvent
    fun onGuiKeyEvent(event: GuiScreenEvent.KeyboardInputEvent.Pre) {
        if (ConfigGui.listeningForKeybind && Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            event.setCanceled(true)
        }
    }

    fun saveGuiElements() {
        val configFile = guiConfigFilePath

        try {
            FileWriter(configFile).use { writer ->
                gson.toJson(guiElements, writer)
            }
            if (CustomizationConfig.developerMode) {
                println("Config saved successfully.")
            }
        } catch (e: Exception) {
            println("Error saving config: ${e.message}")
        }
    }

    fun loadGuiElements() {
        val configFile = guiConfigFilePath

        if (configFile.exists()) {
            try {
                FileReader(configFile).use { reader ->
                    val loadedElements = gson.fromJson(reader, Array<Element>::class.java)
                    for (loadedElement in loadedElements) {
                        val new = guiElements.find { it.elementName == loadedElement.elementName }
                        if (new != null) {
                            new.relativeX = loadedElement.relativeX
                            new.relativeY = loadedElement.relativeY
                            new.scale = loadedElement.scale
                        }
                    }
                    println("Config loaded successfully.")
                }
            } catch (e: Exception) {
                println("Error loading config: ${e.message}")
            }
        } else {
            println("Config file not found. Using default configuration.")
        }
    }

    open class Element {
        var scale = 1.0
        var relativeX: Double = 0.0
        var relativeY: Double = 0.0
        var needsExample = false
        var height = 1
        var width = 1
        var elementName = "Element Name"

        // Returns the name for the popup, aswell as what to config search for when right clicking on it
        fun addToList() {
            guiElements.add(this)
        }

        open fun draw() {}

        // Will be called during gui editing so the user can see what the feature is even if its not explicitly showing right then
        open fun drawExample() {}

        // This will usually consist of locational requirements, example only showing inside crimson isles
        open fun isVisible(): Boolean = true

        // This will tell whether the config option for this has been turned on or not
        open fun isActive(): Boolean = false
    }
}