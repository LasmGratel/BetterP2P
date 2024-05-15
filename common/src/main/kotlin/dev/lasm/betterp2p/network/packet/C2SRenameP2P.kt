package dev.lasm.betterp2p.network.packet

import appeng.api.networking.IInWorldGridNodeHost
import appeng.api.parts.IPartHost
import appeng.parts.p2p.P2PTunnelPart
import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.readP2PLocation
import dev.lasm.betterp2p.network.data.toLoc
import dev.lasm.betterp2p.network.data.writeP2PLocation
import dev.lasm.betterp2p.util.p2p.setCustomName
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import java.util.function.Supplier

class C2SRenameP2P(var p2p: P2PLocation? = null, var name: String = ""): IMessage {

    override fun fromBytes(buf: FriendlyByteBuf) {
        p2p = readP2PLocation(buf)
        name = buf.readUtf()
    }

    override fun toBytes(buf: FriendlyByteBuf) {
        writeP2PLocation(buf, p2p!!)
        buf.writeUtf(name)
    }
}

val ServerRenameP2PTunnelHandler = a@{ message: C2SRenameP2P, ctx: Supplier<NetworkManager.PacketContext> ->
    if (message.p2p == null) {
        return@a Unit
    }
    val player = ctx.get().player;

    val world = player.server?.getLevel(message.p2p!!.dim) ?: return@a Unit
    val te = world.getChunkAt(message.p2p!!.pos).getBlockEntity(message.p2p!!.pos) ?: return@a Unit
    val state = ModNetwork.playerState[player.uuid] ?: return@a Unit
    val facing = message.p2p!!.facing

    if (te is IInWorldGridNodeHost && te is IPartHost && te.getGridNode(facing) != null) {
        val partTunnel = te.getPart(facing)

        if (partTunnel is P2PTunnelPart<*>) {
            partTunnel.setCustomName(Component.literal(message.name))
            val input: P2PTunnelPart<*> = if (partTunnel.isOutput) {
                partTunnel.getInput()!!
            } else {
                partTunnel
            }
            // Mark all dirty
            input.outputs.forEach {
                state.gridCache.markDirty(it.toLoc(), it)
            }
            state.gridCache.markDirty(input.toLoc(), input)

            ModNetwork.requestP2PUpdate(player)
        }
    }
    Unit
}

