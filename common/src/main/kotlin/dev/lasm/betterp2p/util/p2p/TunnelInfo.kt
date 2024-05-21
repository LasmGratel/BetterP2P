package dev.lasm.betterp2p.util.p2p

import appeng.parts.p2p.P2PTunnelPart
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

/** Common tunnel info to be used on server */
open class TunnelInfo(
    val index: Int,
    val stack: ItemStack,
    val clazz: Class<out P2PTunnelPart<*>>
) {
    val dispName: String = stack.displayName.string ?: "§c<Unknown P2P Type>"
}

/**
 * Client tunnel info contains icon info too. Because textures are not loaded until after postInit,
 * we need to use a supplier, unfortunately.
 */
class ClientTunnelInfo(
    index: Int,
    stack: ItemStack,
    clazz: Class<out P2PTunnelPart<*>>,
    val icon: () -> ResourceLocation
) : TunnelInfo(index, stack, clazz)
