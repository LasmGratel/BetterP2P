package dev.lasm.betterp2p.network.data

import appeng.me.GridNode
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.util.p2p.getTypeIndex
import dev.lasm.betterp2p.util.p2p.hasChannel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

class P2PInfo(
    val frequency: Short,
    val pos: BlockPos,
    val dim: ResourceKey<Level>,
    val facing: Direction,
    val name: String,
    val output: Boolean,
    val hasChannel: Boolean,
    val channels: Int,
    val type: Int
) {
    override fun hashCode(): Int {
        return hashP2P(pos, facing.ordinal, dim).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as P2PInfo
        if (this.pos != other.pos) return false
        if (facing != other.facing) return false
        return dim == other.dim
    }
}

fun P2PTunnelPart<*>.toInfo() =
    P2PInfo(
        frequency,
        blockEntity.blockPos,
        blockEntity.level!!.dimension(),
        side,
        customName?.string ?: "",
        isOutput,
        hasChannel,
        (externalFacingNode as? GridNode)?.getUsedChannels() ?: -1,
        getTypeIndex()
    )
