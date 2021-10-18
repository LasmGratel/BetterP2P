package com.projecturanus.betterp2p.item

import appeng.api.networking.IGridHost
import appeng.api.util.AEPartLocation
import appeng.core.CreativeTab
import appeng.items.AEBaseItem
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.S2CListP2P
import com.projecturanus.betterp2p.util.getPart
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.P2PStatus
import com.projecturanus.betterp2p.util.p2p.getInfo
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World

object ItemBetterMemoryCard : AEBaseItem() {
    init {
        maxStackSize = 1
        translationKey = "better_memory_card"
        creativeTab = CreativeTab.instance
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected)
    }

    private fun sendStatus(status: P2PStatus, player: EntityPlayerMP) {
        P2PCache.statusMap[player.uniqueID] = status
        ModNetwork.channel.sendTo(
            S2CListP2P(status.listP2P.mapIndexed { index, p2p -> p2p.getInfo(index) }, status.listP2P.indexOfFirst { it == status.targetP2P }),
            player
        )
    }

    override fun onItemUse(player: EntityPlayer, w: World, pos: BlockPos, hand: EnumHand, side: EnumFacing, hx: Float, hy: Float, hz: Float): EnumActionResult {
        if (!w.isRemote) {
            val te = w.getTileEntity(pos)
            if (te is IGridHost && te.getGridNode(AEPartLocation.fromFacing(side)) != null) {
                val part = getPart(w, pos, hx, hy, hz)
                if (part is PartP2PTunnel<*>) {
                    sendStatus(P2PStatus(0, part.gridNode.grid, part), player as EntityPlayerMP)
                    return EnumActionResult.SUCCESS
                } else {
                    val node = te.getGridNode(AEPartLocation.fromFacing(side))!!
                    sendStatus(P2PStatus(0, node.grid), player as EntityPlayerMP)
                    return EnumActionResult.SUCCESS
                }
            }
        }
        return EnumActionResult.PASS
    }

    override fun doesSneakBypassUse(itemstack: ItemStack, world: IBlockAccess?, pos: BlockPos?, player: EntityPlayer?): Boolean {
        return true
    }
}
