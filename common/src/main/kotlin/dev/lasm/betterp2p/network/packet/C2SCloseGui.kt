package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Supplier

class C2SCloseGui : IMessage {
    override fun toBytes(buf: FriendlyByteBuf) {
    }

    override fun fromBytes(buf: FriendlyByteBuf) {
    }
}


val ServerCloseGuiHandler = { message: C2SCloseGui, ctx: Supplier<NetworkManager.PacketContext> ->
    ModNetwork.playerState.remove(ctx.get().player.uuid)
    Unit
}
