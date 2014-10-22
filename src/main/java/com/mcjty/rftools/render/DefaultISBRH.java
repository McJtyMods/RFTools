package com.mcjty.rftools.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public abstract class DefaultISBRH implements ISimpleBlockRenderingHandler {

    private static class Vt {
        public final int x;
        public final int y;
        public final int z;

        public Vt(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    private static class Quad {
        public final Vt v1;
        public final Vt v2;
        public final Vt v3;
        public final Vt v4;

        public Quad(Vt v1, Vt v2, Vt v3, Vt v4) {
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
        }
    }

    private static final Quad quads[] = new Quad[] {
            new Quad(new Vt(0, 0, 0), new Vt(1, 0, 0), new Vt(1, 0, 1), new Vt(0, 0, 1)),       // DOWN
            new Quad(new Vt(0, 1, 1), new Vt(1, 1, 1), new Vt(1, 1, 0), new Vt(0, 1, 0)),       // UP
            new Quad(new Vt(1, 0, 1), new Vt(1, 1, 1), new Vt(0, 1, 1), new Vt(0, 0, 1)),       // NORTH
            new Quad(new Vt(1, 1, 0), new Vt(1, 0, 0), new Vt(0, 0, 0), new Vt(0, 1, 0)),       // SOUTH
            new Quad(new Vt(0, 0, 1), new Vt(0, 1, 1), new Vt(0, 1, 0), new Vt(0, 0, 0)),       // WEST
            new Quad(new Vt(1, 0, 0), new Vt(1, 1, 0), new Vt(1, 1, 1), new Vt(1, 0, 1)),       // EAST
    };

    protected void addSide(Block block, Tessellator tessellator, int side, int meta) {
        IIcon c = block.getIcon(side, meta);
        float u1 = c.getMinU();
        float v1 = c.getMinV();
        float u2 = c.getMaxU();
        float v2 = c.getMaxV();
        Quad quad = quads[side];
        tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
        tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
        tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
        tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);
    }

    protected void addSideHeight(Block block, Tessellator tessellator, int side, int meta, double height) {
        IIcon c = block.getIcon(side, meta);
        float u1 = c.getMinU();
        float v1 = c.getMinV();
        float u2 = c.getMaxU();
        float v2 = c.getMaxV();

        Quad quad = quads[side];

        double y1 = quad.v1.y == 0 ? 0.0 : height;
        double y2 = quad.v2.y == 0 ? 0.0 : height;
        double y3 = quad.v3.y == 0 ? 0.0 : height;
        double y4 = quad.v4.y == 0 ? 0.0 : height;
        tessellator.addVertexWithUV(quad.v1.x, y1, quad.v1.z, u1, v1);
        tessellator.addVertexWithUV(quad.v2.x, y2, quad.v2.z, u1, v2);
        tessellator.addVertexWithUV(quad.v3.x, y3, quad.v3.z, u2, v2);
        tessellator.addVertexWithUV(quad.v4.x, y4, quad.v4.z, u2, v1);
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
        renderWorldBlock(null, 0, 0, 0, block, modelId, renderer);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        return false;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }
}
