package com.projecturanus.betterp2p.config

import net.minecraftforge.common.config.Configuration

object BetterP2PConfig {
    val client = ClientConfig()

    fun loadConfig(config: Configuration) {
        config.load()
        client.autoRefresh = config.getBoolean("autoRefresh", "client", true, "Auto refresh the P2P status")
    }

    class ClientConfig {
        var autoRefresh: Boolean = true
    }
}
