package mrfast.sbt.utils

import mrfast.sbt.utils.Utils.clean
import net.minecraft.client.Minecraft
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam
import java.util.*
import kotlin.collections.ArrayList


object ScoreboardUtils {
    fun getSidebarLines(cleanColor: Boolean): List<String> {
        val scoreboard = Minecraft.getMinecraft().theWorld?.scoreboard
        val objective = scoreboard?.getObjectiveInDisplaySlot(1)

        return scoreboard?.let {
            it.getSortedScores(objective)
                .map { score: Score ->
                    var out = score.playerName?.let { playerName ->
                        stripAlienCharacters(
                            ScorePlayerTeam.formatPlayerName(
                                it.getPlayersTeam(playerName),
                                playerName
                            )
                        )
                    } ?: ""

                    if (cleanColor) out = out.clean()
                    out
                }
                .reversed()
        } ?: emptyList()
    }

    /**
     * This code is unmodified
     * @Author: nea98
     * @Source: https://moddev.nea.moe
     **/
    private fun stripAlienCharacters(text: String): String {
        val sb = StringBuilder()
        for (c in text.toCharArray()) {
            if (Minecraft.getMinecraft().fontRendererObj.getCharWidth(c) > 0 || c == '§') sb.append(c)
        }
        return sb.toString()
    }

    fun getTabListEntries(): List<String> {
        if (Minecraft.getMinecraft().thePlayer == null) return listOf()

        val playerInfoList = Minecraft.getMinecraft().thePlayer.sendQueue.playerInfoMap
        val playerNames = ArrayList<String>()

        if (playerInfoList != null) {
            for (playerInfo in playerInfoList) {
                playerNames.add(playerInfo.displayName?.unformattedText ?: playerInfo.gameProfile.name)
            }
        }

        return playerNames
    }
}