package dev.lasm.betterp2p.network.data

import com.mojang.datafixers.util.Function9
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import java.util.function.Function

object BetterP2PCodecs {

    val P2P_INFO_STREAM: StreamCodec<ByteBuf, P2PInfo> = composite(
        ByteBufCodecs.SHORT, P2PInfo::frequency,
        BlockPos.STREAM_CODEC, P2PInfo::pos,
        ResourceKey.streamCodec(Registries.DIMENSION), P2PInfo::dim,
        Direction.STREAM_CODEC, P2PInfo::facing,
        ByteBufCodecs.STRING_UTF8, P2PInfo::name,
        ByteBufCodecs.BOOL, P2PInfo::output,
        ByteBufCodecs.BOOL, P2PInfo::hasChannel,
        ByteBufCodecs.INT, P2PInfo::channels,
        ByteBufCodecs.INT, P2PInfo::type,
        ::P2PInfo
    )

    fun <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9> composite(
        codec1: StreamCodec<in B, T1>,
        getter1: Function<C, T1>,
        codec2: StreamCodec<in B, T2>,
        getter2: Function<C, T2>,
        codec3: StreamCodec<in B, T3>,
        getter3: Function<C, T3>,
        codec4: StreamCodec<in B, T4>,
        getter4: Function<C, T4>,
        codec5: StreamCodec<in B, T5>,
        getter5: Function<C, T5>,
        codec6: StreamCodec<in B, T6>,
        getter6: Function<C, T6>,
        codec7: StreamCodec<in B, T7>,
        getter7: Function<C, T7>,
        codec8: StreamCodec<in B, T8>,
        getter8: Function<C, T8>,
        codec9: StreamCodec<in B, T9>,
        getter9: Function<C, T9>,
        factory: Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, C>
    ): StreamCodec<B, C> {
        return object : StreamCodec<B, C> {
            override fun decode(buffer: B): C {
                val t1 = codec1.decode(buffer)
                val t2 = codec2.decode(buffer)
                val t3 = codec3.decode(buffer)
                val t4 = codec4.decode(buffer)
                val t5 = codec5.decode(buffer)
                val t6 = codec6.decode(buffer)
                val t7 = codec7.decode(buffer)
                val t8 = codec8.decode(buffer)
                val t9 = codec9.decode(buffer)
                return factory.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9)
            }

            override fun encode(buffer: B, value: C) {
                codec1.encode(buffer, getter1.apply(value))
                codec2.encode(buffer, getter2.apply(value))
                codec3.encode(buffer, getter3.apply(value))
                codec4.encode(buffer, getter4.apply(value))
                codec5.encode(buffer, getter5.apply(value))
                codec6.encode(buffer, getter6.apply(value))
                codec7.encode(buffer, getter7.apply(value))
                codec8.encode(buffer, getter8.apply(value))
                codec9.encode(buffer, getter9.apply(value))
            }
        }
    }
}
