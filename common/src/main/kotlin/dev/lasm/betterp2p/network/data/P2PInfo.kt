package dev.lasm.betterp2p.network.data

import appeng.me.GridNode
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.util.p2p.getTypeIndex
import dev.lasm.betterp2p.util.p2p.hasChannel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.Registries
import net.minecraft.network.FriendlyByteBuf
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

fun readP2PInfo(buf: FriendlyByteBuf): P2PInfo? {
    try {
        val freq = buf.readShort()
        val pos = BlockPos.of(buf.readLong())
        val world = buf.readResourceKey(Registries.DIMENSION)
        val facing = Direction.values()[buf.readInt()]
        val name = buf.readUtf()
        val output = buf.readBoolean()
        val hasChannel = buf.readBoolean()
        val channels = buf.readByte().toInt()
        val type = buf.readByte().toInt()
        return P2PInfo(freq, pos, world, facing, name.toString(), output, hasChannel, channels, type)
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun writeP2PInfo(buf: FriendlyByteBuf, info: P2PInfo) {
    buf.writeShort(info.frequency.toInt())
    buf.writeLong(info.pos.asLong())
    buf.writeResourceKey(info.dim)
    buf.writeInt(info.facing.ordinal)
    buf.writeUtf(info.name)
    buf.writeBoolean(info.output)
    buf.writeBoolean(info.hasChannel)
    buf.writeByte(info.channels)
    buf.writeByte(info.type)
}

fun P2PTunnelPart<*>.toInfo()
    = P2PInfo(
    frequency,
    blockEntity.blockPos,
    blockEntity.level!!.dimension(),
    side,
    customName?.string ?: "",
    isOutput,
    hasChannel,
    (externalFacingNode as? GridNode)?.usedChannels() ?: -1,
    getTypeIndex()
)
