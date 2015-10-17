package mcjty.rftools.blocks.logic;

import mcjty.lib.varia.BlockTools;
import mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class LogicSlabRenderer extends DefaultISBRH {

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        int meta = 0;
        if (world != null) {
            meta = world.getBlockMetadata(x, y, z);
        }
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);

        int old = renderer.uvRotateTop;
        switch (k) {
            case DOWN:
                break;
            case UP:
                break;
            case NORTH:
                renderer.uvRotateTop = 0;
                break;
            case SOUTH:
                renderer.uvRotateTop = 3;
                break;
            case WEST:
                renderer.uvRotateTop = 2;
                break;
            case EAST:
                renderer.uvRotateTop = 1;
                break;
            case UNKNOWN:
                break;
        }
        boolean rc = renderer.renderStandardBlock(block, x, y, z);
        renderer.uvRotateTop = old;
        return rc;
    }

    @Override
    public int getRenderId() {
        return LogicSlabBlock.RENDERID_LOGICSLAB;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
