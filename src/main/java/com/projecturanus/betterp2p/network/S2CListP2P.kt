package com.projecturanus.betterp2p.network

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage
import java.nio.charset.StandardCharsets

class S2CListP2P(var list: List<P2PInfo> = emptyList()): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        val l = mutableListOf<P2PInfo>()
        for (i in 0 until buf.readInt()) {
            val freq = buf.readShort()
            val length = buf.readInt()
            val facingBlock = if (length != 0) {
                buf.readCharSequence(length, StandardCharsets.UTF_8)
            } else { null }?.toString()
            val isInput = buf.readBoolean()
            l += P2PInfo(freq, facingBlock, isInput)
        }
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(list.size)
        for (info in list) {
            buf.writeShort(info.frequency.toInt())
            if (info.facingBlock == null) {
                buf.writeInt(0)
            } else {
                buf.writeInt(info.facingBlock.length)
                buf.writeCharSequence(info.facingBlock, StandardCharsets.UTF_8)
            }
            buf.writeBoolean(info.isInput)
        }
    }
}
