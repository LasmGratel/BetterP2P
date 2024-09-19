package dev.lasm.betterp2p.client

import appeng.parts.BusCollisionHelper
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import dev.lasm.betterp2p.client.ClientCache.positions
import dev.lasm.betterp2p.client.ClientCache.selectedFacing
import dev.lasm.betterp2p.client.ClientCache.selectedPosition
import dev.lasm.betterp2p.item.ItemAdvancedMemoryCard
import java.util.*
import net.minecraft.client.Camera
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import org.lwjgl.opengl.GL11

object RenderBlockOutline {
    @JvmStatic
    val LINES_BEHIND_BLOCK: RenderType =
        RenderType.create(
            "lines_behind_block",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .setLineState(RenderStateShard.LineStateShard(OptionalDouble.empty()))
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setDepthTestState(RenderStateShard.DepthTestStateShard(">", GL11.GL_GREATER))
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET)
                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                .setCullState(RenderStateShard.NO_CULL)
                .createCompositeState(false)
        )

    fun showPartPlacementPreview(
        player: Player?,
        poseStack: PoseStack,
        buffers: MultiBufferSource,
        camera: Camera
    ) {
        val item = player?.mainHandItem
        if (item?.item is ItemAdvancedMemoryCard) {
            if (positions.isNotEmpty() || selectedPosition != null) {
                if (selectedPosition != null) {
                    val side = selectedFacing
                    val boxes = ArrayList<AABB>()
                    val bch = BusCollisionHelper(boxes, side, true)
                    bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0)
                    bch.addBox(3.0, 3.0, 13.0, 13.0, 13.0, 14.0)
                    bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
                    renderBoxes(
                        poseStack,
                        buffers,
                        camera,
                        selectedPosition,
                        boxes,
                        0x45,
                        0xDA,
                        0x75,
                        true // TODO We need a setting here
                    )
                    // 0x45DA75
                }
                // 0x66CCFF
                for (entry in positions.toList()) {
                    val side = entry.component2()
                    val boxes = ArrayList<AABB>()
                    val bch = BusCollisionHelper(boxes, side, true)
                    bch.addBox(5.0, 5.0, 12.0, 11.0, 11.0, 13.0)
                    bch.addBox(3.0, 3.0, 13.0, 13.0, 13.0, 14.0)
                    bch.addBox(2.0, 2.0, 14.0, 14.0, 14.0, 16.0)
                    renderBoxes(
                        poseStack,
                        buffers,
                        camera,
                        entry.component1(),
                        boxes,
                        0x66,
                        0xCC,
                        0xFF,
                        true // TODO We need a setting here
                    )
                }
            }
        }
    }

    fun renderBoxes(
        poseStack: PoseStack,
        buffers: MultiBufferSource,
        camera: Camera,
        pos: BlockPos?,
        boxes: List<AABB>,
        red: Int,
        green: Int,
        blue: Int,
        insideBlock: Boolean
    ) {
        val renderType = if (insideBlock) LINES_BEHIND_BLOCK else RenderType.lines()
        val buffer = buffers.getBuffer(renderType)
        val alpha = ((if (insideBlock) 0.6f else 0.8f) * 255.0f).toInt()

        for (box in boxes) {
            val shape = Shapes.create(box)

            val x = pos!!.x - camera.position.x
            val y = pos.y - camera.position.y
            val z = pos.z - camera.position.z

            val pose = poseStack.last()
            shape.forAllEdges { k: Double, l: Double, m: Double, n: Double, o: Double, p: Double ->
                var q = (n - k).toFloat()
                var r = (o - l).toFloat()
                var s = (p - m).toFloat()
                val t = Mth.sqrt(q * q + r * r + s * s)
                buffer
                    .vertex(pose.pose(), (k + x).toFloat(), (l + y).toFloat(), (m + z).toFloat())
                    .color(red, green, blue, alpha)
                    .normal(
                        pose.normal(),
                        t.let {
                            q /= it
                            q
                        },
                        t.let {
                            r /= it
                            r
                        },
                        t.let {
                            s /= it
                            s
                        }
                    )
                    .endVertex()
                buffer
                    .vertex(pose.pose(), (n + x).toFloat(), (o + y).toFloat(), (p + z).toFloat())
                    .color(red, green, blue, alpha)
                    .normal(pose.normal(), q, r, s)
                    .endVertex()
            }
        }
    }
}
