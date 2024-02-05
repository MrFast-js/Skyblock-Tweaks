package mrfast.sbt.customevents

import io.socket.engineio.client.Socket
import net.minecraftforge.fml.common.eventhandler.Event


class SocketMessageEvent(var socket: Socket, var message: String, var type: String) : Event()

