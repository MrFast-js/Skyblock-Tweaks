package mrfast.sbt.apis

import mrfast.sbt.config.categories.DeveloperConfig.showItemAbilities
import mrfast.sbt.customevents.UseItemAbilityEvent
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.utils.*
import mrfast.sbt.utils.ItemUtils.getLore
import mrfast.sbt.utils.ItemUtils.getSkyblockId
import mrfast.sbt.utils.Utils.clean
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.MouseEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.Event
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.floor


object ItemAbilities {
    // Stored values of each item and its default cooldown
    private var itemCooldowns = HashMap<String, CooldownItem?>()

    // currently stored and updated cooldowns
    private var activeCooldowns = HashMap<String, Double>()
    private var justUsedAbility: ItemAbility? = null
    private fun sendItemAbilityEvent(ability: ItemAbility?, event: Event) {
        if (ability != null) {
            if (MinecraftForge.EVENT_BUS.post(UseItemAbilityEvent(ability))) {
                // cancel the item use ability
                event.setCanceled(true)
                return
            }
            if (showItemAbilities) {
                ChatUtils.sendClientMessage(ability.itemId + " " + ability.abilityName)
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
        var usedAt: Long
        var abilityName: String = "Unknown"
        var type: String? = null

        init {
            usedAt = System.currentTimeMillis()
        }

        fun reset() {
            if (cooldownSeconds - currentCount <= 0) {
                currentCount = 0.0
            }
            usedAt = System.currentTimeMillis()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationUtils.inSkyblock || Utils.mc.theWorld == null) return

        for (cooldown in activeCooldowns) {
            // Does the count down
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
        if (!LocationUtils.inSkyblock || Utils.mc.theWorld == null) return

        val skyblockId: String? = item?.getSkyblockId()

        if (skyblockId == null || itemCooldowns.containsKey(skyblockId)) return

        val cdItem = CooldownItem()
        var nextAbilityName: String
        var nextCooldownSeconds: Int
        for (line in item.getLore()) {
            val clean = line.clean()
            if (clean.contains("Ability: ")) {
                nextAbilityName =
                    clean.split(": ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split("  ".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray()[0]
                val ability = ItemAbility(skyblockId)
                ability.abilityName = nextAbilityName

                if (clean.endsWith("RIGHT CLICK")) {
                    cdItem.rightClick = ability
                } else if (clean.endsWith("LEFT CLICK")) {
                    cdItem.leftClick = ability
                } else if (clean.endsWith("SNEAK RIGHT CLICK")) {
                    cdItem.sneakRightClick = ability
                } else if (clean.endsWith("SNEAK LEFT CLICK")) {
                    cdItem.sneakLeftClick = ability
                }
            }
            if (clean.contains("Cooldown: ")) {
                nextCooldownSeconds = clean.replace("[^0-9]".toRegex(), "").toInt()
                if (cdItem.rightClick != null) {
                    cdItem.rightClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
                }
                if (cdItem.leftClick != null) {
                    cdItem.leftClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
                }
                if (cdItem.sneakRightClick != null) {
                    cdItem.sneakRightClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
                }
                if (cdItem.sneakLeftClick != null) {
                    cdItem.sneakLeftClick!!.cooldownSeconds = nextCooldownSeconds.toDouble()
                }
            }
        }
        if (cdItem.rightClick != null || cdItem.leftClick != null || cdItem.sneakRightClick != null || cdItem.sneakLeftClick != null) {
            itemCooldowns[skyblockId] = cdItem
        }
    }

    private fun getCooldownReduction(): Int {
        for (sidebarLine in ScoreboardUtils.getSidebarLines(true)) {
            if (sidebarLine.contains(Utils.mc.thePlayer.name)) {
                try {
                    val mageLvl = sidebarLine.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()[2].replace("[^0-9]".toRegex(), "").toInt()
                    return floor(mageLvl.toDouble() / 2).toInt()
                } catch (ignored: Exception) {
                }
            }
        }
        return 0
    }

    private val isMage = false
    private val isUniqueDungeonClass = false

    /*
   Handle left click events differently as they just don't work like normal and more commonly used right click abilities
    */
    @SubscribeEvent
    fun onMouseClick(event: MouseEvent) {
        if (!LocationUtils.inSkyblock || Utils.mc.theWorld == null) return

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
        if (!LocationUtils.inSkyblock || Utils.mc.theWorld == null) return
        val heldItem: ItemStack = ItemUtils.getHeldItem() ?: return
        val skyblockId: String? = heldItem.getSkyblockId()

        if (skyblockId == null || !itemCooldowns.containsKey(skyblockId)) return
        val cdItem = itemCooldowns[skyblockId]

        val sneaking: Boolean = Utils.mc.thePlayer.isSneaking
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {

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

        if (clean.startsWith("Used") && LocationUtils.inDungeons) {
            justUsedAbility = ItemAbility("Dungeon_Ability")
        }

        justUsedAbility?.let { ability ->
            val heldItem = ItemUtils.getHeldItem() ?: return
            val skyblockId = heldItem.getSkyblockId()

            if (ability.itemId != skyblockId) return

            if (clean.startsWith("This ability is on cooldown for")) {
                if (System.currentTimeMillis() - ability.usedAt > 300) return

                val currentCooldown = clean.replace("[^0-9]".toRegex(), "").toInt()
                ability.currentCount = (ability.cooldownSeconds - currentCooldown).toDouble()
                activeCooldowns[ability.abilityName] = currentCooldown.toDouble()
            }
        }
    }

    private fun updateCooldown(cooldownCount: Double): Double {
        var secondsToAdd = 0.05
        if (LocationUtils.inDungeons && cooldownReduction == -1 && isMage) {
            cooldownReduction = getCooldownReduction()
            if (isUniqueDungeonClass) {
                cooldownReduction += 25
            }
            cooldownReduction += 25
        }
        if (cooldownReduction != -1) {
            secondsToAdd *= (100.0 + cooldownReduction) / cooldownReduction
        }
        return cooldownCount - secondsToAdd
    }


}
