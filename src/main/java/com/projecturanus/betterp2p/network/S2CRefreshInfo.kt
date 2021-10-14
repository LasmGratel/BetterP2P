package com.projecturanus.betterp2p.network

import io.netty.buffer.ByteBuf
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

fun readInfo(buf: ByteBuf): P2PInfo {
    val index = buf.readInt()
    val freq = buf.readShort()
    val pos = BlockPos.fromLong(buf.readLong())
    val facing = EnumFacing.values()[buf.readInt()]
    return P2PInfo(index, freq, pos, facing, buf.readBoolean())
}

fun writeInfo(buf: ByteBuf, info: P2PInfo) {
    buf.writeInt(info.index)
    buf.writeShort(info.frequency.toInt())
    buf.writeLong(info.pos.toLong())
    buf.writeInt(info.facing.index)
    buf.writeBoolean(info.output)
}

class S2CRefreshInfo(var input: P2PInfo? = null, var output: P2PInfo? = null) : IMessage {
    override fun fromBytes(buf: ByteBuf) {
        input = readInfo(buf)
        output = readInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        writeInfo(buf, input!!)
        writeInfo(buf, output!!)
    }
}
