package com.projecturanus.betterp2p.capability

import com.projecturanus.betterp2p.item.BetterMemoryCardModes

data class MemoryInfo(var selectedIndex: Int = -1, var mode: BetterMemoryCardModes = BetterMemoryCardModes.OUTPUT)
