package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import dev.lasm.betterp2p.network.data.readP2PLocation
import dev.lasm.betterp2p.network.data.writeP2PLocation
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf

class C2STypeChange(var newType: Int = TUNNEL_ANY, var p2p: P2PLocation? = null) : IC2SMessage {
    override fun fromBytes(buf: FriendlyByteBuf) {
        newType = buf.readByte().toInt()
        p2p = readP2PLocation(buf)
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        buf.writeByte(newType)
        writeP2PLocation(buf, p2p!!)
    }
}

val ServerTypeChangeHandler =
    a@{ message: C2STypeChange, ctx: Supplier<NetworkManager.PacketContext> ->
        if (message.p2p == null) {
            return@a Unit
        }

        val state = ModNetwork.playerState[ctx.get().player.uuid] ?: return@a Unit
        val type = BetterP2P.proxy.getP2PFromIndex(message.newType) ?: return@a Unit

        if (state.gridCache.changeAllP2Ps(message.p2p!!, type)) {
            ModNetwork.requestP2PList(ctx.get().player, type.index)
        }
        Unit
    }
