package com.projecturanus.betterp2p

import com.projecturanus.betterp2p.client.ModGuiHandler
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import net.minecraftforge.fml.common.network.NetworkRegistry
import org.apache.logging.log4j.Logger

const val MODID = "betterp2p"

@Mod(modid = MODID, modLanguageAdapter = "net.shadowfacts.forgelin.KotlinAdapter", dependencies = "required-after: appliedenergistics2")
object BetterP2P {
    lateinit var logger: Logger

    @Mod.EventHandler
    fun preInit(event: FMLPreInitializationEvent) {
        logger = event.modLog
        NetworkRegistry.INSTANCE.registerGuiHandler(this, ModGuiHandler)
    }
}
