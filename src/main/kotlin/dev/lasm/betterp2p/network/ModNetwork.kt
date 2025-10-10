package dev.lasm.betterp2p.network

import appeng.api.networking.IGrid
import dev.architectury.networking.NetworkChannel
import dev.architectury.networking.NetworkManager
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.GridServerCache
import dev.lasm.betterp2p.network.data.MemoryInfo
import dev.lasm.betterp2p.network.packet.*
import java.util.*
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import java.util.function.Supplier

/** Network cooldown time in milliseconds */
const val NETWORK_CD = 250L

/** Mod network manager. Handles server <-> client communication. */
object ModNetwork {
    val channel = NetworkChannel.create(ResourceLocation.tryBuild(BetterP2P.MOD_ID, "networking_channel"))

    fun sendToServer(msg: IC2SMessage) {
        channel.sendToServer(msg);
    }

    fun sendToPlayer(sp: ServerPlayer, msg: IS2CMessage) {
        channel.sendToPlayer(sp, msg);
    }

    fun <T: IMessage> register(msg: Class<T>, fac: () -> T, consumer: (T, Supplier<NetworkManager.PacketContext>) -> Unit) {
        channel.register(msg, encoder, { buf: FriendlyByteBuf -> fac().also { it.fromBytes(buf) } }, consumer)
    }

    /** for client requests (changing viewed p2p) */
    val playerState: MutableMap<UUID, PlayerRequest> = Collections.synchronizedMap(WeakHashMap())

    /** Network Thread */
    private lateinit var networkWorker: ScheduledThreadPoolExecutor

    val encoder = { packet: IMessage, buf: FriendlyByteBuf -> packet.toBytes(buf) }

    fun registerNetwork() {
        register(
            S2COpenGui::class.java,
            ::S2COpenGui,
            ClientOpenGuiHandler
        )

        register(
            S2CUpdateP2P::class.java,
            ::S2CUpdateP2P,
            ClientUpdateP2PHandler
        )

        register(
            C2SLinkP2P::class.java,
            ::C2SLinkP2P,
            ServerLinkP2PHandler
        )
        register(
            C2SCloseGui::class.java,
            ::C2SCloseGui,
            ServerCloseGuiHandler
        )
        register(
            C2SUpdateMemoryInfo::class.java,
            ::C2SUpdateMemoryInfo,
            ServerUpdateMemoryInfoHandler
        )

        register(
            C2SRenameP2P::class.java,
            ::C2SRenameP2P,
            ServerRenameP2PTunnelHandler
        )
        register(
            C2SRefreshP2PList::class.java,
            ::C2SRefreshP2PList,
            ServerRefreshP2PListHandler
        )
        register(
            C2SUnlinkP2P::class.java,
            ::C2SUnlinkP2P,
            ServerUnlinkP2PHandler
        )
        register(
            C2STypeChange::class.java,
            ::C2STypeChange,
            ServerTypeChangeHandler
        )
        networkWorker =
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
    }

    /** Utility function that asks for a full refresh of a specific p2p type. */
    fun requestP2PList(player: Player, type: Int) {
        synchronized(playerState) {
            val playerState = playerState[player.uuid] ?: return
            val cache = playerState.gridCache

            cache.type = type
            if (playerState.updateReady + NETWORK_CD < System.currentTimeMillis()) {
                sendToPlayer(
                    player as ServerPlayer,
                    S2CUpdateP2P(cache.retrieveP2PList(), true)
                )
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule(
                    {
                        synchronized(ModNetwork.playerState) {
                            sendToPlayer(
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
                sendToPlayer(player as ServerPlayer, S2CUpdateP2P(cache.getP2PUpdates()))
                playerState.updateReady = System.currentTimeMillis() + NETWORK_CD
            } else if (!playerState.updatePending) {
                playerState.updatePending = true
                networkWorker.schedule(
                    {
                        synchronized(ModNetwork.playerState) {
                            sendToPlayer(
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
        /*
        MenuRegistry.openMenu(player, object : MenuProvider {
            override fun createMenu(
                id: Int,
                inventory: Inventory,
                player: Player
            ): AbstractContainerMenu {
                return AdvancedMemoryCardMenu(id, inventory).also { it.memoryInfo = info; it.infos = cache.retrieveP2PList() }
            }

            override fun getDisplayName(): Component {
                return Component.translatable("container.examplemod.example_menu")
            }
        })
        */
        sendToPlayer(player, S2COpenGui(cache.retrieveP2PList(), info))
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
