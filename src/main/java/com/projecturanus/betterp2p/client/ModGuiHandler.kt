package com.projecturanus.betterp2p.client

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World
import net.minecraftforge.fml.common.network.IGuiHandler

object ModGuiHandler : IGuiHandler {
    override fun getServerGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? =
        when (ID) {
            else -> null
        }


    override fun getClientGuiElement(ID: Int, player: EntityPlayer?, world: World?, x: Int, y: Int, z: Int): Any? =
        when (ID) {
            0 -> GuiBetterMemoryCard()
            else -> null
        }
}
