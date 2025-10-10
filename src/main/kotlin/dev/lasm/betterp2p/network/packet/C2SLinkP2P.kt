package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.readP2PLocation
import dev.lasm.betterp2p.network.data.writeP2PLocation
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf

class C2SLinkP2P(var input: P2PLocation? = null, var output: P2PLocation? = null) : IC2SMessage {
    override fun fromBytes(buf: FriendlyByteBuf) {
        input = readP2PLocation(buf)
        output = readP2PLocation(buf)
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        writeP2PLocation(buf, input!!)
        writeP2PLocation(buf, output!!)
    }
}

val ServerLinkP2PHandler = { message: C2SLinkP2P, ctx: Supplier<NetworkManager.PacketContext> ->
    if (message.input == null || message.output == null) {
        Unit
    } else {
        ModNetwork.playerState[ctx.get().player.uuid]?.also { state ->
            val result = state.gridCache.linkP2P(message.input!!, message.output!!)

            if (result != null) {
                ModNetwork.requestP2PUpdate(ctx.get().player)
            }
        }
        Unit
    }
}
