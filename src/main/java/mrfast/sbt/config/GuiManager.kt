package mrfast.sbt.config

import com.google.gson.GsonBuilder
import mrfast.sbt.utils.Utils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object GuiManager {
    var guiElements = mutableListOf<Element>()
    private val guiConfigFilePath = "${Minecraft.getMinecraft().mcDataDir}\\config\\skyblocktweaks\\guiConfig.json"
    private val gson = GsonBuilder().setPrettyPrinting().create()
    var showall = false

    @SubscribeEvent
    fun onRender(event: RenderGameOverlayEvent.Post) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if(Utils.mc.currentScreen is GuiEditor) return
            for (guiElement in guiElements) {
                if (guiElement.isActive()) {
                    if (!showall && !guiElement.isVisible()) continue
                    val res = ScaledResolution(Utils.mc)
                    GlStateManager.translate(guiElement.relativeX * res.scaledWidth, guiElement.relativeY * res.scaledHeight, 0.0)
                    GlStateManager.scale(guiElement.scale, guiElement.scale, 1.0)
                    guiElement.draw()
                    GlStateManager.scale(1 / guiElement.scale, 1 / guiElement.scale, 1.0)
                    GlStateManager.translate(-guiElement.relativeX * res.scaledWidth, -guiElement.relativeY * res.scaledHeight, 0.0)
                }
            }
        }
    }

    fun saveGuiElements() {
        val configFile = File(guiConfigFilePath)

        try {
            FileWriter(configFile).use { writer ->
                gson.toJson(guiElements, writer)
            }
            println("Config saved successfully.")
        } catch (e: Exception) {
            println("Error saving config: ${e.message}")
        }
    }

    fun loadGuiElements() {
        val configFile = File(guiConfigFilePath)

        if (configFile.exists()) {
            try {
                FileReader(configFile).use { reader ->
                    val loadedElements = gson.fromJson(reader, Array<Element>::class.java)
                    for (loadedElement in loadedElements) {
                        val new = guiElements.find { it.elementName.equals(loadedElement.elementName) }
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
        var height = 1
        var width = 1
        var elementName = "Element Name"

        // Returns the name for the popup, aswell as what to config search for when right clicking on it
        fun addToList() {
            guiElements.add(this)
        }

        open fun draw() {

        }

        // This will usually consist of locational requirements, example only showing inside crimson isles
        open fun isVisible(): Boolean {
            return true
        }

        // This will tell whether the config option for this has been turned on or not
        open fun isActive(): Boolean {
            return false
        }
    }
}