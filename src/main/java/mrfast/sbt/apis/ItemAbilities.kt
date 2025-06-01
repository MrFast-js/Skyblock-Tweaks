package mrfast.sbt.apis

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DeveloperConfig.showItemAbilities
import mrfast.sbt.customevents.PacketEvent
import mrfast.sbt.customevents.ItemAbilityUsedEvent
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.managers.TickManager
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ItemUtils
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.ScoreboardUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.floor


@SkyblockTweaks.EventComponent
object ItemAbilities {
    // Stored values of each item and its default cooldown
    private var itemCooldowns = HashMap<String, CooldownItem?>()

    // currently stored and updated cooldowns
    private var activeCooldowns = HashMap<String, Double>()
    private var justUsedAbility: ItemAbility? = null
    private var stopLastEvent = false

    private fun sendItemAbilityEvent(ability: ItemAbility?, event: Event) {
            if (ability != null) {
                if(ability.manaCost > PlayerStats.mana) {
                    return
                }
                MinecraftForge.EVENT_BUS.post(ItemAbilityUsedEvent(ability))

                if (showItemAbilities) {
                    ChatUtils.sendClientMessage("§7${ability.itemId} §8${ability.abilityName}")
                }

                justUsedAbility = ability
                activeCooldowns[ability.abilityName] = ability.cooldownSeconds
            }
    }

    class CooldownItem {
        var sneakRightClick: ItemAbility? = null
        var sneakLeftClick: ItemAbility? = null
        var rightClick: ItemAbility? = null
        var leftClick: ItemAbility? = null
    }

    class ItemAbility(var itemId: String) {
        var cooldownSeconds = 0.0
        var currentCount = 0.0
        var manaCost = 0
        var usedAt = System.currentTimeMillis()
        var abilityName = "Unknown"
        var type: String? = null
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationManager.inSkyblock || Utils.mc.theWorld == null) return

        if(TickManager.tickCount % 10 != 0) return

        for (cooldown in activeCooldowns) {
            // Does the countdown
            activeCooldowns[cooldown.key] = updateCooldown(cooldown.value)
        }

        activeCooldowns.clear()
        // Update a
        for (i in 0..7) {
            if (Utils.mc.thePlayer.inventory.mainInventory[i] == null) continue
            val stack: ItemStack = Utils.mc.thePlayer.inventory.mainInventory[i]
            setStackCooldown(stack)
            val skyblockId: String? = stack.getSkyblockId()

            // Sets initial cooldowns when world change
            if (skyblockId != null && itemCooldowns[skyblockId] != null) {
                val cdSeconds = itemCooldowns[skyblockId]?.rightClick?.cooldownSeconds ?: 0.0
                val abilityName = itemCooldowns[skyblockId]?.rightClick?.abilityName ?: "Unknown"
                activeCooldowns[abilityName] = cdSeconds / 2.0
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldLoadEvent) {
        activeCooldowns.clear()
        itemCooldowns.clear()
        cooldownReduction = -1
    }

    private var cooldownReduction = -1
    private fun setStackCooldown(item: ItemStack?) {
        if (!LocationManager.inSkyblock || Utils.mc.theWorld == null) return

        val skyblockId: String? = item?.getSkyblockId()

        if (skyblockId == null || itemCooldowns.containsKey(skyblockId)) return

        val cdItem = CooldownItem()
        for (line in item.getLore()) {
            val clean = line.clean()
            if (clean.contains("Ability: ")) {
                setItemAbility(clean, cdItem, skyblockId)
            }
            if (clean.contains("Cooldown: ")) {
                setCooldownSeconds(clean, cdItem)
            }
            if(clean.contains("Mana Cost: ")) {
                setManaCost(clean, cdItem)
            }
        }
        if (cdItem.rightClick != null || cdItem.leftClick != null || cdItem.sneakRightClick != null || cdItem.sneakLeftClick != null) {
            itemCooldowns[skyblockId] = cdItem
        }
    }

    private fun setItemAbility(
        line: String,
        cdItem: CooldownItem,
        skyblockId: String
    ) {
        val nextAbilityName = line.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("  ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        val ability = ItemAbility(skyblockId)
        ability.abilityName = nextAbilityName

        if (line.endsWith("RIGHT CLICK")) {
            cdItem.rightClick = ability
        } else if (line.endsWith("LEFT CLICK")) {
            cdItem.leftClick = ability
        } else if (line.endsWith("SNEAK RIGHT CLICK")) {
            cdItem.sneakRightClick = ability
        } else if (line.endsWith("SNEAK LEFT CLICK")) {
            cdItem.sneakLeftClick = ability
        }
    }

    private fun setCooldownSeconds(clean: String, cdItem: CooldownItem) {
        val nextCooldownSeconds = clean.replace("[^0-9]".toRegex(), "").toInt()
        if (cdItem.rightClick != null) cdItem.rightClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
        if (cdItem.leftClick != null) cdItem.leftClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
        if (cdItem.sneakRightClick != null) cdItem.sneakRightClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
        if (cdItem.sneakLeftClick != null) cdItem.sneakLeftClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
    }

    private fun setManaCost(clean: String, cdItem: CooldownItem) {
        val manaCost = clean.replace("[^0-9]".toRegex(), "").toInt()
        if (cdItem.rightClick != null) cdItem.rightClick!!.manaCost = manaCost
        if (cdItem.leftClick != null) cdItem.leftClick!!.manaCost = manaCost
        if (cdItem.sneakRightClick != null) cdItem.sneakRightClick!!.manaCost = manaCost
        if (cdItem.sneakLeftClick != null) cdItem.sneakLeftClick!!.manaCost = manaCost
    }

    private fun getCooldownReduction(): Int {
        for (sidebarLine in ScoreboardUtils.getSidebarLines(true)) {
            if (sidebarLine.contains(Utils.mc.thePlayer.name)) {
                try {
                    val mageLvl = sidebarLine.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[2].replace("[^0-9]".toRegex(), "").toInt()
                    return floor(mageLvl.toDouble() / 2).toInt()
                } catch (_: Exception) {
                }
            }
        }
        return 0
    }

    /**
     * Handle left click events differently
     * as they just don't work like normal and more commonly used right click abilities
     */
    @SubscribeEvent
    fun onMouseClick(event: MouseEvent) {
        if (!LocationManager.inSkyblock || Utils.mc.theWorld == null) return

        val heldItem: ItemStack = ItemUtils.getHeldItem() ?: return
        val skyblockId: String? = heldItem.getSkyblockId()
        if (skyblockId == null || !itemCooldowns.containsKey(skyblockId)) return

        val cdItem = itemCooldowns[skyblockId]
        val sneaking: Boolean = Utils.mc.thePlayer.isSneaking
        if (event.button == 0 && event.buttonstate) {
            // Left mouse button pressed
            if (cdItem!!.leftClick != null && (cdItem.sneakLeftClick == null || !sneaking)) {
                sendItemAbilityEvent(cdItem.leftClick, event)
            }
            if (cdItem.sneakLeftClick != null && sneaking) {
                sendItemAbilityEvent(cdItem.sneakLeftClick, event)
            }
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (!LocationManager.inSkyblock || Utils.mc.theWorld == null) return

        val heldItem: ItemStack = ItemUtils.getHeldItem() ?: return
        val skyblockId: String? = heldItem.getSkyblockId()
        if (skyblockId == null || !itemCooldowns.containsKey(skyblockId)) return

        val cdItem = itemCooldowns[skyblockId]
        val sneaking: Boolean = Utils.mc.thePlayer.isSneaking
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
println("Mage: ${isMage()} Unique: ${isUniqueDungeonClass()}")
            // Right mouse button pressed
            if (cdItem!!.rightClick != null && (!sneaking || cdItem.sneakRightClick == null)) {
                sendItemAbilityEvent(cdItem.rightClick, event)
            }
            if (cdItem.sneakRightClick != null && sneaking) {
                sendItemAbilityEvent(cdItem.sneakRightClick, event)
            }
        }
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return
        val clean = event.message.unformattedText.clean()

        if (clean.startsWith("Used") && LocationManager.inDungeons) {
            justUsedAbility = ItemAbility("Dungeon_Ability")
        }

        justUsedAbility?.let { ability ->
            val heldItem = ItemUtils.getHeldItem() ?: return
            val skyblockId = heldItem.getSkyblockId()

            if (ability.itemId != skyblockId) return

            if (clean.startsWith("This ability is on cooldown for")) {
                if (System.currentTimeMillis() - ability.usedAt > 300) return

                val currentCooldown = clean.replace("[^0-9]".toRegex(), "").toInt()
                ability.currentCount = (ability.cooldownSeconds - currentCooldown)
                activeCooldowns[ability.abilityName] = currentCooldown.toDouble()
            }
        }
    }


    @SubscribeEvent
    fun onSoundPacket(event: PacketEvent.Received) {
        if (event.packet is S29PacketSoundEffect) {
            val packet = event.packet
            if (packet.soundName == "mob.endermen.portal" && justUsedAbility != null && packet.pitch == 0.0f) {
                stopLastEvent = true
            }
        }
    }

    private fun updateCooldown(cooldownCount: Double): Double {
        var secondsToAdd = 0.05
        if (LocationManager.inDungeons && cooldownReduction == -1 && isMage()) {
            cooldownReduction = getCooldownReduction()
            if (isUniqueDungeonClass()) {
                cooldownReduction += 25
            }
            cooldownReduction += 25
        }
        if (cooldownReduction != -1) {
            secondsToAdd *= (100.0 + cooldownReduction) / cooldownReduction
        }
        return cooldownCount - secondsToAdd
    }

    private fun isMage(): Boolean {
        return ScoreboardUtils.getTabListEntries().any {
            return@any it.clean().contains(Utils.mc.thePlayer.name) && it.clean().contains("Mage")
        }
    }


    private fun isUniqueDungeonClass(): Boolean {
        return ScoreboardUtils.getTabListEntries().filter {
            val args = it.clean().split(" ")
            args.size >= 2 && args[args.size - 2] == "(Mage"
        }.size == 1
    }
}
