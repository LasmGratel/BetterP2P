package dev.lasm.betterp2p.util

import appeng.api.parts.IPart
import appeng.api.parts.IPartHost
import appeng.api.parts.SelectedPart
import appeng.blockentity.networking.CableBusBlockEntity
import appeng.parts.AEBasePart
import appeng.parts.ICableBusContainer
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.phys.HitResult

/**
 * @see appeng.block.networking.BlockCableBus.cb
 */
fun getCableBus(w: BlockGetter, pos: BlockPos): ICableBusContainer? {
    val te = w.getBlockEntity(pos)
    var out: ICableBusContainer? = null
    if (te is CableBusBlockEntity) {
        out = te.cableBus
    }
    return out
}

fun getPart(w: BlockGetter, pos: BlockPos, hitResult: HitResult): IPart? {
    val te = w.getBlockEntity(pos)
    if (te !is IPartHost) return null
    val p: SelectedPart? = (te as IPartHost).selectPartWorld(hitResult.location)
    return p?.part
}

val AEBasePart.facingPos: BlockPos?
    get() =
        host?.location?.pos?.offset(side?.normal ?: Direction.UP.normal)

val AEBasePart.facingTile: BlockEntity?
    get() {
        if (host.isInWorld) {
            val pos = facingPos
            if (pos != null)
                return host?.location?.level?.getBlockEntity(pos)
        }
        return null
    }

