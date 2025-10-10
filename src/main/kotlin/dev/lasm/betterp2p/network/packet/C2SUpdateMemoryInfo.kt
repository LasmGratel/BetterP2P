package dev.lasm.betterp2p.network.packet

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.MemoryInfo
import io.netty.buffer.ByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class C2SUpdateMemoryInfo(val info: MemoryInfo = MemoryInfo()) : IC2SMessage {

    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<C2SUpdateMemoryInfo>(
            ResourceLocation.fromNamespaceAndPath(
                BetterP2P.MOD_ID,
                "update_memory_info"
            )
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SUpdateMemoryInfo> = StreamCodec.composite(
            MemoryInfo.STREAM_CODEC, C2SUpdateMemoryInfo::info,
            ::C2SUpdateMemoryInfo
        )
    }
}

val ServerUpdateMemoryInfoHandler: ((C2SUpdateMemoryInfo, IPayloadContext) -> Unit) =
    { message: C2SUpdateMemoryInfo, ctx: IPayloadContext ->
        val player = ctx.player()
        val stack = player.mainHandItem
        if(stack.has(BetterP2P.MEMORY_INFO)) {
            stack.update(BetterP2P.MEMORY_INFO.get(), MemoryInfo()) {_ -> message.info}
        }
        Unit
    }
