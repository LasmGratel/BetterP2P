package dev.lasm.betterp2p.client

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

object ClientCache {
    val positions = mutableListOf<Pair<BlockPos, Direction>>()
    var selectedPosition: BlockPos? = null
    var selectedFacing: Direction? = null
    var searchText: String = ""

    fun clear() {
        positions.clear()
        selectedPosition = null
        selectedFacing = null
        searchText = ""
    }
}
