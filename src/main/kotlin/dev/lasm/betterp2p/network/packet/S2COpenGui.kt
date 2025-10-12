package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.network.data.*
import io.netty.buffer.ByteBuf
import net.minecraft.client.Minecraft
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class S2COpenGui(
    val infos: List<P2PInfo> = emptyList(),
    val memoryInfo: MemoryInfo = MemoryInfo()
) : IS2CMessage {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE =
            CustomPacketPayload.Type<S2COpenGui>(
                ResourceLocation.fromNamespaceAndPath(BetterP2P.MOD_ID, "open_gui")
            )
        val STREAM_CODEC: StreamCodec<ByteBuf, S2COpenGui> =
            StreamCodec.composite(
                ByteBufCodecs.collection(
                    ::ArrayList,
                    BetterP2PCodecs.P2P_INFO_STREAM,
                    Int.MAX_VALUE
                ),
                S2COpenGui::infos,
                MemoryInfo.STREAM_CODEC,
                S2COpenGui::memoryInfo,
                ::S2COpenGui
            )
    }
}

val ClientOpenGuiHandler: ((S2COpenGui, IPayloadContext) -> Unit) =
    { message: S2COpenGui, ctx: IPayloadContext ->
        val gui = Minecraft.getInstance().screen
        if (gui is GuiAdvancedMemoryCard) {
            gui.refreshInfo(message.infos)
            gui.memoryInfo = message.memoryInfo
        } else {
            Minecraft.getInstance().submit {
                Minecraft.getInstance()
                    .setScreen(
                        GuiAdvancedMemoryCard(
                            AdvancedMemoryCardMenu(0, null).also {
                                it.memoryInfo = message.memoryInfo
                                it.infos = message.infos
                            }
                        )
                    )
            }
        }
        Unit
    }
