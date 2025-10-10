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

/** Unlink input from outputs message (set freq to 0) */
class C2SUnlinkP2P(val p2p: P2PLocation, val type: Int = TUNNEL_ANY) : IC2SMessage {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<C2SUnlinkP2P>(
            ResourceLocation.fromNamespaceAndPath(
                BetterP2P.MOD_ID,
                "unlink_p2p"
            )
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SUnlinkP2P> = StreamCodec.composite(
            P2PLocation.STREAM_CODEC, C2SUnlinkP2P::p2p,
            ByteBufCodecs.INT, C2SUnlinkP2P::type,
            ::C2SUnlinkP2P
        )
    }
}

/** Client -> C2SUnlinkP2P -> Server Handler on server side */
val ServerUnlinkP2PHandler: ((C2SUnlinkP2P, IPayloadContext) -> Unit) =
    a@{ message: C2SUnlinkP2P, ctx: IPayloadContext ->
        val cache = ModNetwork.playerState[ctx.player().uuid] ?: return@a
        cache.gridCache.unlinkP2P(message.p2p)
        ModNetwork.requestP2PUpdate(ctx.player())
    }
