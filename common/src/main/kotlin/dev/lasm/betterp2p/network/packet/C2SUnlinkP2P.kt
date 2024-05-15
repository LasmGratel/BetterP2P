package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import dev.lasm.betterp2p.network.data.readP2PLocation
import dev.lasm.betterp2p.network.data.writeP2PLocation
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Supplier

/**
 * Unlink input from outputs message (set freq to 0)
 */
class C2SUnlinkP2P(var p2p: P2PLocation? = null, var type: Int = TUNNEL_ANY): IMessage {
    override fun fromBytes(buf: FriendlyByteBuf) {
        p2p = readP2PLocation(buf)
        type = buf.readByte().toInt()
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        // Clientside can crash >:3
        writeP2PLocation(buf, p2p!!)
        buf.writeByte(type)
    }
}

/**
 * Client -> C2SUnlinkP2P -> Server
 * Handler on server side
 */
val ServerUnlinkP2PHandler = a@{ message: C2SUnlinkP2P, ctx: Supplier<NetworkManager.PacketContext> ->
    if (message.p2p == null) {
        return@a Unit
    }
    val cache = ModNetwork.playerState[ctx.get().player.uuid] ?: return@a Unit

    cache.gridCache.unlinkP2P(message.p2p!!)
    ModNetwork.requestP2PUpdate(ctx.get().player)
}
