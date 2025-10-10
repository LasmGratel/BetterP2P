package dev.lasm.betterp2p.network.packet

import appeng.api.networking.IInWorldGridNodeHost
import appeng.api.parts.IPartHost
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.P2PLocation
import dev.lasm.betterp2p.network.data.toLoc
import dev.lasm.betterp2p.util.p2p.setCustomName
import io.netty.buffer.ByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.network.handling.IPayloadContext

class C2SRenameP2P(val p2p: P2PLocation, val name: String) : IC2SMessage {
    override fun type(): CustomPacketPayload.Type<out CustomPacketPayload?> = TYPE

    companion object {
        val TYPE = CustomPacketPayload.Type<C2SRenameP2P>(
            ResourceLocation.fromNamespaceAndPath(
                BetterP2P.MOD_ID,
                "rename_p2p"
            )
        )
        val STREAM_CODEC: StreamCodec<ByteBuf, C2SRenameP2P> = StreamCodec.composite(
            P2PLocation.STREAM_CODEC, C2SRenameP2P::p2p,
        ByteBufCodecs.STRING_UTF8, C2SRenameP2P::name,
        ::C2SRenameP2P
        )
    }
}

val ServerRenameP2PTunnelHandler: (C2SRenameP2P, IPayloadContext) -> Unit =
    a@{ message: C2SRenameP2P, ctx: IPayloadContext ->
        val player = ctx.player()

        val world = player.server?.getLevel(message.p2p.dim) ?: return@a
        val te =
            world.getChunkAt(message.p2p.pos).getBlockEntity(message.p2p.pos) ?: return@a
        val state = ModNetwork.playerState[player.uuid] ?: return@a
        val facing = message.p2p.facing

        if (te is IInWorldGridNodeHost && te is IPartHost && te.getGridNode(facing) != null) {
            val partTunnel = te.getPart(facing)

            if (partTunnel is P2PTunnelPart<*>) {
                partTunnel.setCustomName(Component.literal(message.name))
                val input: P2PTunnelPart<*> =
                    if (partTunnel.isOutput) {
                        partTunnel.getInput()!!
                    } else {
                        partTunnel
                    }
                // Mark all dirty
                input.outputs.forEach { state.gridCache.markDirty(it.toLoc(), it) }
                state.gridCache.markDirty(input.toLoc(), input)

                ModNetwork.requestP2PUpdate(player)
            }
        }
        Unit
    }
