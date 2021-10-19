package com.projecturanus.betterp2p.util.p2p

import appeng.api.config.SecurityPermissions
import appeng.api.networking.security.ISecurityGrid
import appeng.api.util.AEColor
import appeng.parts.p2p.PartP2PTunnel
import appeng.util.Platform
import com.projecturanus.betterp2p.network.P2PInfo
import net.minecraft.entity.player.EntityPlayer

val PartP2PTunnel<*>.colorCode: Array<AEColor> get() = Platform.p2p().toColors(this.frequency)

fun linkP2P(player: EntityPlayer, input: PartP2PTunnel<*>, output: PartP2PTunnel<*>) : Boolean {
    val grid = input.gridNode.grid
    if (grid is ISecurityGrid) {
        if (!grid.hasPermission(player, SecurityPermissions.BUILD)) {
            return false
        }
    }

    // TODO Change to exception
    if (input.javaClass != output.javaClass) {
        // Cannot pair two different type of P2P
        return false
    }
    if (input == output) {
        // Network loop
        return false
    }
    val cache = input.proxy.p2P
    // TODO reduce changes
    if (input.frequency.toInt() == 0 || input.isOutput) {
        cache.updateFreq(input, cache.newFrequency())
        input.onTunnelConfigChange()
        output.onTunnelNetworkChange()
    }
    if (cache.getInput(input.frequency) != null) {
        val originalInput = cache.getInput(input.frequency)
        originalInput.outputProperty = false
        originalInput.onTunnelConfigChange()
        originalInput.onTunnelNetworkChange()
    }
    input.outputProperty = false
    output.outputProperty = true
    cache.updateFreq(output, input.frequency)
    output.onTunnelConfigChange()
    output.onTunnelNetworkChange()
    return true
}

var PartP2PTunnel<*>.outputProperty
    get() = isOutput
    set(value) {
        val field = PartP2PTunnel::class.java.getDeclaredField("output")
        field.isAccessible = true
        field.setBoolean(this, value)
    }

val PartP2PTunnel<*>.hasChannel
    get() = isPowered && isActive

fun PartP2PTunnel<*>.getInfo(index: Int)
    = P2PInfo(index, frequency, location.pos, side.facing, isOutput, hasChannel)
