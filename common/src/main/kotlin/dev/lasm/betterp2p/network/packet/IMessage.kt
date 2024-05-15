package dev.lasm.betterp2p.network.packet

import net.minecraft.network.FriendlyByteBuf

interface IMessage {
    fun toBytes(buf: FriendlyByteBuf)
    fun fromBytes(buf: FriendlyByteBuf)
}
