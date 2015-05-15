package mcjty.rftools.blocks.storage;

import mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class RemoteStorageRenderer extends DefaultISBRH {

    @Override
    public int getRenderId() {
        return RemoteStorageBlock.RENDERID_REMOTESTORAGE;
    }

    private static final float OFFS = 0.005f;
    private static final float L0 = 0.19f;
    private static final float R0 = 0.45f;
    private static final float U0 = 0.68f;
    private static final float D0 = 0.815f;

    private static final Quad quadsBar0[] = new Quad[]{
            new Quad(new Vt(R0, -OFFS, 1 - U0), new Vt(L0, -OFFS, 1 - U0), new Vt(L0, -OFFS, 1 - D0), new Vt(R0, -OFFS, 1 - D0)),       // DOWN
            new Quad(new Vt(L0, 1 + OFFS, 1 - U0), new Vt(R0, 1 + OFFS, 1 - U0), new Vt(R0, 1 + OFFS, 1 - D0), new Vt(L0, 1 + OFFS, 1 - D0)),   // UP
            new Quad(new Vt(1 - L0, U0, -OFFS), new Vt(1 - R0, U0, -OFFS), new Vt(1 - R0, D0, -OFFS), new Vt(1 - L0, D0, -OFFS)),       // NORTH
            new Quad(new Vt(L0, U0, 1 + OFFS), new Vt(R0, U0, 1 + OFFS), new Vt(R0, D0, 1 + OFFS), new Vt(L0, D0, 1 + OFFS)),           // SOUTH
            new Quad(new Vt(-OFFS, U0, L0), new Vt(-OFFS, U0, R0), new Vt(-OFFS, D0, R0), new Vt(-OFFS, D0, L0)),               // WEST
            new Quad(new Vt(1 + OFFS, U0, 1 - L0), new Vt(1 + OFFS, U0, 1 - R0), new Vt(1 + OFFS, D0, 1 - R0), new Vt(1 + OFFS, D0, 1 - L0)),   // EAST
    };
    private static final float L1 = 0.19f;
    private static final float R1 = 0.45f;
    private static final float U1 = 0.38f;
    private static final float D1 = 0.51f;

    private static final Quad quadsBar1[] = new Quad[]{
            new Quad(new Vt(R1, -OFFS, 1 - U1), new Vt(L1, -OFFS, 1 - U1), new Vt(L1, -OFFS, 1 - D1), new Vt(R1, -OFFS, 1 - D1)),       // DOWN
            new Quad(new Vt(L1, 1 + OFFS, 1 - U1), new Vt(R1, 1 + OFFS, 1 - U1), new Vt(R1, 1 + OFFS, 1 - D1), new Vt(L1, 1 + OFFS, 1 - D1)),   // UP
            new Quad(new Vt(1 - L1, U1, -OFFS), new Vt(1 - R1, U1, -OFFS), new Vt(1 - R1, D1, -OFFS), new Vt(1 - L1, D1, -OFFS)),       // NORTH
            new Quad(new Vt(L1, U1, 1 + OFFS), new Vt(R1, U1, 1 + OFFS), new Vt(R1, D1, 1 + OFFS), new Vt(L1, D1, 1 + OFFS)),           // SOUTH
            new Quad(new Vt(-OFFS, U1, L1), new Vt(-OFFS, U1, R1), new Vt(-OFFS, D1, R1), new Vt(-OFFS, D1, L1)),               // WEST
            new Quad(new Vt(1 + OFFS, U1, 1 - L1), new Vt(1 + OFFS, U1, 1 - R1), new Vt(1 + OFFS, D1, 1 - R1), new Vt(1 + OFFS, D1, 1 - L1)),   // EAST
    };

    private static final float L2 = 1 - 0.45f;
    private static final float R2 = 1 - 0.19f;
    private static final float U2 = 0.68f;
    private static final float D2 = 0.815f;

    private static final Quad quadsBar2[] = new Quad[]{
            new Quad(new Vt(R2, -OFFS, 1 - U2), new Vt(L2, -OFFS, 1 - U2), new Vt(L2, -OFFS, 1 - D2), new Vt(R2, -OFFS, 1 - D2)),       // DOWN
            new Quad(new Vt(L2, 1 + OFFS, 1 - U2), new Vt(R2, 1 + OFFS, 1 - U2), new Vt(R2, 1 + OFFS, 1 - D2), new Vt(L2, 1 + OFFS, 1 - D2)),   // UP
            new Quad(new Vt(1 - L2, U2, -OFFS), new Vt(1 - R2, U2, -OFFS), new Vt(1 - R2, D2, -OFFS), new Vt(1 - L2, D2, -OFFS)),       // NORTH
            new Quad(new Vt(L2, U2, 1 + OFFS), new Vt(R2, U2, 1 + OFFS), new Vt(R2, D2, 1 + OFFS), new Vt(L2, D2, 1 + OFFS)),           // SOUTH
            new Quad(new Vt(-OFFS, U2, L2), new Vt(-OFFS, U2, R2), new Vt(-OFFS, D2, R2), new Vt(-OFFS, D2, L2)),               // WEST
            new Quad(new Vt(1 + OFFS, U2, 1 - L2), new Vt(1 + OFFS, U2, 1 - R2), new Vt(1 + OFFS, D2, 1 - R2), new Vt(1 + OFFS, D2, 1 - L2)),   // EAST
    };

    private static final float L3 = 1 - 0.45f;
    private static final float R3 = 1 - 0.19f;
    private static final float U3 = 0.38f;
    private static final float D3 = 0.51f;

    private static final Quad quadsBar3[] = new Quad[]{
            new Quad(new Vt(R3, -OFFS, 1 - U3), new Vt(L3, -OFFS, 1 - U3), new Vt(L3, -OFFS, 1 - D3), new Vt(R3, -OFFS, 1 - D3)),       // DOWN
            new Quad(new Vt(L3, 1 + OFFS, 1 - U3), new Vt(R3, 1 + OFFS, 1 - U3), new Vt(R3, 1 + OFFS, 1 - D3), new Vt(L3, 1 + OFFS, 1 - D3)),   // UP
            new Quad(new Vt(1 - L3, U3, -OFFS), new Vt(1 - R3, U3, -OFFS), new Vt(1 - R3, D3, -OFFS), new Vt(1 - L3, D3, -OFFS)),       // NORTH
            new Quad(new Vt(L3, U3, 1 + OFFS), new Vt(R3, U3, 1 + OFFS), new Vt(R3, D3, 1 + OFFS), new Vt(L3, D3, 1 + OFFS)),           // SOUTH
            new Quad(new Vt(-OFFS, U3, L3), new Vt(-OFFS, U3, R3), new Vt(-OFFS, D3, R3), new Vt(-OFFS, D3, L3)),               // WEST
            new Quad(new Vt(1 + OFFS, U3, 1 - L3), new Vt(1 + OFFS, U3, 1 - R3), new Vt(1 + OFFS, D3, 1 - R3), new Vt(1 + OFFS, D3, 1 - L3)),   // EAST
    };

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        boolean rc = renderer.renderStandardBlock(block, x, y, z);
        if (rc) {
            RemoteStorageTileEntity remoteStorageTileEntity = (RemoteStorageTileEntity) world.getTileEntity(x, y, z);

            ForgeDirection front = ModularStorageSetup.remoteStorageBlock.getOrientation(world.getBlockMetadata(x, y, z));
            Tessellator tessellator = Tessellator.instance;
            tessellator.addTranslation(x, y, z);
            tessellator.setBrightness(240);

            IIcon icon = ModularStorageSetup.remoteStorageBlock.getOverlayIcon();

            float u1 = icon.getMinU();
            float u2 = icon.getMaxU();
            u2 = u1 + (u2 - u1) / 2;

            float v1 = icon.getMinV();
            float v2 = icon.getMaxV();
            v2 = v1 + (v2 - v1) / 8;

            if (remoteStorageTileEntity.hasStorage(0)) {
                Quad quad = quadsBar0[front.ordinal()];
                tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
                tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
                tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
                tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);
            }
            if (remoteStorageTileEntity.hasStorage(1)) {
                Quad quad = quadsBar1[front.ordinal()];
                tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
                tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
                tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
                tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);
            }
            if (remoteStorageTileEntity.hasStorage(2)) {
                Quad quad = quadsBar2[front.ordinal()];
                tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
                tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
                tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
                tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);
            }
            if (remoteStorageTileEntity.hasStorage(3)) {
                Quad quad = quadsBar3[front.ordinal()];
                tessellator.addVertexWithUV(quad.v1.x, quad.v1.y, quad.v1.z, u1, v1);
                tessellator.addVertexWithUV(quad.v2.x, quad.v2.y, quad.v2.z, u1, v2);
                tessellator.addVertexWithUV(quad.v3.x, quad.v3.y, quad.v3.z, u2, v2);
                tessellator.addVertexWithUV(quad.v4.x, quad.v4.y, quad.v4.z, u2, v1);
            }

            tessellator.addTranslation(-x, -y, -z);
        }
        return rc;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
