package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class C2SLinkP2P(val input: P2PLocation, val output: P2PLocation) : IC2SMessage {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE =
            CustomPacketPayload.Type<C2SLinkP2P>(
                ResourceLocation.fromNamespaceAndPath(BetterP2P.MOD_ID, "link_p2p")
            )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SLinkP2P> =
            StreamCodec.composite(
                P2PLocation.STREAM_CODEC,
                C2SLinkP2P::input,
                P2PLocation.STREAM_CODEC,
                C2SLinkP2P::output,
                ::C2SLinkP2P
            )
    }
}

val ServerLinkP2PHandler: ((C2SLinkP2P, IPayloadContext) -> Unit) =
    { message: C2SLinkP2P, ctx: IPayloadContext ->
        ModNetwork.playerState[ctx.player().uuid]?.also { state ->
            val result = state.gridCache.linkP2P(message.input, message.output)

            if (result != null) {
                ModNetwork.requestP2PUpdate(ctx.player())
            }
        }
        Unit
    }
