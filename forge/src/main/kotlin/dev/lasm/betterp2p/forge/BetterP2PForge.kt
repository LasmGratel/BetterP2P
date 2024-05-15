package dev.lasm.betterp2p.forge

import dev.architectury.platform.forge.EventBuses
import dev.lasm.betterp2p.BetterP2P
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import thedarkcolour.kotlinforforge.forge.MOD_BUS

@Mod(BetterP2P.MOD_ID)
object BetterP2PForge {
    init {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(
            BetterP2P.MOD_ID,
            MOD_BUS
        )
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) { Runnable { BetterP2PForgeClient.init() } }

        BetterP2P.init()
    }
}
