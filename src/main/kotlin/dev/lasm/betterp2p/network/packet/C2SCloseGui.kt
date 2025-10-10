package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import java.util.function.Supplier
import net.minecraft.network.FriendlyByteBuf

class C2SCloseGui : IC2SMessage {
    override fun toBytes(buf: FriendlyByteBuf) {}

    override fun fromBytes(buf: FriendlyByteBuf) {}
}

val ServerCloseGuiHandler = { message: C2SCloseGui, ctx: Supplier<NetworkManager.PacketContext> ->
    ModNetwork.playerState.remove(ctx.get().player.uuid)
    Unit
}
