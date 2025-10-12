package dev.lasm.betterp2p.network

import appeng.api.networking.IGrid
import dev.lasm.betterp2p.client.AdvancedMemoryCardMenu
import dev.lasm.betterp2p.network.data.GridServerCache
import dev.lasm.betterp2p.network.data.MemoryInfo
import dev.lasm.betterp2p.network.packet.*
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.neoforged.neoforge.network.PacketDistributor
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent
import net.neoforged.neoforge.network.registration.HandlerThread

/** Network cooldown time in milliseconds */
const val NETWORK_CD = 250L

/** Mod network manager. Handles server <-> client communication. */
object ModNetwork {

    /** for client requests (changing viewed p2p) */
    val playerState: MutableMap<UUID, PlayerRequest> = Collections.synchronizedMap(WeakHashMap())

    /** Network Thread */
    private val networkWorker: ScheduledThreadPoolExecutor =
        ScheduledThreadPoolExecutor(
            1,
            ThreadFactory {
                val th = Thread(it)
                th.name = "BetterP2P-NetworkWorker"
                th.isDaemon = true
                th.priority = Thread.MIN_PRIORITY
                th
            }
        )

    fun registerNetwork(event: RegisterPayloadHandlersEvent) {
        val reg = event.registrar("1").executesOn(HandlerThread.NETWORK)
        reg.playToClient(S2COpenGui.TYPE, S2COpenGui.STREAM_CODEC, ClientOpenGuiHandler)
        reg.playToClient(S2CUpdateP2P.TYPE, S2CUpdateP2P.STREAM_CODEC, ClientUpdateP2PHandler)
        reg.playToServer(C2SLinkP2P.TYPE, C2SLinkP2P.STREAM_CODEC, ServerLinkP2PHandler)
        reg.playToServer(C2SCloseGui.TYPE, C2SCloseGui.STREAM_CODEC, ServerCloseGuiHandler)
        reg.playToServer(
            C2SUpdateMemoryInfo.TYPE,
            C2SUpdateMemoryInfo.STREAM_CODEC,
            ServerUpdateMemoryInfoHandler
        )
        reg.playToServer(C2SRenameP2P.TYPE, C2SRenameP2P.STREAM_CODEC, ServerRenameP2PTunnelHandler)
        reg.playToServer(
            C2SRefreshP2PList.TYPE,
            C2SRefreshP2PList.STREAM_CODEC,
            ServerRefreshP2PListHandler
        )
        reg.playToServer(C2SUnlinkP2P.TYPE, C2SUnlinkP2P.STREAM_CODEC, ServerUnlinkP2PHandler)
        reg.playToServer(
            C2SChangeP2PType.TYPE,
            C2SChangeP2PType.STREAM_CODEC,
            ServerTypeChangeHandler
        )
    }

    /** Utility function that asks for a full refresh of a specific p2p type. */
    fun requestP2PList(player: Player, type: Int) {
        synchronized(playerState) {
            val playerState = playerState[player.uuid] ?: return
            val cache = playerState.gridCache

            cache.type = type
            if (playerState.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                PacketDistributor.sendToPlayer(
                    player as ServerPlayer,
                    S2CUpdateP2P(cache.retrieveP2PList(), true)
                )
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule(
                    {
                        synchronized(ModNetwork.playerState) {
                            PacketDistributor.sendToPlayer(
                                player as ServerPlayer,
                                S2CUpdateP2P(cache.retrieveP2PList(), true)
                            )
                            playerState.updatePending = false
                        }
                    },
                    playerState.updateReady - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        }
    }

    /**
     * Utility function that asks for a p2p update. Multiple requests are bundled into 1. If dirty,
     * only an incremental update is sent.
     */
    fun requestP2PUpdate(player: Player) {
        synchronized(playerState) {
            val playerState = playerState[player.uuid] ?: return
            val cache = playerState.gridCache

            if (playerState.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                PacketDistributor.sendToPlayer(
                    player as ServerPlayer,
                    S2CUpdateP2P(cache.getP2PUpdates())
                )
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule(
                    {
                        synchronized(ModNetwork.playerState) {
                            PacketDistributor.sendToPlayer(
                                player as ServerPlayer,
                                S2CUpdateP2P(cache.getP2PUpdates())
                            )
                            playerState.updatePending = false
                        }
                    },
                    playerState.updateReady - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        }
    }

    /** Sets up a connection. */
    fun initConnection(player: Player, grid: IGrid, info: MemoryInfo) {
        val cache = GridServerCache(grid, player, info.type)

        playerState[player.uuid] = PlayerRequest(gridCache = cache)
        if (player !is ServerPlayer) return

        player.openMenu(
            object : MenuProvider {
                override fun createMenu(
                    id: Int,
                    inventory: Inventory,
                    player: Player
                ): AbstractContainerMenu {
                    return AdvancedMemoryCardMenu(id, inventory).also {
                        it.memoryInfo = info
                        it.infos = cache.retrieveP2PList()
                    }
                }

                override fun getDisplayName(): Component {
                    return Component.translatable("container.examplemod.example_menu")
                }
            }
        )
        PacketDistributor.sendToPlayer(player, S2COpenGui(cache.retrieveP2PList(), info))
    }

    fun removeConnection(player: Player) {
        playerState.remove(player.uuid)
    }

    fun stop() {
        networkWorker.shutdown()
    }
}

/** Keeps track of when to send network updates. */
data class PlayerRequest(
    internal var updatePending: Boolean = false,
    internal var updateReady: Long = System.currentTimeMillis(),
    val gridCache: GridServerCache
)
