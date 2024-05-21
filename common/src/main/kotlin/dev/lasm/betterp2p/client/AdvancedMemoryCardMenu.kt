package dev.lasm.betterp2p.client

import dev.lasm.betterp2p.BetterP2P
import dev.lasm.betterp2p.network.data.MemoryInfo
import dev.lasm.betterp2p.network.data.P2PInfo
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.item.ItemStack

class AdvancedMemoryCardMenu(id: Int, inv: Inventory?) :
    AbstractContainerMenu(BetterP2P.ADVANCED_MEMORY_CARD_MENU.get(), id) {
    var infos: List<P2PInfo> = emptyList()
    var memoryInfo: MemoryInfo = MemoryInfo()
    override fun quickMoveStack(player: Player, i: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun stillValid(player: Player): Boolean {
        return true
    }

    override fun addDataSlot(dataSlot: DataSlot): DataSlot {
        return super.addDataSlot(dataSlot)
    }

    override fun sendAllDataToRemote() {
        super.sendAllDataToRemote()
    }
}
