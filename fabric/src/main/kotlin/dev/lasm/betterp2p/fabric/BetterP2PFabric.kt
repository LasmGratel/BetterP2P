package dev.lasm.betterp2p.fabric

import dev.lasm.betterp2p.BetterP2P
import net.fabricmc.api.ModInitializer

object BetterP2PFabric : ModInitializer {
    override fun onInitialize() {
        BetterP2P.init()
    }
}
