package dev.lasm.betterp2p.network.packet

import net.minecraft.network.protocol.common.custom.CustomPacketPayload

sealed interface IMessage : CustomPacketPayload {}
interface IC2SMessage: IMessage {}
interface IS2CMessage: IMessage {}
