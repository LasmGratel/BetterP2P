package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf

/** Send a request to the server to refresh the p2p list with the given type. */
class C2SRefreshP2PList(var type: Int = TUNNEL_ANY) : IMessage {
    override fun fromBytes(buf: FriendlyByteBuf) {
        type = buf.readByte().toInt()
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        buf.writeByte(type)
    }
}

/** Client -> C2SRefreshP2P -> Server Handler on server side */
val ServerRefreshP2PListHandler =
    { message: C2SRefreshP2PList, ctx: Supplier<NetworkManager.PacketContext> ->
        ModNetwork.requestP2PList(ctx.get().player, message.type)
    }
