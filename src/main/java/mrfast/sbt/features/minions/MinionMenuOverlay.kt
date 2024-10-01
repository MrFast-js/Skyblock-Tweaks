package mrfast.sbt.features.minions

import com.google.gson.JsonObject
import com.mojang.realmsclient.gui.ChatFormatting
import gg.essential.elementa.dsl.constraint
import gg.essential.universal.UMatrixStack
import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.CustomizationConfig
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.config.categories.MiscellaneousConfig
import mrfast.sbt.config.components.OutlinedRoundedRectangle
import mrfast.sbt.customevents.GuiContainerBackgroundDrawnEvent
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.customevents.SlotClickedEvent
import mrfast.sbt.managers.DataManager
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.OverlayManager
import mrfast.sbt.utils.GuiUtils
import mrfast.sbt.utils.GuiUtils.chestName
import mrfast.sbt.utils.ItemUtils.getItemBasePrice
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.RenderUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.cleanColor
import mrfast.sbt.utils.Utils.formatNumber
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.getStringWidth
import mrfast.sbt.utils.Utils.matches
import mrfast.sbt.utils.Utils.toDateTimestamp
import mrfast.sbt.utils.Utils.toFormattedDuration
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.item.Item
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@SkyblockTweaks.EventComponent
object MinionMenuOverlay {
    private var minions = JsonObject()
    private var minionSlots = mutableListOf(
        21, 22, 23, 24, 25,
        30, 31, 32, 33, 34,
        39, 40, 41, 42, 43
    )
    private val MINION_REGEX = """.* Minion [IXV]{1,4}$""".toRegex()
    private val TIME_REMAINING_REGEX = """Time Remaining: (.*)""".toRegex()

    @SubscribeEvent
    fun onProfileSwap(event: ProfileLoadEvent?) {
        minions = DataManager.getProfileDataDefault("minions", JsonObject()) as JsonObject
    }

    var closestMinion: Entity? = null
    var openedMinionValue = 0.0
    var openedMinionCoinsPerHour = 0.0
    var openedMinionTitle = ""
    var fuelDuration = ""
    var fuelDurationDate = 0L

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        if (event.message.unformattedText.startsWith("You picked up a minion! You currently have")) {
            if (minions.has(closestMinion?.position.toString())) {
                minions.remove(closestMinion?.position.toString())
            }
        }
    }

    @SubscribeEvent
    fun onContainerDrawn(event: GuiContainerBackgroundDrawnEvent) {
        if(!MiscellaneousConfig.minionOverlay || event.gui !is GuiChest) return
        if (!(event.gui as GuiContainer).chestName().matches(MINION_REGEX)) return

        openedMinionTitle = (event.gui as GuiContainer).chestName().cleanColor()

        var minionHeldValue = 0.0
        minionSlots.forEach {
            val slot = (event.gui as GuiChest).inventorySlots.getSlot(it)
            if (slot.hasStack) {
                if (slot.stack.getSkyblockId() != null) {
                    minionHeldValue += slot.stack.getItemBasePrice() * slot.stack.stackSize
                }
            }
        }
        openedMinionValue = minionHeldValue
        val fuelStack = (event.gui as GuiChest).inventorySlots.getSlot(19).stack ?: return
        if (fuelStack.getSkyblockId() != null) {
            var fuelRunsOut = "Unlimited"
            fuelDurationDate = -1L
            for (line in fuelStack.getLore(true)) {
                if (line.matches(TIME_REMAINING_REGEX)) {
                    fuelRunsOut = line.getRegexGroups(TIME_REMAINING_REGEX)!![1]!!.value

                    val parsedNumber = line.split(" ")[2].toLong()
                    val unitNumber = when {
                        line.contains("days") -> parsedNumber * 1000 * 60 * 60 * 24
                        line.contains("hours") -> parsedNumber * 1000 * 60 * 60
                        line.contains("minutes") -> parsedNumber * 1000 * 60
                        else -> parsedNumber * 1000
                    }
                    fuelDurationDate = System.currentTimeMillis() + unitNumber
                }
            }
            fuelDuration = fuelRunsOut
        }

        if (minions.has(closestMinion?.position.toString())) {
            val minionObj = minions[closestMinion?.position.toString()].getAsJsonObject()
            val lastCollectedAt = minionObj["lastCollectedAt"].asLong
            val timeDifference = System.currentTimeMillis() - lastCollectedAt
            val hoursDifference = timeDifference / 1000.0 / 3600.0
            openedMinionCoinsPerHour = (openedMinionValue / hoursDifference)
        }
    }

    @SubscribeEvent
    fun onSlotClick(event: SlotClickedEvent) {
        if(!MiscellaneousConfig.minionOverlay) return

        val chestName = event.gui.chestName()
        if (chestName.matches(MINION_REGEX) && event.slot.hasStack && closestMinion != null) {
            val nameOfItem = event.slot.stack.displayName.cleanColor()
            if (nameOfItem.startsWith("Collect All") || minionSlots.contains(event.slot.slotNumber)) {
                Utils.setTimeout({
                    if (minions.has(closestMinion!!.position.toString())) {
                        minions.get(closestMinion!!.position.toString()).getAsJsonObject().addProperty("lastCollectedAt", System.currentTimeMillis())

                        DataManager.saveProfileData("minions", minions)
                    }
                }, 400)
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if(!MiscellaneousConfig.minionOverlay) return

        if (LocationManager.inSkyblock && LocationManager.currentArea == "Your Island") {
            for (e in Utils.mc.theWorld.loadedEntityList) {
                if (e !is EntityArmorStand || !isMinion(e)) continue

                if (DataManager.profileLoaded && !minions.has(e.position.toString())) {
                    val minion = JsonObject()
                    minion.addProperty("lastCollectedAt", System.currentTimeMillis())
                    minions.add(e.position.toString(), minion)
                    DataManager.saveProfileData("minions", minions)
                }

                if (CustomizationConfig.developerMode && DeveloperConfig.showMinionDebug) {
                    RenderUtils.drawSpecialBB(e.position, Color.GREEN, event.partialTicks)
                }

                if (MiscellaneousConfig.lastCollectedAboveMinion && minions.has(e.position.toString()) && Utils.mc.thePlayer.getDistanceToEntity(e) < 8) {
                    val minion = minions[e.position.toString()].asJsonObject
                    val timeElapsed = (System.currentTimeMillis() - minion["lastCollectedAt"].asLong)
                    val duration = timeElapsed.toFormattedDuration()

                    RenderUtils.draw3DString(
                        ChatFormatting.YELLOW.toString() + "Last Collected: " + ChatFormatting.AQUA + duration,
                        e.getPositionVector().add(Vec3(0.0, 1.5, 0.0)),
                        event.partialTicks
                    )
                }

                if (CustomizationConfig.developerMode && DeveloperConfig.showMinionDebug && e == closestMinion) {
                    RenderUtils.draw3DString("§cCLOSEST MINION | ${closestMinion!!.position}",
                        closestMinion!!.positionVector.add(Vec3(0.0, 2.0, 0.0)),
                        event.partialTicks
                    )
                    RenderUtils.drawSpecialBB(closestMinion!!.position, Color.RED, event.partialTicks)
                }

                if (closestMinion == null) {
                    closestMinion = e
                    continue
                }
                if (Utils.mc.thePlayer.getDistanceToEntity(e) < Utils.mc.thePlayer.getDistanceToEntity(closestMinion)) {
                    closestMinion = e
                }
            }
        }
    }

    private fun isMinion(e: EntityArmorStand): Boolean {
        for (i in 0..3) {
            if (e.getCurrentArmor(i) == null) return false
        }
        return Item.getIdFromItem(e.getCurrentArmor(0).item) == 301 &&
                Item.getIdFromItem(e.getCurrentArmor(1).item) == 300 &&
                Item.getIdFromItem(e.getCurrentArmor(2).item) == 299 &&
                Item.getIdFromItem(e.getCurrentArmor(3).item) == 397
    }

    init {
        MinionOverlay()
    }

    class MinionOverlay : OverlayManager.Overlay() {
        init {
            this.x = 0.0
            this.y = 0.0
            this.leftAlign = false
            this.addToList(OverlayManager.OverlayType.CHEST)
        }

        override fun draw(mouseX: Int, mouseY: Int, event: Event) {
            val minionObj = minions[closestMinion?.position.toString()].getAsJsonObject()
            val lastCollectedAt = minionObj["lastCollectedAt"].asLong
            val lastCollectedDuration = System.currentTimeMillis() - lastCollectedAt

            val lines = mutableListOf(
                GuiUtils.Element(5f, 5f, "§c${openedMinionTitle}", null, null),
                GuiUtils.Element(
                    5f,
                    15f,
                    "§a • Fuel Duration: $fuelDuration",
                    listOf(if(fuelDurationDate>0) "§9Runs out on ${fuelDurationDate.toDateTimestamp()}" else "§9Fuel will never run out."),
                    null
                ),
                GuiUtils.Element(
                    5f,
                    27f,
                    "§b • Last Collected: ${(lastCollectedDuration).toFormattedDuration(true)}",
                    listOf("§eCollected on ${lastCollectedAt.toDateTimestamp()}"),
                    null
                ),
                GuiUtils.Element(
                    5f,
                    39f,
                    "§3 • Value Over Time*",
                    listOf("§9Calculates the value based on held", "§9value and the time since last collected."),
                    null
                ),
                GuiUtils.Element(
                    5f,
                    49f,
                    "  §e- Value Per Hour: ${openedMinionCoinsPerHour.formatNumber()}",
                    null,
                    null
                ),
                GuiUtils.Element(5f, 59f, "  §6- Held Value: ${openedMinionValue.formatNumber()}", null, null),
            )

            val width =
                (lines.sortedBy { it.text.getStringWidth() }[lines.size - 1].text.getStringWidth() + 15).coerceIn(
                    100, 150
                )

            val chestEvent = (event as GuiContainerBackgroundDrawnEvent)
            // Change z-depth in order to be above NEU inventory buttons
            GlStateManager.translate(0f, 0f, 52f)
            OutlinedRoundedRectangle.drawOutlinedRoundedRectangle(
                UMatrixStack(),
                2f,
                0f,
                width.toFloat(),
                (10 + 11 * lines.size).toFloat(),
                4f,
                Color(18, 18, 18),
                GuiUtils.rainbowColor.get().constraint,
                2f
            )

            for (segment in lines) {
                segment.draw(mouseX, mouseY, (x + chestEvent.guiLeft + 180).toInt(), (y + chestEvent.guiTop).toInt())
            }
            GlStateManager.translate(0f, 0f, -52f)
        }

        override fun isActive(event: Event): Boolean {
            if (event !is GuiContainerBackgroundDrawnEvent) return false

            return (event.gui as GuiContainer).chestName().matches(MINION_REGEX) && MiscellaneousConfig.minionOverlay
        }
    }
}