package com.projecturanus.betterp2p.util

import appeng.api.util.AEColor
import appeng.parts.p2p.PartP2PTunnel
import appeng.util.Platform

val PartP2PTunnel<*>.colorCode: Array<AEColor> get() = Platform.p2p().toColors(this.frequency)
