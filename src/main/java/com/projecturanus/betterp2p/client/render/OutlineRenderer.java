package com.projecturanus.betterp2p.client.render;

import kotlin.Pair;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.ShaderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.Collection;

/**
 * Everything in this inner class is From a mod that has MIT license owned by romelo333 and maintained by McJty
 *
 * License is here: https://github.com/romelo333/notenoughwands1.8.8/blob/master/LICENSE
 *
 * Specific source of code from the GenericWand class:
 * https://github.com/romelo333/notenoughwands1.8.8/blob/20952f50e7c1ab3fd676ed3da302666295e3cac8/src/main/java/romelo333/notenoughwands/Items/GenericWand.java
 *
 * Version 1.1: Edited to support rotation
 */
public class OutlineRenderer {

    public static void renderOutlines(RenderWorldLastEvent evt, EntityPlayer p, Collection<BlockPos> coordinates, int r, int g, int b) {
        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();
        GlStateManager.pushAttrib();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        renderOutlines(coordinates, r, g, b, 4);
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    private static void renderOutlines(Collection<BlockPos> coordinates, int r, int g, int b, int thickness) {
        Tessellator tessellator = Tessellator.getInstance();
        //    net.minecraft.client.renderer.VertexBuffer
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        //    GlStateManager.color(r / 255.0f, g / 255.0f, b / 255.0f);
        GL11.glLineWidth(thickness);
        for (BlockPos coordinate : coordinates) {
            float x = coordinate.getX();
            float y = coordinate.getY();
            float z = coordinate.getZ();
            renderHighLightedBlocksOutline(buffer, x, y, z, r / 255.0f, g / 255.0f, b / 255.0f, 1.0f); // .02f
        }
        tessellator.draw();
    }

    public static void renderHighLightedBlocksOutline(BufferBuilder buffer, float mx, float my, float mz, float r, float g, float b, float a) {
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my + 1, mz).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx + 1, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my, mz + 1).color(r, g, b, a).endVertex();
        buffer.pos(mx, my + 1, mz + 1).color(r, g, b, a).endVertex();
    }


    public static void renderOutlinesWithFacing(RenderWorldLastEvent evt, EntityPlayer p, Collection<Pair<BlockPos, EnumFacing>> coordinates, int r, int g, int b) {

        double doubleX = p.lastTickPosX + (p.posX - p.lastTickPosX) * evt.getPartialTicks();
        double doubleY = p.lastTickPosY + (p.posY - p.lastTickPosY) * evt.getPartialTicks();
        double doubleZ = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * evt.getPartialTicks();
        GlStateManager.pushAttrib();
        GlStateManager.disableDepth();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.depthMask(false);

        renderOutlinesWithFacing(coordinates, -doubleX, -doubleY, -doubleZ, r, g, b, 4);
        GlStateManager.popAttrib();
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
    }

    private static void renderOutlinesWithFacing(Collection<Pair<BlockPos, EnumFacing>> coordinates, double x, double y, double z, int r, int g, int b, int thickness) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (Pair<BlockPos, EnumFacing> coordinate : coordinates) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            GL11.glLineWidth(thickness);
            BlockPos pos = coordinate.component1();
            EnumFacing facing = coordinate.component2();
            buffer.setTranslation(pos.getX(), pos.getY(), pos.getZ());

            renderHighLightedBlocksOutlineForFacing(buffer, r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
            buffer.setTranslation(0, 0, 0);
            GlStateManager.translate(pos.getX(), pos.getY(), pos.getZ());
            GlStateManager.translate(0.5, 0.5, 0.5);

            switch (facing) {
                case DOWN:
                    GL11.glRotated(90, 0, 1, 0);
                    GL11.glRotated(90, 0, 0, 1);
                    break;
                case UP:
                    GL11.glRotated(90, 0, 1, 0);
                    GL11.glRotated(270, 0, 0, 1);
                    break;
                case NORTH:
                    GL11.glRotated(-90, 0, 1, 0);
                    break;
                case SOUTH:
                    GL11.glRotated(90, 0, 1, 0);
                    break;
                case EAST:
                    GL11.glRotated(180, 0, 0, 1);
                    break;
                case WEST:
                    break;
            }
            int[] west_matrix = {1, 0, 0, 0, 1, 0, 0, 0, 1};
            int[] east_matrix = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            int[] north_matrix = {0, 0, -1, 0, 1, 0, 1, 0, 0};
            int[] south_matrix = {0, 0, 1, 0, 1, 0, -1, 0, 0};

            GlStateManager.translate(-0.5, -0.5, -0.5);
            GlStateManager.translate(-pos.getX(), -pos.getY(), -pos.getZ());
            tessellator.draw();
            GlStateManager.popMatrix();
        }

    }


    public static void renderHighLightedBlocksOutlineForFacing(BufferBuilder buffer,
                                                               float r, float g, float b, float a) {
        double minX = 0;
        double minY = 0.125;
        double minZ = 0.125;

        double maxX = 0.1875;
        double maxY = 0.875;
        double maxZ = 0.875;

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();

        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
    }
}
