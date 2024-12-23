package dev.lasm.betterp2p

import appeng.api.parts.IPartItem
import appeng.core.definitions.AEParts
import appeng.core.definitions.ItemDefinition
import appeng.parts.p2p.*
import dev.architectury.platform.Platform
import dev.lasm.betterp2p.util.p2p.ClientTunnelInfo
import dev.lasm.betterp2p.util.p2p.TunnelInfo
import java.util.function.Supplier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

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

        registerModTunnel(
            def = { BuiltInRegistries.ITEM[ResourceLocation("appmek", "chemical_p2p_tunnel")] },
            type = typeId++,
            classType = "me.ramidzkh.mekae2.ae2.ChemicalP2PTunnelPart"
        )

        registerModTunnel(
            def = { BuiltInRegistries.ITEM[ResourceLocation("mae2", "pattern_p2p_tunnel")] },
            type = typeId++,
            classType = "stone.mae2.parts.p2p.PatternP2PTunnelPart"
        )

        if (Platform.isModLoaded("gtceu"))
            registerModTunnel(
                def = { BuiltInRegistries.ITEM[ResourceLocation("mae2", "eu_p2p_tunnel")] },
                type = typeId++,
                classType = "stone.mae2.parts.p2p.EUP2PTunnelPart"
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

    private fun registerModTunnel(def: Supplier<Item>, type: Int, classType: String) {
        try {
            val clazz = Class.forName(classType)
            val stack = ItemStack(def.get())
            if (!P2PTunnelPart::class.java.isAssignableFrom(clazz) || def.get() !is IPartItem<*>) {
                BetterP2P.logger.error(
                    "Found mod support {} but it's not a P2P tunnel, this indicates a mod update, please report it to BetterP2P repository",
                    classType
                )
            }

            val info = TunnelInfo(type, stack, clazz as Class<out P2PTunnelPart<*>>)
            tunnelTypes[clazz] = info
            tunnelIndices[type] = info
        } catch (e: ClassNotFoundException) {
            BetterP2P.logger.error("Mod support for {} not found", classType)
        } catch (e: NullPointerException) {
            BetterP2P.logger.error("Mod support for {} not found", classType)
        }
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

        registerModTunnel(
            def = { BuiltInRegistries.ITEM[ResourceLocation("appmek", "chemical_p2p_tunnel")] },
            type = typeId++,
            classType = "me.ramidzkh.mekae2.ae2.ChemicalP2PTunnelPart",
            icon = ResourceLocation("mekanism", "textures/block/block_osmium.png")
        )

        registerModTunnel(
            def = { BuiltInRegistries.ITEM[ResourceLocation("mae2", "pattern_p2p_tunnel")] },
            type = typeId++,
            classType = "stone.mae2.parts.p2p.PatternP2PTunnelPart",
            icon = ResourceLocation("ae2", "textures/block/pattern_provider.png")
        )

        if (Platform.isModLoaded("gtceu"))
            registerModTunnel(
                def = { BuiltInRegistries.ITEM[ResourceLocation("mae2", "eu_p2p_tunnel")] },
                type = typeId++,
                classType = "stone.mae2.parts.p2p.EUP2PTunnelPart",
                icon = ResourceLocation("minecraft", "textures/block/copper_block.png")
            )

        BetterP2P.logger.info("Registered tunnel types: {}", tunnelTypes.values.joinToString(","))
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

    private inline fun registerModTunnel(
        def: Supplier<Item>,
        type: Int,
        classType: String,
        icon: ResourceLocation
    ) {
        try {
            val clazz = Class.forName(classType)
            if (!P2PTunnelPart::class.java.isAssignableFrom(clazz)) {
                BetterP2P.logger.error(
                    "Found mod support {} but it's not a P2P tunnel, this indicates a mod update, please report it to BetterP2P repository",
                    classType
                )
            }
            val stack = ItemStack(def.get())
            val info = ClientTunnelInfo(type, stack, clazz as Class<out P2PTunnelPart<*>>) { icon }
            tunnelTypes[clazz] = info
            tunnelIndices[type] = info
        } catch (e: ClassNotFoundException) {
            BetterP2P.logger.error("Mod support for {} not found", classType)
        } catch (e: NullPointerException) {
            BetterP2P.logger.error("Mod support for {} not found", classType)
        }
    }
}
