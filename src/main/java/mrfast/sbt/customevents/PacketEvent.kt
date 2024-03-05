package mrfast.sbt.customevents

import net.minecraft.network.Packet
import net.minecraftforge.fml.common.eventhandler.Event


open class PacketEvent : Event() {
    class Received(val packet: Packet<*>) : PacketEvent() {
        override fun isCancelable(): Boolean {
            return true
        }
    }

    class Sending(val packet: Packet<*>) : PacketEvent() {
        override fun isCancelable(): Boolean {
            return true
        }
    }
}