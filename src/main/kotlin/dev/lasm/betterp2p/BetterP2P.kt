package dev.lasm.betterp2p

import com.mojang.blaze3d.vertex.PoseStack
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.client.RenderBlockOutline
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.item.ItemAdvancedMemoryCard
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.MemoryInfo
import java.util.function.Supplier
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent
import net.neoforged.neoforge.client.event.RenderLevelStageEvent
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.registries.DeferredHolder
import net.neoforged.neoforge.registries.DeferredRegister
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.runForDist

@Mod(BetterP2P.MOD_ID)
object BetterP2P {
    val proxy: CommonProxy =
        runForDist({ Supplier { ClientProxy() } }, { Supplier { CommonProxy() } }).get()

    const val MOD_ID = "betterp2p"

    val logger: Logger = LogManager.getLogger(MOD_ID)

    val ITEMS: DeferredRegister<Item> = DeferredRegister.createItems(MOD_ID)
    val ADVANCED_MEMORY_CARD_ITEM: DeferredHolder<Item, ItemAdvancedMemoryCard> =
        ITEMS.register("advanced_memory_card", Supplier { ItemAdvancedMemoryCard })

    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(Registries.MENU, MOD_ID)
    val ADVANCED_MEMORY_CARD_MENU: DeferredHolder<MenuType<*>, MenuType<AdvancedMemoryCardMenu>> =
        MENUS.register(
            "advanced_memory_card",
            Supplier { MenuType(::AdvancedMemoryCardMenu, FeatureFlagSet.of()) }
        )

    val DATA_COMPONENTS: DeferredRegister.DataComponents =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID)
    val MEMORY_INFO: DeferredHolder<DataComponentType<*>, DataComponentType<MemoryInfo>> =
        DATA_COMPONENTS.registerComponentType("memory_info") { builder ->
            builder.persistent(MemoryInfo.CODEC).networkSynchronized(MemoryInfo.STREAM_CODEC)
        }

    init {
        ITEMS.register(MOD_BUS)
        MENUS.register(MOD_BUS)
        DATA_COMPONENTS.register(MOD_BUS)
        NeoForge.EVENT_BUS.addListener(::onPlayerQuit)
        NeoForge.EVENT_BUS.addListener(::onRenderLevelStage)
        MOD_BUS.addListener(ModNetwork::registerNetwork)
        MOD_BUS.addListener(::onRegisterMenuScreens)
        MOD_BUS.addListener(::onBuildCreativeModeTabContents)
        MOD_BUS.addListener(::onCommonSetup)
    }

    fun onCommonSetup(event: FMLCommonSetupEvent) {
        logger.info("Tunnels init")
        proxy.initTunnels()
    }

    fun onPlayerQuit(event: PlayerEvent.PlayerLoggedOutEvent) {
        ModNetwork.removeConnection(event.entity)
    }

    fun onBuildCreativeModeTabContents(event: BuildCreativeModeTabContentsEvent) {
        if (
            event.tabKey ==
                ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    ResourceLocation.fromNamespaceAndPath("ae2", "main")
                )
        )
            event.accept(ADVANCED_MEMORY_CARD_ITEM.get())
    }

    fun onRenderLevelStage(context: RenderLevelStageEvent) {
        if (context.stage != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return
        val level: ClientLevel? = Minecraft.getInstance().level
        val poseStack: PoseStack = context.poseStack
        val buffers: MultiBufferSource? = Minecraft.getInstance().renderBuffers().bufferSource()
        val camera: Camera = context.camera
        if (level == null || buffers == null) {
            return
        } else {
            RenderBlockOutline.showPartPlacementPreview(
                Minecraft.getInstance().player,
                poseStack,
                buffers,
                camera
            )
        }
    }

    fun onRegisterMenuScreens(event: RegisterMenuScreensEvent) {
        event.register(ADVANCED_MEMORY_CARD_MENU.get()) { menu, inv, name ->
            GuiAdvancedMemoryCard(menu)
        }
    }
}
