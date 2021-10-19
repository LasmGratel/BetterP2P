package com.projecturanus.betterp2p.client

import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos

object ClientCache {
    val positions = mutableListOf<Pair<BlockPos, EnumFacing>>()
    var selectedPosition: BlockPos? = null
    var selectedFacing: EnumFacing? = null

    fun clear() {
        positions.clear()
        selectedPosition = null
        selectedFacing = null
    }
}
