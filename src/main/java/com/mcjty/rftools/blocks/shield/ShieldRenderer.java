package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.Constants;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class ShieldRenderer extends DefaultISBRH {

    private void renderSolid(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
        int meta = 0;
        if (world != null) {
            meta = world.getBlockMetadata(x, y, z);
        }
//        ForgeDirection k = BlockTools.getOrientationHoriz(meta);

        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);
        tessellator.addTranslation(x, y, z);
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        addSide(block, tessellator, Constants.SIDE_NORTH, meta);
        addSide(block, tessellator, Constants.SIDE_SOUTH, meta);
        addSide(block, tessellator, Constants.SIDE_WEST, meta);
        addSide(block, tessellator, Constants.SIDE_EAST, meta);
        addSide(block, tessellator, Constants.SIDE_UP, meta);
        addSide(block, tessellator, Constants.SIDE_DOWN, meta);

        tessellator.addTranslation(-x, -y, -z);
    }

    private void renderTransparent(IBlockAccess world, int x, int y, int z, Block block, RenderBlocks renderer) {
//        int meta = 0;
//        if (world != null) {
//            meta = world.getBlockMetadata(x, y, z);
//        }
//        ForgeDirection k = BlockTools.getOrientationHoriz(meta);

        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);
        tessellator.addTranslation(x, y, z);
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));

        IIcon shieldIcon = ModBlocks.shieldBlock.getShieldIcon();

        for (int dx = 0 ; dx < 4 ; dx++) {
            for (int dy = 1 ; dy < 4 ; dy++) {
                addSideShifted(shieldIcon, tessellator, Constants.SIDE_NORTH, dx, dy, 0);
                addSideShifted(shieldIcon, tessellator, Constants.SIDE_SOUTH, dx, dy, 0);
                if (dx == 0) {
                    addSideShifted(shieldIcon, tessellator, Constants.SIDE_WEST, dx, dy, 0);
                }
                if (dx == 3) {
                    addSideShifted(shieldIcon, tessellator, Constants.SIDE_EAST, dx, dy, 0);
                }
            }
            addSideShifted(shieldIcon, tessellator, Constants.SIDE_UP, dx, 3, 0);
            addSideShifted(shieldIcon, tessellator, Constants.SIDE_DOWN, dx, 1, 0);
        }

        tessellator.addTranslation(-x, -y, -z);
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        if (ShieldBlock.currentPass == 0) {
            renderSolid(world, x, y, z, block, renderer);
        } else {
            renderTransparent(world, x, y, z, block, renderer);
        }
        return true;
    }

    @Override
    public int getRenderId() {
        return ShieldBlock.RENDERID_SHIELD;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }
}
