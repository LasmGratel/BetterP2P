package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager.PacketContext
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.network.data.*
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Supplier


class S2COpenGui(var infos: List<P2PInfo> = emptyList(),
                 var memoryInfo: MemoryInfo = MemoryInfo()) : IMessage {
    override fun toBytes(buf: FriendlyByteBuf) {
        buf.writeInt(infos.size)
        for (info in infos) {
            writeP2PInfo(buf, info)
        }
        writeMemoryInfo(buf, memoryInfo)
    }

    override fun fromBytes(buf: FriendlyByteBuf) {
        val length = buf.readInt()
        val list = ArrayList<P2PInfo>(length)

        for (i in 0 until length) {
            val info = readP2PInfo(buf)

            if (info != null) {
                list.add(info)
            }
        }

        infos = list
        memoryInfo = readMemoryInfo(buf)
    }

}

val ClientOpenGuiHandler = { message: S2COpenGui, ctx: Supplier<PacketContext> ->
    val gui = Minecraft.getInstance().screen
    if (gui is GuiAdvancedMemoryCard) {
        gui.refreshInfo(message.infos)
        gui.memoryInfo = message.memoryInfo
    } else {
        Minecraft.getInstance().submit {
            Minecraft.getInstance().setScreen(GuiAdvancedMemoryCard(AdvancedMemoryCardMenu(0, null).also { it.memoryInfo = message.memoryInfo; it.infos = message.infos }))
        }
    }
    Unit
}
