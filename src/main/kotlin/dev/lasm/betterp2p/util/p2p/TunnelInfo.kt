package dev.lasm.betterp2p.util.p2p

import appeng.parts.p2p.P2PTunnelPart
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

/** Common tunnel info to be used on server */
open class TunnelInfo(
    val index: Int,
    val stack: ItemStack,
    val clazz: Class<out P2PTunnelPart<*>>
) {
    val dispName: Component =
        stack.displayName ?: Component.literal("<Unknown P2P Type>").withStyle(ChatFormatting.RED)
    override fun toString(): String {
        return "TunnelInfo(index=$index, stack=$stack, clazz=$clazz, dispName='${dispName.string}')"
    }
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
) : TunnelInfo(index, stack, clazz) {
    override fun toString(): String {
        return "ClientTunnelInfo(icon=${icon()}) ${super.toString()}"
    }
}
