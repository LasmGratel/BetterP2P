package dev.lasm.betterp2p

import appeng.core.definitions.AEParts
import appeng.core.definitions.ItemDefinition
import appeng.parts.p2p.*
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import dev.lasm.betterp2p.util.p2p.TunnelInfo
import net.minecraft.resources.ResourceLocation

/** A proxy for the server */
open class CommonProxy {
    /**
     * Tunnels available in this instance. These are used to communicate p2p information between
     * server/client.
     */
    protected val tunnelTypes = mutableMapOf<Class<*>, TunnelInfo>()

    /** Same as above, but maps ints -> tunnel info. */
    protected val tunnelIndices = mutableMapOf<Int, TunnelInfo>()

    /** Discover what tunnels are available. */
    open fun initTunnels() {
        var typeId = 0
        registerTunnel(
            def = AEParts.ME_P2P_TUNNEL,
            type = typeId++,
            classType = MEP2PTunnelPart::class.java
        )
        registerTunnel(
            def = AEParts.FE_P2P_TUNNEL,
            type = typeId++,
            classType = FEP2PTunnelPart::class.java
        )
        registerTunnel(
            def = AEParts.REDSTONE_P2P_TUNNEL,
            type = typeId++,
            classType = RedstoneP2PTunnelPart::class.java
        )
        registerTunnel(
            def = AEParts.FLUID_P2P_TUNNEL,
            type = typeId++,
            classType = FluidP2PTunnelPart::class.java
        )
        registerTunnel(
            def = AEParts.ITEM_P2P_TUNNEL,
            type = typeId++,
            classType = ItemP2PTunnelPart::class.java
        )
        registerTunnel(
            def = AEParts.LIGHT_P2P_TUNNEL,
            type = typeId++,
            classType = LightP2PTunnelPart::class.java
        )
    }

    private fun registerTunnel(
        def: ItemDefinition<*>,
        type: Int,
        classType: Class<out P2PTunnelPart<*>>
    ) {
        val stack = def.stack(1)
        val info = TunnelInfo(type, stack, classType)
        tunnelTypes[classType] = info
        tunnelIndices[type] = info
    }

    fun getP2PFromIndex(index: Int): TunnelInfo? {
        if (tunnelTypes.isEmpty()) initTunnels()
        return tunnelIndices[index]
    }

    fun getP2PFromClass(clazz: Class<*>): TunnelInfo? {
        if (tunnelTypes.isEmpty()) initTunnels()
        return tunnelTypes[clazz]
    }

    fun getP2PTypeList(): List<TunnelInfo> {
        return tunnelIndices.values.toList()
    }
}

/** A proxy for the client */
class ClientProxy : CommonProxy() {

    /** Keeps a cache of icons to use in GUI. */
    override fun initTunnels() {
        var typeId = 0
        registerTunnel(
            def = AEParts.ME_P2P_TUNNEL,
            type = typeId++,
            classType = MEP2PTunnelPart::class.java,
            icon = ResourceLocation("ae2", "textures/block/quartz_block.png")
        )
        registerTunnel(
            def = AEParts.FE_P2P_TUNNEL,
            type = typeId++,
            classType = FEP2PTunnelPart::class.java,
            icon = ResourceLocation("minecraft", "textures/block/gold_block.png")
        )
        registerTunnel(
            def = AEParts.REDSTONE_P2P_TUNNEL,
            type = typeId++,
            classType = RedstoneP2PTunnelPart::class.java,
            icon = ResourceLocation("minecraft", "textures/block/redstone_block.png")
        )
        registerTunnel(
            def = AEParts.FLUID_P2P_TUNNEL,
            type = typeId++,
            classType = FluidP2PTunnelPart::class.java,
            icon = ResourceLocation("minecraft", "textures/block/lapis_block.png")
        )
        registerTunnel(
            def = AEParts.ITEM_P2P_TUNNEL,
            type = typeId++,
            classType = ItemP2PTunnelPart::class.java,
            icon = ResourceLocation("minecraft", "textures/block/hopper_outside.png")
        )
        registerTunnel(
            def = AEParts.LIGHT_P2P_TUNNEL,
            type = typeId++,
            classType = LightP2PTunnelPart::class.java,
            icon = ResourceLocation("minecraft", "textures/block/quartz_block_top.png")
        )
    }

    private inline fun registerTunnel(
        def: ItemDefinition<*>,
        type: Int,
        classType: Class<out P2PTunnelPart<*>>,
        icon: ResourceLocation
    ) {
        val stack = def.stack(1)
        val info = ClientTunnelInfo(type, stack, classType) { icon }
        tunnelTypes[classType] = info
        tunnelIndices[type] = info
    }
}
