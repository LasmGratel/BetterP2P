package dev.lasm.betterp2p.network.data

import appeng.parts.p2p.P2PTunnelPart
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

/**
 * Generates a 64-bit hash code from X, Y, Z, Facing, and Dimension info.
 * bits 0-47: FastHash of (x, y, z) ->
 * bits 48-59: dim
 * bits 60-62: facing
 * bit 63: Reserved
 */
fun hashP2P(pos: BlockPos, facing: Int, dim: ResourceKey<Level>): Long {
    val ret = facing.toULong() shl 60
    val lo: ULong = pos.x.toULong() or (pos.y.toULong() shl 32)
    val hi: ULong = pos.z.toULong() or (dim.location().hashCode().toULong() shl 32)
    var hash = hashLen16(lo, hi)
    hash = hash xor (hash shr 59)
    return (ret or hash).toLong()
}

fun hashP2P(p: P2PTunnelPart<*>): Long
    = hashP2P(p.blockEntity.blockPos, p.side.ordinal, p.level.dimension())

const val k2 = 0x9ae16a3b2f90404fUL

/**
 * Fetches the contiguous byte-aligned 64 bits from the 128 bit "register". Only works for indexes 1-7.
 * lo - low 64 bits
 * hi - high 64 bits
 * idx - start byte index, should be 1-7. don't even bother using this method for other amounts
 */
private fun fetch64(lo: ULong, hi: ULong, idx: Int): ULong {
    return (lo shr (idx * Byte.SIZE_BITS)) or (hi shl ULong.SIZE_BITS - (idx * Byte.SIZE_BITS))
}

/**
 * City64 hash. It's basically black magic, but here's a link
 * https://opensource.googleblog.com/2011/04/introducing-cityhash.html
 */
private fun hashLen16(lo: ULong, hi: ULong): ULong {
    val mul: ULong = k2 + 32U
    val a: ULong = lo + k2
    val b: ULong = hi
    val c: ULong = b.rotateRight(37) * mul + a
    val d: ULong = (a.rotateRight(25) + b) * mul

    var e: ULong = (c xor d) * mul
    e = e xor (e shr 47)
    var f: ULong = (d xor e) * mul
    f = f xor (f shr 47)
    f *= mul

    return f
}

