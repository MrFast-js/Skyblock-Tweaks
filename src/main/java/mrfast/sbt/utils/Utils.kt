package mrfast.sbt.utils

import net.minecraft.client.Minecraft

object Utils {
    val mc: Minecraft = Minecraft.getMinecraft()

    fun setTimeout(runnable: () -> Unit, delayMillis: Long) {
        Thread {
            Thread.sleep(delayMillis)
            runnable()
        }.start()
    }

    fun cleanColor(text: String): String {
        return text.replace("§[0-9a-fA-F]".toRegex(),"")
    }

    fun formatNumber(number: Int): String {
        return String.format("%,d", number)
    }
}