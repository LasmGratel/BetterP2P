package dev.lasm.betterp2p

import dev.architectury.event.events.client.ClientLifecycleEvent
import dev.architectury.event.events.common.PlayerEvent
import dev.architectury.registry.menu.MenuRegistry
import dev.architectury.registry.registries.DeferredRegister
import dev.architectury.registry.registries.RegistrySupplier
import dev.architectury.utils.EnvExecutor
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.client.gui.GuiAdvancedMemoryCard
import dev.lasm.betterp2p.item.ItemAdvancedMemoryCard
import dev.lasm.betterp2p.network.ModNetwork
import java.util.function.Supplier
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.Item
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object BetterP2P {
    val proxy: CommonProxy =
        EnvExecutor.getEnvSpecific({ Supplier { ClientProxy() } }, { Supplier { CommonProxy() } })

    const val MOD_ID = "betterp2p"

    val logger: Logger = LogManager.getLogger(MOD_ID)

    val ITEMS: DeferredRegister<Item> = DeferredRegister.create(MOD_ID, Registries.ITEM)
    val ADVANCED_MEMORY_CARD_ITEM: RegistrySupplier<Item> =
        ITEMS.register("advanced_memory_card") { ItemAdvancedMemoryCard }

    val MENUS: DeferredRegister<MenuType<*>> = DeferredRegister.create(MOD_ID, Registries.MENU)
    val ADVANCED_MEMORY_CARD_MENU: RegistrySupplier<MenuType<AdvancedMemoryCardMenu>> =
        MENUS.register("advanced_memory_card") {
            MenuType(::AdvancedMemoryCardMenu, FeatureFlagSet.of())
        }

    fun init() {
        PlayerEvent.PLAYER_QUIT.register { ModNetwork.removeConnection(it) }
        ITEMS.register()
        MENUS.register()
        ModNetwork.registerNetwork()
    }

    fun initClient() {
        ClientLifecycleEvent.CLIENT_SETUP.register {
            MenuRegistry.registerScreenFactory(ADVANCED_MEMORY_CARD_MENU.get()) { menu, inv, name ->
                GuiAdvancedMemoryCard(menu)
            }
        }
    }
}
