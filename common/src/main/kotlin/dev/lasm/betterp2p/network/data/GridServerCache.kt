package dev.lasm.betterp2p.network.data

import appeng.api.networking.IGrid
import appeng.api.parts.IPartItem
import appeng.me.service.P2PService
import appeng.parts.p2p.P2PTunnelPart
import appeng.util.Platform
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.util.p2p.TunnelInfo
import dev.lasm.betterp2p.util.p2p.getTypeIndex
import dev.lasm.betterp2p.util.p2p.pleaseSetTheFuckingOutputState
import dev.lasm.betterp2p.util.p2p.setCustomName
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.player.Player
import java.util.*

/**
 * When the player uses the adv memory card, this is cached on the server side
 * to provide access to the Grid when the player performs actions in the GUI
 * Each player has a list of p2ps that will be sent. These are tracked in [listP2P].
 */
class GridServerCache(private val grid: IGrid, val player: Player, var type: Int) {
    /**
     * The P2P list. On init, this is the full list.
     */
    private val listP2P: MutableMap<P2PLocation, P2PTunnelPart<*>> = mutableMapOf()

    /**
     * The dirty P2P list. Updates are accumulated here and sent altogether.
     */
    private val dirtyP2P: MutableSet<P2PLocation> = mutableSetOf()

    init {
        rebuildList(type)
    }

    /**
     * Refreshes the global p2p list
     */
    private fun rebuildList(type: Int) {
        synchronized(listP2P) {
            listP2P.clear()
            dirtyP2P.clear()
            grid.machineClasses.forEach {
                // Find all P2P tunnels...
                if (P2PTunnelPart::class.java.isAssignableFrom(it) &&
                    (type == TUNNEL_ANY || BetterP2P.proxy.getP2PFromIndex(type)?.clazz == it)
                ) {
                    grid.getMachines(it).forEach { gridNode ->
                        val p2p = gridNode as P2PTunnelPart<*>
                        listP2P[p2p.toLoc()] = p2p
                    }
                }
            }
        }
    }

    /**
     * Refreshes and gets the p2p list of the targeted type
     * If [type] is [TUNNEL_ANY] or invalid, returns the full list; else filters the list to the
     * targeted type
     */
    fun retrieveP2PList(): List<P2PInfo> {
        rebuildList(type)

        return listP2P.values.mapNotNull {
            val index = BetterP2P.proxy.getP2PFromClass(it.javaClass)?.index ?: TUNNEL_ANY
            if (type == TUNNEL_ANY || index == type) {
                it.toInfo()
            } else null
        }
    }

    /**
     * Sets the entry to the p2p tunnel and marks it as dirty to be sent in the next network update.
     * Only p2ps of the currently targeted type can be shown
     */
    fun markDirty(key: P2PLocation, p2p: P2PTunnelPart<*>) {
        synchronized(listP2P) {
            if (type == TUNNEL_ANY || p2p.getTypeIndex() == type) {
                listP2P[key] = p2p
                dirtyP2P.add(key)
            }
        }
    }

    /**
     * Returns the list of P2Ps that are currently marked dirty, and clears the dirty list.
     */
    fun getP2PUpdates(): List<P2PInfo> {
        val result = dirtyP2P.mapNotNull {
            listP2P[it]?.toInfo()
        }

        dirtyP2P.clear()

        return result;
    }

    /**
     * Link the two P2P tunnels together. Returns the pair of P2P tunnels on success, or null otherwise.
     */
    fun linkP2P(inputIndex: P2PLocation, outputIndex: P2PLocation):
        Pair<P2PTunnelPart<*>, P2PTunnelPart<*>>? {
        // If these calls mess up we have bigger problems...
        val input = listP2P[inputIndex] ?: return null
        var output = listP2P[outputIndex] ?: return null

        //change type if necessary
        if (input.javaClass != output.javaClass) {
            output = changeP2PType(output, BetterP2P.proxy.getP2PFromClass(input.javaClass)!!)
                ?: return null
        }

        // Network loop
        if (input == output) {
            return null
        }

        var frequency = input.frequency
        val cache = P2PService.get(input.gridNode!!.grid)

        // Generate a new frequency if needed
        if (input.frequency == 0.toShort() || input.isOutput) {
            frequency = cache.newFrequency()
        }

        // If tunnel was already bound, unbind that one
        if (cache.getInput(frequency) != null) {
            val originalInput = cache.getInput(frequency)
            if (originalInput != input) {
                updateP2P(
                    originalInput.toLoc(),
                    originalInput,
                    frequency,
                    true,
                    input.customName
                )
            }
        }

        // Perform the link
        val inputResult: P2PTunnelPart<*> =
            updateP2P(inputIndex, input, frequency, false, input.customName)
        val outputResult: P2PTunnelPart<*> =
            updateP2P(outputIndex, output, frequency, true, input.customName)

        return inputResult to outputResult
    }

    fun unlinkP2P(p2pIndex: P2PLocation): P2PTunnelPart<*>? {
        val tunnel = listP2P[p2pIndex] ?: return null
        val oldFreq = tunnel.frequency
        if (oldFreq == 0.toShort()) {
            return tunnel
        }

        return updateP2P(p2pIndex, tunnel, 0.toShort(), false, tunnel.customName)
    }

    /**
     * Sets the p2p tunnel to the frequency, output, and custom name. Removes the old one and replaces it, which lets
     * AE2 trigger the Grid refresh for us (though we need to update the tunnels ourselves)
     */
    private fun updateP2P(
        key: P2PLocation,
        tunnel: P2PTunnelPart<*>,
        frequency: Short,
        output: Boolean,
        name: Component?
    ): P2PTunnelPart<*> {
        val service = P2PService.get(tunnel.mainNode.grid)

        tunnel.pleaseSetTheFuckingOutputState(output)
        tunnel.setCustomName(name)

        service.updateFreq(tunnel, frequency)

        Platform.notifyBlocksOfNeighbors(tunnel.blockEntity.level, tunnel.blockEntity.blockPos)
        return tunnel
    }

    /**
     * Converts one P2P into the type
     * @see P2PTunnelPart.onPartActivate
     */
    private fun changeP2PType(tunnel: P2PTunnelPart<*>, newType: TunnelInfo): P2PTunnelPart<*>? {
        if (BetterP2P.proxy.getP2PFromClass(tunnel.javaClass) == newType) {
            player.displayClientMessage(Component.translatable("gui.advanced_memory_card.error.same_type"),false)
            return null
        }

        val level = tunnel.blockEntity?.level
        if (level is ServerLevel) {
            var newBus: P2PTunnelPart<*>? = null
            level.server.executeBlocking {
                val oldOutput: Boolean = tunnel.isOutput
                val freq = tunnel.frequency
                // Regular checks
                Objects.requireNonNull(tunnel.blockEntity)
                if (newType.stack.item !is IPartItem<*>) {
                    BetterP2P.logger.error("Attempt to assign a invalid type {} to tunnel {}, this shouldn't happen!", newType, tunnel.blockEntity)
                    return@executeBlocking
                }
                val partItem = newType.stack.item as IPartItem<*>
                if (!P2PTunnelPart::class.java.isAssignableFrom(partItem.partClass)) {
                    BetterP2P.logger.error("Attempt to assign a invalid type {} to tunnel {}, this shouldn't happen!", partItem.partClass, tunnel.blockEntity)
                    return@executeBlocking
                }

                newBus = tunnel
                if (newBus!!.partItem !== partItem) {
                    val replaced = tunnel.host.replacePart(partItem, tunnel.side, player, InteractionHand.MAIN_HAND)!! as P2PTunnelPart<*>

                    replaced.pleaseSetTheFuckingOutputState(oldOutput)
                    replaced.onTunnelNetworkChange()

                    replaced.setFrequency(freq)
                    newBus = replaced
                }

                Platform.notifyBlocksOfNeighbors(tunnel.level, tunnel.blockEntity.blockPos)
            }
            return newBus
        }

        return tunnel
    }

    /**
     * Converts all connected P2Ps to a new type
     */
    fun changeAllP2Ps(p2p: P2PLocation, newType: TunnelInfo): Boolean {

        var tunnel = listP2P[p2p] ?: return false

        try {
            if (tunnel.isOutput && tunnel.getInput() != null) {
                tunnel = tunnel.getInput()!!
            }

            val outputs = tunnel.outputs.toMutableList()
            /* TODO: static p2p
            if (newType.clazz.superclass == P2PTunnelPartStatic::class.java) {
                val amt = outputs.size + 1
                var hasItems = 0
                for (stack in player.inventory.mainInventory) {
                    if (stack?.isItemEqual(newType.stack) == true) {
                        hasItems += stack.stackSize
                        if (hasItems >= amt) {
                            break
                        }
                    }
                }
                if (hasItems < amt) {
                    player.addChatMessage(ChatComponentTranslation("gui.advanced_memory_card.error.missing_items", amt, newType.stack.displayName))
                    return false
                }
            }
             */
            changeP2PType(tunnel, newType)
            for (o in outputs) {
                changeP2PType(o, newType)
            }
            return true
        } catch (e: Exception) {
            // :P
        }
        return false
    }

}
