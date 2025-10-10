package dev.lasm.betterp2p.network.packet

import net.minecraft.network.FriendlyByteBuf

sealed interface IMessage {
    fun toBytes(buf: FriendlyByteBuf)
    fun fromBytes(buf: FriendlyByteBuf)
}
interface IC2SMessage: IMessage {}
interface IS2CMessage: IMessage {}
