package mcjty.rftools.blocks.storage;

import mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class ModularStorageRenderer extends DefaultISBRH {

    @Override
    public int getRenderId() {
        return ModularStorageBlock.RENDERID_MODULARSTORAGE;
    }

    private static final float OFFS = 0.005f;
    private static final float L = 0.19f;
    private static final float R = 0.25f;
    private static final float U = 0.32f;
    private static final float D = 0.82f;
    private static final Quad quadsBar[] = new Quad[] {
            new Quad(new Vt(R, -OFFS, 1-U), new Vt(L, -OFFS, 1-U), new Vt(L, -OFFS, 1-D), new Vt(R, -OFFS, 1-D)),       // DOWN
            new Quad(new Vt(L, 1+OFFS, 1-U), new Vt(R, 1+OFFS, 1-U), new Vt(R, 1+OFFS, 1-D), new Vt(L, 1+OFFS, 1-D)),   // UP
            new Quad(new Vt(1-L, U, -OFFS), new Vt(1-R, U, -OFFS), new Vt(1-R, D, -OFFS), new Vt(1-L, D, -OFFS)),       // NORTH
            new Quad(new Vt(L, U, 1+OFFS), new Vt(R, U, 1+OFFS), new Vt(R, D, 1+OFFS), new Vt(L, D, 1+OFFS)),           // SOUTH
            new Quad(new Vt(-OFFS, U, L), new Vt(-OFFS, U, R), new Vt(-OFFS, D, R), new Vt(-OFFS, D, L)),               // WEST
            new Quad(new Vt(1+OFFS, U, 1-L), new Vt(1+OFFS, U, 1-R), new Vt(1+OFFS, D, 1-R), new Vt(1+OFFS, D, 1-L)),   // EAST
    };

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        boolean rc = renderer.renderStandardBlock(block, x, y, z);
        if (rc) {
            ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) world.getTileEntity(x, y, z);
            int level = modularStorageTileEntity.getRenderLevel();
            if (level == -1) {
                return rc;      // No storage module.
            }

            ForgeDirection front = ModularStorageSetup.modularStorageBlock.getOrientation(world.getBlockMetadata(x, y, z));
            Tessellator tessellator = Tessellator.instance;
            tessellator.addTranslation(x, y, z);
            tessellator.setBrightness(240);

            IIcon icon = ModularStorageSetup.modularStorageBlock.getOverlayIcon();

            float pct = level / 7.0f;
            float u1 = icon.getMinU();
            float u2 = icon.getMaxU();
            u2 = u1 + (u2-u1) / 2;
            float du = (u2-u1) * (1-pct);
            u1 += du;
            u2 += du;

            float v1 = icon.getMinV();
            float v2 = icon.getMaxV();
            v2 = v1 + (v2-v1) / 8;

            Quad quad = quadsBar[front.ordinal()];
            tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
            tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
            tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
            tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);

            tessellator.addTranslation(-x, -y, -z);
        }
        return rc;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
