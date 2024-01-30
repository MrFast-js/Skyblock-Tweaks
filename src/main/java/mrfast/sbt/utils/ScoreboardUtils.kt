package mrfast.sbt.utils

import mrfast.sbt.utils.Utils.clean
import net.minecraft.client.Minecraft
import net.minecraft.scoreboard.Score
import net.minecraft.scoreboard.ScorePlayerTeam


object ScoreboardUtils {

    fun getScoreboardLines(cleanColor: Boolean): List<String> {
        val scoreboard = Minecraft.getMinecraft().theWorld?.scoreboard
        val objective = scoreboard?.getObjectiveInDisplaySlot(1)

        return scoreboard?.let {
            it.getSortedScores(objective)
                    .map { score: Score ->
                        val out = score.playerName?.let { playerName ->
                            stripAlienCharacters(ScorePlayerTeam.formatPlayerName(it.getPlayersTeam(playerName), playerName))
                        } ?: ""

                        if (cleanColor) out.clean()
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
}