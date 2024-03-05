package mrfast.sbt.features.general

import com.google.gson.JsonObject
import mrfast.sbt.apis.ItemApi
import mrfast.sbt.config.GuiManager
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SlotDrawnEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getExtraAttributes
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.LocationUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.formatNumber
import net.minecraft.init.Items
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor
import kotlin.math.max

object QuiverOverlay {
    private var selectedArrowId = ""
    private var arrowCounts = JsonObject()
    private var loadedData = false
    private var readArrowFromSwapper = false
    private var sentLowArrows = false

    @SubscribeEvent
    fun onProfileSwap(event: ProfileLoadEvent) {
        arrowCounts = DataManager.getProfileDataDefault("arrows", JsonObject()) as JsonObject
        selectedArrowId = DataManager.getProfileDataDefault("selectedArrowType", "ARROW") as String

        loadedData = true
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        readArrowFromSwapper = false
        sentLowArrows = false
    }

    @SubscribeEvent
    fun onSlotDraw(event: SlotDrawnEvent.Post) {
        if (!MiscellaneousConfig.quiverOverlay) return

        val stack = event.slot.stack
        if (stack != null && stack.item != null && stack.getSkyblockId() == "ARROW_SWAPPER" && !readArrowFromSwapper) {
            for (lore in stack.getLore()) {
                if (lore.startsWith("§aSelected: ")) {
                    readArrowFromSwapper = true
                    selectedArrowId = getArrowIdFromName(lore.split("§aSelected: ")[1])
                    DataManager.saveProfileData("selectedArrowType", selectedArrowId)
                    return
                }
            }
        }

        if (event.slot.slotIndex == 0 && event.gui.chestName() == "Quiver") {
            val newArrowCount = JsonObject()
            for (i in 0 until event.gui.inventorySlots.inventorySlots.size) {
                val slotStack = event.gui.inventorySlots.getSlot(i).stack ?: continue

                if (slotStack.item == Items.arrow && slotStack.getSkyblockId()?.contains("ARROW") == true) {
                    val displayName = slotStack.displayName
                    val arrowObj = JsonObject()
                    arrowObj.addProperty("arrowName", displayName)
                    val oldCount = newArrowCount[slotStack.getSkyblockId()]?.asJsonObject?.get("count")?.asDouble ?: 0.0
                    arrowObj.addProperty("count", oldCount + slotStack.stackSize)

                    newArrowCount.add(
                        slotStack.getSkyblockId(),
                        arrowObj
                    )
                }
            }
            arrowCounts = newArrowCount
            DataManager.saveProfileData("arrows", arrowCounts)
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2 || !MiscellaneousConfig.quiverOverlay) return

        val clean = event.message.unformattedText.clean()
        when {
            clean.startsWith("You set your selected arrow type to") -> {
                val arrowName = event.message.formattedText.split("You set your selected arrow type to §r")[1].replace(
                    "§r§a!§r",
                    ""
                )
                selectedArrowId = getArrowIdFromName(arrowName)

                DataManager.saveProfileData("selectedArrowType", selectedArrowId)
            }

            clean == "Cleared your quiver!" || clean == "Your quiver is empty!" || clean.startsWith("You don't have any more arrows left in your Quiver!") -> {
                arrowCounts = JsonObject()
                DataManager.saveProfileData("arrows", arrowCounts)
            }

            clean.startsWith("You filled your quiver with") -> {
                val arrowCount = clean.replace("[^0-9]".toRegex(), "").toDouble()
                val old = arrowCounts.get("§fFlint Arrow")?.asDouble ?: 0.0
                arrowCounts.addProperty("§fFlint Arrow", old + arrowCount)
                DataManager.saveProfileData("arrows", arrowCounts)
            }

            clean.startsWith("Jax forged") -> {
                val arrowName = event.message.formattedText.split("Jax forged §r")[1].split("§r§8 x")[0]
                val arrowCount = clean.split(" x")[1].split(" ")[0].replace("[^0-9]".toRegex(), "").toDouble()
                val oldCount = arrowCounts.get(arrowName)?.asDouble ?: 0.0
                val arrowObj = JsonObject()
                arrowObj.addProperty("arrowName", arrowName)
                arrowObj.addProperty("count", oldCount + arrowCount)

                arrowCounts.add(
                    getArrowIdFromName(arrowName),
                    arrowObj
                )
                DataManager.saveProfileData("arrows", arrowCounts)
            }
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Received) {
        if (!MiscellaneousConfig.quiverOverlay || Utils.mc.thePlayer == null || event.packet !is S29PacketSoundEffect) return

        val packet = event.packet
        val heldItem = Utils.mc.thePlayer.heldItem
        if (heldItem != null && heldItem.item is ItemBow && packet.soundName == "random.bow") {
            arrowShot()
        }
    }

    private fun arrowShot() {
        val held = Utils.mc.thePlayer.heldItem ?: return
        if (held.getSkyblockId() == null || !loadedData) return

        var countToRemove = 1.0
        val enchants = held.getExtraAttributes()?.getCompoundTag("enchantments")

        if (enchants != null) {
            if (enchants.hasKey("infinite_quiver")) {
                val level = enchants.getInteger("infinite_quiver")
                countToRemove = (1 - level * 0.03)
            }
        }
        val selectedArrow = arrowCounts[selectedArrowId]?.asJsonObject ?: return
        val currentCount = selectedArrow.get("count")?.asDouble ?: 0.0

        selectedArrow.addProperty(
            "count",
            max(0.0, currentCount - countToRemove)
        )

        val lowArrows = selectedArrow.get("count").asDouble < 128
        if (MiscellaneousConfig.quiverOverlayLowArrowNotification) {
            if (!sentLowArrows && lowArrows) {
                sentLowArrows = true
                ChatUtils.sendClientMessage("§cRefill Quiver")
            }
        }

        DataManager.saveProfileData("arrows", arrowCounts)
    }

    fun getDisplay(): String {
        var quiverArrows = -1.0
        if (arrowCounts.has(selectedArrowId)) {
            quiverArrows = arrowCounts[selectedArrowId].asJsonObject.get("count").asDouble
        }
        quiverArrows = floor(quiverArrows)
        var display = if (quiverArrows > 0) "§r§7x" + quiverArrows.formatNumber() else "§cEmpty Quiver"

        if (quiverArrows != -1.0 && MiscellaneousConfig.quiverOverlayType) {
            val arrowName = arrowCounts[selectedArrowId].asJsonObject.get("arrowName").asString
            display += " $arrowName"
        }
        if (selectedArrowId == "Unknown") {
            display = "§cNo Arrow Selected"
        }
        return display
    }

    fun getArrowIdFromName(name: String): String {
        arrowCounts.entrySet().forEach {
            if (it.value.asJsonObject.get("arrowName").asString == name) {
                return it.key
            }
        }
        return "Unknown"
    }

    init {
        QuiverOverlay()
    }

    class QuiverOverlay : GuiManager.Element() {
        init {
            this.relativeX = 0.371875
            this.relativeY = 0.842593
            this.elementName = "Quiver Overlay"
            this.addToList()
            this.height = 16
        }

        override fun draw() {
            GuiUtils.renderItemStackOnScreen(ItemApi.createItemStack(selectedArrowId), 0f, 0f, 16f, 16f)

            GuiUtils.drawText(getDisplay(), 16f, 3f, GuiUtils.TextStyle.DROP_SHADOW)
            this.width = Utils.mc.fontRendererObj.getStringWidth(getDisplay().clean()) + 17
        }

        override fun isActive(): Boolean {
            return GeneralConfig.healthDisplay && LocationUtils.inSkyblock
        }

        override fun isVisible(): Boolean {
            // Stop from showing if no held bow
            if (MiscellaneousConfig.quiverOverlayOnlyBow) {
                val held: ItemStack = Utils.mc.thePlayer.heldItem ?: return false
                if (held.item !is ItemBow) {
                    return false
                }
            }

            return true
        }
    }
}