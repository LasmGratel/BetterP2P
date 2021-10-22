package com.projecturanus.betterp2p.util.p2p

import appeng.api.config.SecurityPermissions
import appeng.api.networking.IGrid
import appeng.api.networking.IGridConnection
import appeng.api.networking.IGridNode
import appeng.api.networking.security.ISecurityGrid
import appeng.api.parts.IPart
import appeng.api.parts.PartItemStack
import appeng.api.util.AEColor
import appeng.api.util.AEPartLocation
import appeng.me.GridAccessException
import appeng.me.GridNode
import appeng.parts.p2p.PartP2PTunnel
import appeng.util.Platform
import com.projecturanus.betterp2p.network.P2PInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

val PartP2PTunnel<*>.colorCode: Array<AEColor> get() = Platform.p2p().toColors(this.frequency)

fun linkP2P(player: EntityPlayer, inputIndex: Int, outputIndex: Int, status: P2PStatus) : Pair<PartP2PTunnel<*>, PartP2PTunnel<*>>? {
    val input = status.listP2P[inputIndex]
    val output = status.listP2P[outputIndex]

    val grid: IGrid? = input.gridNode?.grid
    if (grid is ISecurityGrid) {
        if (!grid.hasPermission(player, SecurityPermissions.BUILD)) {
            return null
        }
    }

    // TODO Change to exception
    if (input.javaClass != output.javaClass) {
        // Cannot pair two different type of P2P
        return null
    }
    if (input == output) {
        // Network loop
        return null
    }
    var frequency = input.frequency
    val cache = input.proxy.p2P
    // TODO reduce changes
    if (input.frequency.toInt() == 0 || input.isOutput) {
        frequency = cache.newFrequency()
        updateP2P(input, frequency, false)
    }
    if (cache.getInput(frequency) != null) {
        val originalInput = cache.getInput(frequency)
        if (originalInput != input)
            updateP2P(originalInput, frequency, true)
    }

    return updateP2P(input, frequency, false) to updateP2P(output, frequency, true)
}

/**
 * Due to Applied Energistics' limit
 */
fun updateP2P(tunnel: PartP2PTunnel<*>, frequency: Short, output: Boolean): PartP2PTunnel<*> {
    tunnel.host.removePart(tunnel.side, true)

    val data = NBTTagCompound()
    val p2pItem: ItemStack = tunnel.getItemStack(PartItemStack.WRENCH)
    val type: String = p2pItem.translationKey
    tunnel.outputProperty = output

    p2pItem.writeToNBT(data)
    data.setShort("freq", frequency)

    val colors = Platform.p2p().toColors(frequency)
    val colorCode = intArrayOf(
        colors[0].ordinal, colors[0].ordinal, colors[1].ordinal, colors[1].ordinal,
        colors[2].ordinal, colors[2].ordinal, colors[3].ordinal, colors[3].ordinal
    )

    data.setIntArray("colorCode", colorCode)

    val newType = ItemStack(data)
    val dir: AEPartLocation = tunnel.host.addPart(newType, tunnel.side, null, null)
    val newBus: IPart = tunnel.host.getPart(dir)

    if (newBus is PartP2PTunnel<*>) {
        val newTunnel = newBus
        newTunnel.outputProperty = output
        try {
            val p2p = newTunnel.proxy.p2P
            p2p.updateFreq(newTunnel, frequency)
        } catch (e: GridAccessException) {
            // :P
        }
        newTunnel.onTunnelNetworkChange()
        return newTunnel
    } else {
        throw RuntimeException("Cannot bind")
    }
}

var PartP2PTunnel<*>.outputProperty
    get() = isOutput
    set(value) {
        val field = PartP2PTunnel::class.java.getDeclaredField("output")
        field.isAccessible = true
        field.setBoolean(this, value)
    }

val IGridNode.connectionList: java.util.List<IGridConnection>
    get() {
        val field = GridNode::class.java.getDeclaredField("connections")
        field.isAccessible = true
        return field.get(this as GridNode) as java.util.List<IGridConnection>
    }

val PartP2PTunnel<*>.hasChannel
    get() = isPowered && isActive

fun PartP2PTunnel<*>.getInfo(index: Int)
    = P2PInfo(index, frequency, location.pos, side.facing, isOutput, hasChannel)
