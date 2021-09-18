package com.projecturanus.betterp2p.network

import io.netty.buffer.ByteBuf
import net.minecraftforge.fml.common.network.simpleimpl.IMessage

class AEPartLocationMessage(var x: Int = 0, var y: Int = 0, var z: Int = 0, var hitX: Float = 0f, var hitY: Float = 0f, var hitZ: Float = 0f): IMessage {
    override fun fromBytes(buf: ByteBuf) {
        x = buf.readInt()
        y = buf.readInt()
        z = buf.readInt()
        hitX = buf.readFloat()
        hitY = buf.readFloat()
        hitZ = buf.readFloat()
    }

    override fun toBytes(buf: ByteBuf) {
        buf.writeInt(x)
        buf.writeInt(y)
        buf.writeInt(z)
        buf.writeFloat(hitX)
        buf.writeFloat(hitY)
        buf.writeFloat(hitZ)
    }
}
