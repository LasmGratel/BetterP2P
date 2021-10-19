package com.projecturanus.betterp2p.network

import com.projecturanus.betterp2p.capability.MemoryInfo
import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

class S2CListP2P(var infos: List<P2PInfo> = emptyList(),
                 var memoryInfo: MemoryInfo = MemoryInfo()) : IMessage {
    override fun fromBytes(buf: ByteBuf) {
        val length = buf.readInt()
        val list = ArrayList<P2PInfo>(length)
        for (i in 0 until length) {
            list += readInfo(buf)
        }
        infos = list
        memoryInfo = readMemoryInfo(buf)
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(infos.size)
        for (info in infos) {
            writeInfo(buf, info)
        }
        writeMemoryInfo(buf, memoryInfo!!)
    }
}
