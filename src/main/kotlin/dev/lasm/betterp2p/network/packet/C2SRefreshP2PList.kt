package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

/** Send a request to the server to refresh the p2p list with the given type. */
class C2SRefreshP2PList(val type: Int = TUNNEL_ANY) : IC2SMessage {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<C2SRefreshP2PList>(
            ResourceLocation.fromNamespaceAndPath(
                BetterP2P.MOD_ID,
                "refresh_p2p_list"
            )
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SRefreshP2PList> = StreamCodec.composite(
            ByteBufCodecs.INT, C2SRefreshP2PList::type,
            ::C2SRefreshP2PList
        )
    }
}

/** Client -> C2SRefreshP2P -> Server Handler on server side */
val ServerRefreshP2PListHandler: ((C2SRefreshP2PList, IPayloadContext) -> Unit) =
    { message: C2SRefreshP2PList, ctx: IPayloadContext ->
        ModNetwork.requestP2PList(ctx.player(), message.type)
    }
