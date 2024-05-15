package dev.lasm.betterp2p.util.p2p

import appeng.parts.AEBasePart
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.TUNNEL_ANY
import net.minecraft.network.chat.Component

val P2PTunnelPart<*>.hasChannel
    get() = isPowered && isActive

/**
 * Get the type index or use TUNNEL_ANY
 */
fun P2PTunnelPart<*>.getTypeIndex()
    = BetterP2P.proxy.getP2PFromClass(this.javaClass)?.index ?: TUNNEL_ANY

fun AEBasePart.setCustomName(value: Component?) {
    // FUCK YOUR MOM
    val field = AEBasePart::class.java.getDeclaredField("customName")
    field.isAccessible = true
    field.set(this, value)
}

fun P2PTunnelPart<*>.pleaseSetTheFuckingOutputState(output: Boolean) {
    val field = P2PTunnelPart::class.java.getDeclaredField("output")
    field.isAccessible = true
    field.set(this, output)
    host.markForSave()
}
