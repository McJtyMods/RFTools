package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.Constants;
import com.mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class BeamRenderer extends DefaultISBRH {

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);
        tessellator.setBrightness(190);
        tessellator.addTranslation(x, y, z);
//
//        addSide(block, tessellator, Constants.SIDE_DOWN);
//        addSide(block, tessellator, Constants.SIDE_UP);
//        addSide(block, tessellator, Constants.SIDE_NORTH);
//        addSide(block, tessellator, Constants.SIDE_SOUTH);
//        addSide(block, tessellator, Constants.SIDE_WEST);
//        addSide(block, tessellator, Constants.SIDE_EAST);
        int meta = 0;
        if (world != null) {
            meta = world.getBlockMetadata(x, y, z);
        }

        addSideHeightWithRotation(block, tessellator, Constants.SIDE_NORTH, meta, 4, ForgeDirection.NORTH);
        addSideHeightWithRotation(block, tessellator, Constants.SIDE_SOUTH, meta, 4, ForgeDirection.SOUTH);
        addSideHeightWithRotation(block, tessellator, Constants.SIDE_WEST, meta, 4, ForgeDirection.NORTH);
        addSideHeightWithRotation(block, tessellator, Constants.SIDE_EAST, meta, 4, ForgeDirection.NORTH);

        tessellator.addTranslation(-x, -y, -z);
        return true;
    }

    @Override
    public int getRenderId() {
        return TeleportBeamBlock.RENDERID_BEAM;
    }
}
