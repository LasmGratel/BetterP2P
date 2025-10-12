package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class C2SCloseGui : IC2SMessage {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE =
            CustomPacketPayload.Type<C2SCloseGui>(
                ResourceLocation.fromNamespaceAndPath(BetterP2P.MOD_ID, "close_gui")
            )
        private val EMPTY = C2SCloseGui()
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SCloseGui> = StreamCodec.unit(EMPTY)
    }

    override fun equals(other: Any?): Boolean = other is C2SCloseGui
    override fun hashCode(): Int = 0
}

val ServerCloseGuiHandler: ((C2SCloseGui, IPayloadContext) -> Unit) =
    { message: C2SCloseGui, ctx: IPayloadContext ->
        ModNetwork.playerState.remove(ctx.player().uuid)
        Unit
    }
