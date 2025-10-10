package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class C2SChangeP2PType(val newType: Int = TUNNEL_ANY, val p2p: P2PLocation) : IC2SMessage {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<C2SChangeP2PType>(
            ResourceLocation.fromNamespaceAndPath(
                BetterP2P.MOD_ID,
                "change_p2p_type"
            )
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SChangeP2PType> = StreamCodec.composite(
            ByteBufCodecs.INT, C2SChangeP2PType::newType,
            P2PLocation.STREAM_CODEC, C2SChangeP2PType::p2p,
            ::C2SChangeP2PType
        )
    }
}

val ServerTypeChangeHandler: ((C2SChangeP2PType, IPayloadContext) -> Unit) =
    a@{ message: C2SChangeP2PType, ctx: IPayloadContext ->

        val state = ModNetwork.playerState[ctx.player().uuid] ?: return@a
        val type = BetterP2P.proxy.getP2PFromIndex(message.newType) ?: return@a

        if (state.gridCache.changeAllP2Ps(message.p2p, type)) {
            ModNetwork.requestP2PList(ctx.player(), type.index)
        }
        Unit
    }
