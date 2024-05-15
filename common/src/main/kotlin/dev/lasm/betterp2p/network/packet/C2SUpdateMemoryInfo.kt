package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.item.ItemAdvancedMemoryCard
import dev.lasm.betterp2p.network.data.MemoryInfo
import dev.lasm.betterp2p.network.data.readMemoryInfo
import dev.lasm.betterp2p.network.data.writeMemoryInfo
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Supplier

class C2SUpdateMemoryInfo(var info: MemoryInfo = MemoryInfo()) : IMessage {
    override fun fromBytes(buf: FriendlyByteBuf) {
        info = readMemoryInfo(buf)
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        writeMemoryInfo(buf, info)
    }
}

val ServerUpdateMemoryInfoHandler = { message: C2SUpdateMemoryInfo, ctx: Supplier<NetworkManager.PacketContext> ->
    val player = ctx.get().player
    val stack = player.mainHandItem

    if (stack.item is ItemAdvancedMemoryCard) {
        ItemAdvancedMemoryCard.writeInfo(stack, message.info)
    }
    Unit
}
