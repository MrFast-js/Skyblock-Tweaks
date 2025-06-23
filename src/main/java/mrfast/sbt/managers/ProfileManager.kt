package mrfast.sbt.managers

import mrfast.sbt.SkyblockTweaks
import mrfast.sbt.customevents.ProfileLoadEvent
import mrfast.sbt.utils.ChatUtils
import mrfast.sbt.utils.ScoreboardUtils
import mrfast.sbt.utils.Utils
import mrfast.sbt.utils.Utils.clean
import mrfast.sbt.utils.Utils.getRegexGroups
import mrfast.sbt.utils.Utils.matches
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

@SkyblockTweaks.EventComponent
object ProfileManager {
    var profileIds = mutableMapOf<String, String>() // Player-ID, Profile-ID
    private var listenForProfileId = false

    fun sendProfileIdCommand() {
        listenForProfileId = true
        ChatUtils.sendPlayerMessage("/profileid")
    }

    var profileLoaded = false
    var onIronman = false

    fun getCurrentProfileId(): String? {
        return profileIds[Utils.getPlayer()!!.uniqueID.toString()]
    }

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (event.type.toInt() == 2) return

        val clean = event.message.unformattedText.clean()
        val profileIdPattern = """Profile ID: (\S+)""".toRegex()
        val suggestPattern = """CLICK THIS TO SUGGEST IT IN CHAT .*""".toRegex()
        val firstJoinPattern = """Latest update: SkyBlock .*""".toRegex() // First startup, get profile id
        val switchPattern = """Your profile was changed to: """.toRegex()

        if (clean.matches(firstJoinPattern) || clean.matches(switchPattern)) {
            sendProfileIdCommand()
            onIronman = ScoreboardUtils.getSidebarLines(true).any { it.clean().contains("♲ Ironman") }
        }

        if (clean.matches(profileIdPattern)) {
            val newProfileId = clean.getRegexGroups(profileIdPattern)?.get(1)?.value ?: return
            val currentProfile = profileIds[Utils.getPlayer()!!.uniqueID.toString()]

            if (currentProfile == null || currentProfile != newProfileId || !profileLoaded) {
                profileLoaded = true
                MinecraftForge.EVENT_BUS.post(ProfileLoadEvent())
            }

            profileIds[Utils.getPlayer()!!.uniqueID.toString()] = newProfileId

            DataManager.saveData("selectedProfileIds", profileIds)

            if (listenForProfileId) event.isCanceled = true
        }

        if (clean.matches(suggestPattern) && listenForProfileId) {
            event.isCanceled = true
            if (clean.contains("[NO DASHES]")) {
                listenForProfileId = false
            }
        }
    }
}