package mrfast.sbt.customevents

import mrfast.sbt.apis.SkyblockMobDetector
import net.minecraftforge.fml.common.eventhandler.Event

open class SkyblockMobEvent(val sbMob: SkyblockMobDetector.SkyblockMob, val partialTicks: Float?) : Event() {

    class Spawn(sbMob: SkyblockMobDetector.SkyblockMob) : SkyblockMobEvent(sbMob, null) {
        override fun isCancelable(): Boolean {
            return false
        }
    }

    class Death(sbMob: SkyblockMobDetector.SkyblockMob) : SkyblockMobEvent(sbMob, null) {
        override fun isCancelable(): Boolean {
            return false
        }
    }

    class Render(sbMob: SkyblockMobDetector.SkyblockMob, partialTicks: Float) : SkyblockMobEvent(sbMob, partialTicks) {
        override fun isCancelable(): Boolean {
            return false
        }
    }
}