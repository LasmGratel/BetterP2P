package com.projecturanus.betterp2p.network

import io.netty.buffer.ByteBuf
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

class S2CListP2P(var infos: List<P2PInfo> = emptyList(),

                 // Index of target p2p
                 var targetIndex: Int = -1) : IMessage {
    override fun fromBytes(buf: ByteBuf) {
        val length = buf.readInt()
        val list = ArrayList<P2PInfo>(length)
        for (i in 0 until length) {
            list += readInfo(buf)
        }
        infos = list
        targetIndex = buf.readInt()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(infos.size)
        for (info in infos) {
            writeInfo(buf, info)
        }
        buf.writeInt(targetIndex)
    }
}
