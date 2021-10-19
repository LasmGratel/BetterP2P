package com.projecturanus.betterp2p.item

import appeng.api.networking.IGridHost
import appeng.api.util.AEPartLocation
import appeng.core.CreativeTab
import appeng.parts.p2p.PartP2PTunnel
import com.projecturanus.betterp2p.capability.MemoryInfo
import com.projecturanus.betterp2p.client.ClientCache
import com.projecturanus.betterp2p.network.ModNetwork
import com.projecturanus.betterp2p.network.S2CListP2P
import com.projecturanus.betterp2p.util.getPart
import com.projecturanus.betterp2p.util.p2p.P2PCache
import com.projecturanus.betterp2p.util.p2p.P2PStatus
import com.projecturanus.betterp2p.util.p2p.getInfo
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.EnumActionResult
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

object ItemBetterMemoryCard : Item() {
    init {
        maxStackSize = 1
        translationKey = "better_memory_card"
        creativeTab = CreativeTab.instance
    }

    override fun onUpdate(stack: ItemStack, worldIn: World, entityIn: Entity, itemSlot: Int, isSelected: Boolean) {
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected)
    }

    private fun sendStatus(status: P2PStatus, info: MemoryInfo, player: EntityPlayerMP) {
        P2PCache.statusMap[player.uniqueID] = status
        ModNetwork.channel.sendTo(
            S2CListP2P(status.listP2P.mapIndexed { index, p2p -> p2p.getInfo(index) }, info),
            player
        )
    }

    @SideOnly(Side.CLIENT)
    override fun addInformation(stack: ItemStack, worldIn: World?, tooltip: MutableList<String>, flagIn: ITooltipFlag) {
        val info = getInfo(stack)
        tooltip += I18n.format("gui.better_memory_card.mode.${info.mode.name.toLowerCase()}")
    }

    @SideOnly(Side.CLIENT)
    private fun clearClientCache() {
        ClientCache.clear()
    }

    override fun onItemRightClick(worldIn: World, playerIn: EntityPlayer, handIn: EnumHand): ActionResult<ItemStack> {
        if (playerIn.isSneaking && worldIn.isRemote) {
            clearClientCache()
        }
        return super.onItemRightClick(worldIn, playerIn, handIn)
    }

    override fun onItemUse(player: EntityPlayer, w: World, pos: BlockPos, hand: EnumHand, side: EnumFacing, hx: Float, hy: Float, hz: Float): EnumActionResult {
        if (!w.isRemote) {
            val te = w.getTileEntity(pos)
            if (te is IGridHost && te.getGridNode(AEPartLocation.fromFacing(side)) != null) {
                val part = getPart(w, pos, hx, hy, hz)
                val stack = player.getHeldItem(hand)
                val info = getInfo(stack)
                if (part is PartP2PTunnel<*>) {
                    val status = P2PStatus(player, 0, part.gridNode.grid, part)
                    info.selectedIndex = status.listP2P.indexOfFirst { it == status.targetP2P }
                    writeInfo(stack, info)

                    sendStatus(status, info, player as EntityPlayerMP)
                    return EnumActionResult.SUCCESS
                } else {
                    val node = te.getGridNode(AEPartLocation.fromFacing(side))!!
                    info.selectedIndex = -1
                    writeInfo(stack, info)
                    sendStatus(P2PStatus(player, 0, node.grid), info, player as EntityPlayerMP)
                    return EnumActionResult.SUCCESS
                }
            }
        }
        return EnumActionResult.PASS
    }

    override fun doesSneakBypassUse(itemstack: ItemStack, world: IBlockAccess?, pos: BlockPos?, player: EntityPlayer?): Boolean {
        return true
    }

    fun getInfo(stack: ItemStack): MemoryInfo {
        if (stack.item != this) throw ClassCastException("Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}")

        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        val compound = stack.tagCompound!!
        if (!compound.hasKey("selectedIndex")) compound.setInteger("selectedIndex", -1)

        return MemoryInfo(compound.getInteger("selectedIndex"), BetterMemoryCardModes.values()[compound.getInteger("mode")])
    }

    fun writeInfo(stack: ItemStack, info: MemoryInfo) {
        if (stack.item != this) throw ClassCastException("Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}")

        if (stack.tagCompound == null) stack.tagCompound = NBTTagCompound()
        val compound = stack.tagCompound!!
        compound.setInteger("selectedIndex", info.selectedIndex)
        compound.setInteger("mode", info.mode.ordinal)
    }
}
