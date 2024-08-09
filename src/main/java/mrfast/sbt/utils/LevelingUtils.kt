package mrfast.sbt.utils

object LevelingUtils {
    private val dungeonsXpPerLevel = intArrayOf(
        0, 50, 75, 110, 160, 230, 330, 470, 670, 950, 1340, 1890, 2665, 3760, 5260, 7380, 10300, 14400,
        20000, 27600, 38000, 52500, 71500, 97000, 132000, 180000, 243000, 328000, 445000, 600000, 800000,
        1065000, 1410000, 1900000, 2500000, 3300000, 4300000, 5600000, 7200000, 9200000, 12000000, 15000000,
        19000000, 24000000, 30000000, 38000000, 48000000, 60000000, 75000000, 93000000, 116250000
    )

    fun calculateDungeonsLevel(xp: Double): Double {
        var xpNeeded = 0
        for (levelIndex in dungeonsXpPerLevel.indices) {
            xpNeeded += dungeonsXpPerLevel[levelIndex]
            if (xp < xpNeeded) {
                val level =
                    (levelIndex - 1) + (xp - (xpNeeded - dungeonsXpPerLevel[levelIndex])) / dungeonsXpPerLevel[levelIndex]
                return level.roundToTwoDecimalPlaces()
            }
        }
        return 50.0 // Default level
    }

    fun Double.roundToTwoDecimalPlaces() = (this * 100).toInt() / 100.0
}