package mcjty.rftools.blocks.logic;

import mcjty.rftools.Constants;
import mcjty.rftools.blocks.BlockTools;
import mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class LogicSlabRenderer extends DefaultISBRH {

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);
        tessellator.addTranslation(x, y, z);
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        int meta = 0;
        if (world != null) {
            meta = world.getBlockMetadata(x, y, z);
        }
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);

        addSideHeight(block, tessellator, Constants.SIDE_NORTH, meta, .3);
        addSideHeight(block, tessellator, Constants.SIDE_SOUTH, meta, .3);
        addSideHeight(block, tessellator, Constants.SIDE_WEST, meta, .3);
        addSideHeight(block, tessellator, Constants.SIDE_EAST, meta, .3);
        addSideHeightWithRotation(block, tessellator, Constants.SIDE_UP, meta, .3, k);
        addSideHeight(block, tessellator, Constants.SIDE_DOWN, meta, .3);

        tessellator.addTranslation(-x, -y, -z);
        return true;
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
