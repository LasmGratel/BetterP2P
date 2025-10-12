package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.network.data.BetterP2PCodecs
import dev.lasm.betterp2p.network.data.P2PInfo
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class S2CUpdateP2P(val infos: List<P2PInfo> = emptyList(), val clear: Boolean = false) :
    IS2CMessage {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE =
            CustomPacketPayload.Type<S2CUpdateP2P>(
                ResourceLocation.fromNamespaceAndPath(BetterP2P.MOD_ID, "update_p2p")
            )
        val STREAM_CODEC: StreamCodec<ByteBuf, S2CUpdateP2P> =
            StreamCodec.composite(
                ByteBufCodecs.collection(
                    ::ArrayList,
                    BetterP2PCodecs.P2P_INFO_STREAM,
                    Int.MAX_VALUE
                ),
                S2CUpdateP2P::infos,
                ByteBufCodecs.BOOL,
                S2CUpdateP2P::clear,
                ::S2CUpdateP2P
            )
    }
}

val ClientUpdateP2PHandler: ((S2CUpdateP2P, IPayloadContext) -> Unit) =
    { message: S2CUpdateP2P, _: IPayloadContext ->
        Minecraft.getInstance().submit {
            val gui = Minecraft.getInstance().screen

            if (gui is GuiAdvancedMemoryCard) {
                if (message.clear) {
                    gui.refreshInfo(message.infos)
                } else {
                    gui.updateInfo(message.infos)
                }
            }
        }
        Unit
    }
