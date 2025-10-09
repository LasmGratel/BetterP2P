package dev.lasm.betterp2p.forge

import dev.architectury.platform.hooks.EventBusesHooks
import dev.lasm.betterp2p.BetterP2P
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod

@Mod(BetterP2P.MOD_ID)
object BetterP2PForge {
    init {
        // Submit our event bus to let architectury register our content on the right time
        EventBusesHooks.getModEventBus(BetterP2P.MOD_ID)
        BetterP2P.init()
    }
}
