package com.projecturanus.betterp2p.client.render;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Functions from this inner class are not authored by me (Sam Bassett aka Lothrazar) they are from BuildersGuides by
 *
 * @author Ipsis
 *
 *         All credit goes to author for this
 *
 *         Source code: https://github.com/Ipsis/BuildersGuides Source License https://github.com/Ipsis/BuildersGuides/blob/master/COPYING.LESSER
 *
 *         I used and modified two functions from this library https://github.com/Ipsis/BuildersGuides/blob/master/src/main/java/ipsis/buildersguides/util/RenderUtils.java
 *
 *
 */
@SuppressWarnings("serial")
public class ShadowRenderer {

    public static void renderBlockPos(BlockPos p, BlockPos center, double relX, double relY, double relZ, float red, float green, float blue) {
        if (p == null) {
            return;
        }
        renderBlockList(new ArrayList<BlockPos>() {

            {
                add(p);
            }
        }, center, relX, relY, relZ, red, green, blue);
    }

    public static void renderBlockPhantom(World world, final BlockPos pos, ItemStack stack, final double relX, final double relY, final double relZ,
                                          BlockPos target, boolean isSolid) {
        if (stack.getItem() instanceof ItemBlock) {
            ItemBlock ib = (ItemBlock) stack.getItem();
            IBlockState stateFromStack = ib.getBlock().getStateForPlacement(world, pos, EnumFacing.DOWN, pos.getX(), pos.getY(), pos.getZ(),
                stack.getItemDamage(), null, EnumHand.MAIN_HAND);
            renderBlockPhantom(world, pos, stateFromStack, relX, relY, relZ, target, isSolid);
        }
    }

    public static void renderBlockPhantom(World world, final BlockPos pos, IBlockState state, final double relX, final double relY, final double relZ, BlockPos target, boolean isSolid) {
        int xOffset = target.getX() - pos.getX();
        int yOffset = target.getY() - pos.getY();
        int zOffset = target.getZ() - pos.getZ();
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        //this first translate is to make relative to TE and everything
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F);
        RenderHelper.disableStandardItemLighting();
        if (isSolid == false) {
            GlStateManager.blendFunc(770, 775);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
        }
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        //move into frame and then back to zero - so world relative
        bufferBuilder.setTranslation(-0.5 - pos.getX() + xOffset, -.5 - pos.getY() + yOffset, -.5 - pos.getZ() + zOffset);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        //TODO: pos below is the targetPos, other rel and pos are TE
        blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, bufferBuilder, false);
        bufferBuilder.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    public static void renderBlockList(List<BlockPos> blockPosList, BlockPos center, double relX, double relY, double relZ, float red, float green, float blue) {
        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();
        // translate to center or te
        GlStateManager.translate(relX + 0.5F, relY + 0.5F, relZ + 0.5F);
        GlStateManager.disableLighting(); // so colors are correct
        GlStateManager.disableTexture2D(); // no texturing needed
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        float alpha = 0.5F;
        GlStateManager.color(red, green, blue, alpha);
        if (Minecraft.isAmbientOcclusionEnabled())
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
        else
            GlStateManager.shadeModel(GL11.GL_FLAT);
        for (BlockPos p : blockPosList) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                (center.getX() - p.getX()) * -1.0F,
                (center.getY() - p.getY()) * -1.0F,
                (center.getZ() - p.getZ()) * -1.0F);
            shadedCube(0.4F);
            GlStateManager.popMatrix();
        }
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    private static void shadedCube(float scale) {
        float size = 1.0F * scale;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        // Front - anticlockwise vertices
        // Back - clockwise vertices
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        // xy anti-clockwise - front
        worldRenderer.pos(-size, -size, size).endVertex();
        worldRenderer.pos(size, -size, size).endVertex();
        worldRenderer.pos(size, size, size).endVertex();
        worldRenderer.pos(-size, size, size).endVertex();
        // xy clockwise - back
        worldRenderer.pos(-size, -size, -size).endVertex();
        worldRenderer.pos(-size, size, -size).endVertex();
        worldRenderer.pos(size, size, -size).endVertex();
        worldRenderer.pos(size, -size, -size).endVertex();
        // anti-clockwise - left
        worldRenderer.pos(-size, -size, -size).endVertex();
        worldRenderer.pos(-size, -size, size).endVertex();
        worldRenderer.pos(-size, size, size).endVertex();
        worldRenderer.pos(-size, size, -size).endVertex();
        // clockwise - right
        worldRenderer.pos(size, -size, -size).endVertex();
        worldRenderer.pos(size, size, -size).endVertex();
        worldRenderer.pos(size, size, size).endVertex();
        worldRenderer.pos(size, -size, size).endVertex();
        // anticlockwise - top
        worldRenderer.pos(-size, size, -size).endVertex();
        worldRenderer.pos(-size, size, size).endVertex();
        worldRenderer.pos(size, size, size).endVertex();
        worldRenderer.pos(size, size, -size).endVertex();
        // clockwise - bottom
        worldRenderer.pos(-size, -size, -size).endVertex();
        worldRenderer.pos(size, -size, -size).endVertex();
        worldRenderer.pos(size, -size, size).endVertex();
        worldRenderer.pos(-size, -size, size).endVertex();
        tessellator.draw();
    }
}
