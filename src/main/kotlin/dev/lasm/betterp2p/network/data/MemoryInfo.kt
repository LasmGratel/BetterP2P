package dev.lasm.betterp2p.network.data

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.lasm.betterp2p.client.gui.widget.GuiScale
import dev.lasm.betterp2p.item.BetterMemoryCardModes
import io.netty.buffer.ByteBuf
import java.util.Optional
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec

const val TUNNEL_ANY: Int = -1

data class MemoryInfo(
    val selectedEntry: Optional<P2PLocation> = Optional.empty(),
    val frequency: Short = 0,
    val mode: BetterMemoryCardModes = BetterMemoryCardModes.OUTPUT,
    val guiScale: GuiScale = GuiScale.DYNAMIC,
    val type: Int = TUNNEL_ANY
) {
    companion object {
        val STREAM_CODEC: StreamCodec<ByteBuf, MemoryInfo> =
            StreamCodec.composite(
                ByteBufCodecs.optional(P2PLocation.STREAM_CODEC),
                MemoryInfo::selectedEntry,
                ByteBufCodecs.SHORT,
                MemoryInfo::frequency,
                ByteBufCodecs.INT.map(
                    BetterMemoryCardModes.values()::get,
                    BetterMemoryCardModes::ordinal
                ),
                MemoryInfo::mode,
                ByteBufCodecs.INT.map(GuiScale.values()::get, GuiScale::ordinal),
                MemoryInfo::guiScale,
                ByteBufCodecs.INT,
                MemoryInfo::type,
                ::MemoryInfo
            )

        val CODEC: Codec<MemoryInfo> =
            RecordCodecBuilder.create { instance ->
                instance
                    .group(
                        Codec.optionalField("selectedEntry", P2PLocation.CODEC, false)
                            .forGetter(MemoryInfo::selectedEntry),
                        Codec.SHORT.fieldOf("frequency").forGetter(MemoryInfo::frequency),
                        Codec.INT.xmap(
                                BetterMemoryCardModes.values()::get,
                                BetterMemoryCardModes::ordinal
                            )
                            .fieldOf("mode")
                            .forGetter(MemoryInfo::mode),
                        Codec.INT.xmap(GuiScale.values()::get, GuiScale::ordinal)
                            .fieldOf("guiScale")
                            .forGetter(MemoryInfo::guiScale)
                    )
                    .apply(instance, ::MemoryInfo)
            }
    }
}
