package com.projecturanus.betterp2p.util

import appeng.api.parts.IPart
import appeng.api.parts.IPartHost
import appeng.api.parts.SelectedPart
import appeng.parts.AEBasePart
import appeng.parts.ICableBusContainer
import appeng.tile.networking.TileCableBus
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.IBlockAccess

/**
 * @see appeng.block.networking.BlockCableBus.cb
 */
fun getCableBus(w: IBlockAccess, pos: BlockPos): ICableBusContainer? {
    val te = w.getTileEntity(pos)
    var out: ICableBusContainer? = null
    if (te is TileCableBus) {
        out = te.cableBus
    }
    return out
}

fun getPart(w: IBlockAccess, pos: BlockPos, hitX: Float, hitY: Float, hitZ: Float): IPart? {
    val vec = Vec3d(hitX.toDouble(), hitY.toDouble(), hitZ.toDouble())
    val te = w.getTileEntity(pos)
    val p: SelectedPart? = (te as IPartHost?)?.selectPart(vec)
    return p?.part
}

val AEBasePart.facingPos: BlockPos?
    get() =
        host?.location?.pos?.offset(side?.facing ?: EnumFacing.UP)

val AEBasePart.facingTile: TileEntity?
    get() {
        if (host.isInWorld) {
            val pos = facingPos
            if (pos != null)
                return host?.location?.world?.getTileEntity(pos)
        }
        return null
    }
