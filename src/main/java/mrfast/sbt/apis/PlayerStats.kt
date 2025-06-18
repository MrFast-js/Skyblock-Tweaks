package mrfast.sbt.apis

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.config.categories.DeveloperConfig
import mrfast.sbt.config.categories.GeneralConfig
import mrfast.sbt.customevents.WorldLoadEvent
import mrfast.sbt.managers.LocationManager
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraft.util.ChatComponentText
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs
import kotlin.math.max

@SkyblockTweaks.EventComponent
object PlayerStats {
    private var HEALTH_REGEX = """(§.)(?<currentHealth>[\d,]+)\/(?<maxHealth>[\d,]+)❤""".toRegex()
    var health = 0
    var maxHealth = 0
    var absorption = 0

    private var MANA_REGEX = """§b(?<currentMana>[\d,]+)\/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()
    private var OVERFLOW_REGEX = """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()
    var mana = 0
    var maxMana = 0
    var overflowMana = 0

    private var DEFENSE_REGEX = """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()
    var defense = 0
    var effectiveHealth = 0
    var maxEffectiveHealth = 0

    private var RIFT_REGEX = """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()
    var maxRiftTime = 0
    var riftTimeSeconds = 0

    private var DRILL_FUEL_REGEX = """§2(?<currentFuel>[\d,]+)\/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()
    var drillFuel = 0
    var maxDrillFuel = 0

    private var DUNGEON_SECRETS_REGEX = """§7(?<secrets>[\d,]+)\/(?<maxSecrets>[\d,]+) Secrets§r""".toRegex()
    var currentRoomSecrets = 0
    var currentRoomMaxSecrets = 0

    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        maxRiftTime = 0
        currentRoomSecrets = -1
        currentRoomMaxSecrets = 0
    }

    var displayedHealth = health.toFloat()
    var displayedMana = mana.toFloat()

    private var lastHealth = health.toFloat()
    private var lastMana = mana.toFloat()

    private var healthRegenPerInterval = 0f
    private var manaRegenPerInterval = 0f

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationManager.inSkyblock || !Utils.isWorldLoaded()) return

        if(!GeneralConfig.interpolateStats) {
            displayedMana = mana.toFloat()
            displayedHealth = health.toFloat()
            return
        }

        // Default when world loads
        if(health >= maxHealth || health == (20 + absorption)) {
            lastHealth = maxHealth.toFloat()
            health = maxHealth
            displayedHealth = maxHealth.toFloat()
        }

        if(mana == maxMana) {
            lastMana = mana.toFloat()
            mana = maxMana
            displayedMana = maxMana.toFloat()
        }

        // if displayed is far from actual, reset it
        if(abs(displayedMana-mana) > 50) {
            lastMana = mana.toFloat()
            displayedMana = mana.toFloat()
        }
        if(abs(displayedHealth-health) > 50) {
            lastHealth = health.toFloat()
            displayedHealth = health.toFloat()
        }

        // Health update logic
        if (health.toFloat() < lastHealth) {
            // Health dropped — reset regen tracking
            displayedHealth = health.toFloat()
        } else if (health.toFloat() > lastHealth) {
            // Health went up — calculate how much and update regen rate
            val regenAmount = health - lastHealth
            healthRegenPerInterval = regenAmount
        }
        lastHealth = health.toFloat()

        // Mana update logic
        if (mana.toFloat() < lastMana) {
            // Mana dropped — reset regen tracking
            displayedMana = mana.toFloat()
        } else if (mana.toFloat() > lastMana) {
            // Mana went up — calculate how much and update regen rate
            val regenAmount = mana - lastMana
            manaRegenPerInterval = regenAmount
        }
        lastMana = mana.toFloat()

        // Predict current health based on regen rate and elapsed time
        displayedHealth += healthRegenPerInterval / 20f
        if(displayedHealth > maxHealth) {
            displayedHealth = maxHealth.toFloat() // Clamp to max health
        }

        // Predict current mana based on regen rate and elapsed time
        displayedMana += manaRegenPerInterval / 20f
        if (displayedMana > maxMana) {
            displayedMana = maxMana.toFloat() // Clamp to max mana
        }
    }



    // Animate value counting


    @SubscribeEvent
    fun onEvent(event: ClientChatReceivedEvent) {
        if (!LocationManager.inSkyblock) return
        if (event.type.toInt() == 2) {
            var actionBar: String = event.message.formattedText

            extractPlayerStats(actionBar)

            if (GeneralConfig.cleanerActionBar) {
                actionBar = HEALTH_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideHealthFromBar) "" else it.value
                }

                actionBar = DEFENSE_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideDefenseFromBar) "" else it.value
                }

                actionBar = DUNGEON_SECRETS_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideSecretsFromBar) "" else it.value
                }

                actionBar = MANA_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideManaFromBar) "" else it.value
                }

                actionBar = OVERFLOW_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideOverflowManaFromBar) "" else it.value
                }

                actionBar = RIFT_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideRiftTimeFromBar) "" else it.value
                }

                actionBar = DRILL_FUEL_REGEX.replace(actionBar) {
                    if (GeneralConfig.hideDrillFuel) "" else it.value
                }

                event.message = ChatComponentText(actionBar.trim().replace("§r  ", " "))
            }
        }
    }

    private fun extractPlayerStats(filledActionBar: String) {
        val actionBar = filledActionBar.replace(",", "").replace("k", "000")


        if (DeveloperConfig.logActionBar) {
            println(actionBar)
        }

        if (actionBar.matches(HEALTH_REGEX)) {
            val groups = actionBar.getRegexGroups(HEALTH_REGEX) ?: return
            health = groups["currentHealth"]!!.value.toInt()
            maxHealth = groups["maxHealth"]!!.value.toInt()
            effectiveHealth = (health * (1 + defense / 100))
            maxEffectiveHealth = (maxHealth * (1 + defense / 100))
            absorption = max(health - maxHealth, 0)
        }

        if (actionBar.matches(DRILL_FUEL_REGEX)) {
            val groups = actionBar.getRegexGroups(DRILL_FUEL_REGEX) ?: return
            drillFuel = groups["currentFuel"]!!.value.toInt()
            maxDrillFuel = groups["maxFuel"]!!.value.toInt()
        }

        if (actionBar.matches(DUNGEON_SECRETS_REGEX)) {
            val groups = actionBar.getRegexGroups(DUNGEON_SECRETS_REGEX) ?: return
            currentRoomSecrets = groups["secrets"]!!.value.toInt()
            currentRoomMaxSecrets = groups["maxSecrets"]!!.value.toInt()
        }

        if (actionBar.matches(MANA_REGEX)) {
            val groups = actionBar.getRegexGroups(MANA_REGEX) ?: return
            mana = groups["currentMana"]!!.value.toInt()
            maxMana = groups["maxMana"]!!.value.toInt()
        }

        if (actionBar.matches(OVERFLOW_REGEX)) {
            val groups = actionBar.getRegexGroups(OVERFLOW_REGEX) ?: return
            overflowMana = groups["overflowMana"]!!.value.toInt()
        }

        if (actionBar.matches(DEFENSE_REGEX)) {
            val groups = actionBar.getRegexGroups(DEFENSE_REGEX) ?: return
            defense = groups["defense"]!!.value.toInt()
        }

        if (actionBar.matches(RIFT_REGEX)) {
            val groups = actionBar.getRegexGroups(RIFT_REGEX) ?: return
            val minutes = groups["minutes"]?.value?.toInt() ?: 0
            val seconds = groups["seconds"]!!.value.toInt()

            riftTimeSeconds = minutes * 60 + seconds
            if (riftTimeSeconds > maxRiftTime) maxRiftTime = riftTimeSeconds
        }
    }
}
