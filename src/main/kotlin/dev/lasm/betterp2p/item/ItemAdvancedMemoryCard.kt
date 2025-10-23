package dev.lasm.betterp2p.item

import appeng.api.networking.IInWorldGridNodeHost
import appeng.api.parts.IPartHost
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.client.ClientCache
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.*
import dev.lasm.betterp2p.util.p2p.getTypeIndex
import java.util.Optional
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

object ItemAdvancedMemoryCard :
    Item(Properties().stacksTo(1).component(BetterP2P.MEMORY_INFO, MemoryInfo())) {

    override fun appendHoverText(
        stack: ItemStack,
        context: TooltipContext,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val info = stack.components.get(BetterP2P.MEMORY_INFO.get())!!
        list.add(
            Component.translatable("gui.advanced_memory_card.mode.${info.mode.name.lowercase()}")
        )
    }

    override fun use(
        level: Level,
        player: Player,
        interactionHand: InteractionHand
    ): InteractionResultHolder<ItemStack> {
        if (player.isCrouching && !level.isClientSide) {
            ClientCache.clear()
            return InteractionResultHolder.success(player.getItemInHand(interactionHand))
        }
        return super.use(level, player, interactionHand)
    }

    override fun useOn(useOnContext: UseOnContext): InteractionResult {
        val w = useOnContext.level
        val player = useOnContext.player!!
        val stack = useOnContext.itemInHand
        val pos = useOnContext.clickedPos

        if (w.isClientSide) return InteractionResult.PASS

        val te = w.getBlockEntity(pos)
        if (te is IInWorldGridNodeHost && te is IPartHost) {
            val part = te.selectPartWorld(useOnContext.clickLocation).part ?: te.getPart(null)
            val grid = part?.gridNode?.grid ?: return InteractionResult.FAIL

            val info = stack.components.get(BetterP2P.MEMORY_INFO.get())!!
            val selectedEntry: Optional<P2PLocation>
            val type: Int
            if (part is P2PTunnelPart<*>) {
                type = part.getTypeIndex()
                selectedEntry = Optional.of(part.toLoc())
            } else {
                type = TUNNEL_ANY
                selectedEntry = Optional.empty()
            }
            val info1 = MemoryInfo(selectedEntry, info.frequency, info.mode, info.guiScale, type)
            stack.update(BetterP2P.MEMORY_INFO, MemoryInfo()) { info -> info1 }
            ModNetwork.initConnection(player, grid, info1)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }
}
