package dev.lasm.betterp2p.item

import appeng.api.networking.IInWorldGridNodeHost
import appeng.api.parts.IPartHost
import appeng.parts.p2p.P2PTunnelPart
import dev.lasm.betterp2p.client.ClientCache
import dev.lasm.betterp2p.client.gui.widget.GuiScale
import dev.lasm.betterp2p.network.ModNetwork
import dev.lasm.betterp2p.network.data.*
import dev.lasm.betterp2p.util.p2p.getTypeIndex
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
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
    Item(
        Properties()
            .stacksTo(1)
            .`arch$tab`(
                ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation("ae2", "main"))
            )
    ) {

    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        list: MutableList<Component>,
        tooltipFlag: TooltipFlag
    ) {
        val info = getInfo(stack)
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

            val info = getInfo(stack)
            val type: Int
            if (part is P2PTunnelPart<*>) {
                type = part.getTypeIndex()
                info.selectedEntry = part.toLoc()
            } else {
                type = TUNNEL_ANY
                info.selectedEntry = null
            }
            info.type = type
            writeInfo(stack, info)
            ModNetwork.initConnection(player, grid, info)
            return InteractionResult.SUCCESS
        }

        return InteractionResult.PASS
    }

    fun getInfo(stack: ItemStack): MemoryInfo {
        if (stack.item != this)
            throw ClassCastException(
                "Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}"
            )

        // Initialize NBT if it isn't already a thing
        val compound = stack.orCreateTag
        if (!compound.contains("gui")) {
            compound.putByte("gui", GuiScale.DYNAMIC.ordinal.toByte())
        }
        if (!compound.contains("selectedIndex", Tag.TAG_COMPOUND.toInt())) {
            compound.put("selectedIndex", CompoundTag())
        }

        return MemoryInfo(
            selectedEntry = readP2PLocation(compound.getCompound("selectedIndex")),
            frequency = compound.getShort("frequency"),
            mode = BetterMemoryCardModes.values()[compound.getInt("mode")],
            guiScale = GuiScale.values()[compound.getByte("gui").toInt()]
        )
    }

    fun writeInfo(stack: ItemStack, info: MemoryInfo) {
        if (stack.item != this)
            throw ClassCastException(
                "Cannot cast ${stack.item.javaClass.name} to ${javaClass.name}"
            )

        val compound = stack.orCreateTag
        compound.put("selectedIndex", writeP2PLocation(info.selectedEntry))
        compound.putShort("frequency", info.frequency)
        compound.putInt("mode", info.mode.ordinal)
        compound.putByte("gui", info.guiScale.ordinal.toByte())
    }
}
