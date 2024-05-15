package dev.lasm.betterp2p.network.packet

import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.network.data.P2PInfo
import dev.lasm.betterp2p.network.data.readP2PInfo
import dev.lasm.betterp2p.network.data.writeP2PInfo
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import java.util.function.Supplier

class S2CUpdateP2P(var infos: List<P2PInfo> = emptyList(), var clear: Boolean = false) : IMessage {
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
        clear = buf.readBoolean()
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        buf.writeInt(infos.size)
        infos.forEach { writeP2PInfo(buf, it) }
        buf.writeBoolean(clear)
    }
}

val ClientUpdateP2PHandler = { message: S2CUpdateP2P, _: Supplier<NetworkManager.PacketContext> ->
    Minecraft.getInstance().submit {
        val gui = Minecraft.getInstance().screen

        if (gui is GuiAdvancedMemoryCard) {
            if (message.clear) {
                gui.refreshInfo(message.infos)
            } else {
                gui.updateInfo(message.infos)
            }
        }
    }
    Unit
}

