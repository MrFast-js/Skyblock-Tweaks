package mrfast.sbt.customevents

import net.minecraftforge.fml.common.eventhandler.Event

open class SlayerEvent : Event() {
    class Death : SlayerEvent()
    class Spawn : SlayerEvent()
}

