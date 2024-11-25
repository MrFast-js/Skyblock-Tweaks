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
import kotlin.math.max

@SkyblockTweaks.EventComponent
object PlayerStats {
    var HEALTH_REGEX = """§c(?<currentHealth>[\d,]+)\/(?<maxHealth>[\d,]+)❤""".toRegex()
    var health = 0
    var maxHealth = 0
    var absorption = 0

    var MANA_REGEX = """§b(?<currentMana>[\d,]+)\/(?<maxMana>[\d,]+)✎( Mana)?""".toRegex()
    var OVERFLOW_REGEX = """§3(?<overflowMana>[\d,]+)ʬ""".toRegex()
    var mana = 0
    var maxMana = 0
    var overflowMana = 0

    var DEFENSE_REGEX = """§a(?<defense>[\d,]+)§a❈ Defense""".toRegex()
    var defense = 0
    var effectiveHealth = 0
    var maxEffectiveHealth = 0

    var RIFT_REGEX = """(§7|§a)(?:(?<minutes>\d+)m\s*)?(?<seconds>\d+)sф Left""".toRegex()
    var maxRiftTime = 0
    var riftTimeSeconds = 0

    var DRILL_FUEL_REGEX = """§2(?<currentFuel>[\d,]+)\/(?<maxFuel>[\d,k]+) Drill Fuel""".toRegex()
    var drillFuel = 0
    var maxDrillFuel = 0

    var DUNGEON_SECRETS_REGEX = """§7(?<secrets>[\d,]+)\/(?<maxSecrets>[\d,]+) Secrets§r""".toRegex()
    var currentRoomSecrets = 0
    var currentRoomMaxSecrets = 0

    @SubscribeEvent
    fun onWorldChange(event: WorldLoadEvent) {
        maxRiftTime = 0
        currentRoomSecrets = -1
        currentRoomMaxSecrets = 0
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START) return
        if (!LocationManager.inSkyblock) return

        if (LocationManager.currentIsland == "The Rift") {
            health = Utils.mc.thePlayer.health.toInt()
            maxHealth = Utils.mc.thePlayer.maxHealth.toInt()
        }
    }

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