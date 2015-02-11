package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.render.DefaultISBRH;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class SolidShieldBlockRenderer extends DefaultISBRH {
    @Override
    public int getRenderId() {
        return SolidShieldBlock.RENDERID_SHIELDBLOCK;
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setColorOpaque(255, 255, 255);
        tessellator.addTranslation(x, y, z);
//        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        tessellator.setBrightness(240);

//        int meta = 0;
//        if (world != null) {
//            meta = world.getBlockMetadata(x, y, z);
//        }

        IIcon[] icons = ((SolidShieldBlock) block).getIcons();
        IIcon icon = icons[(x+y+z) & 0x3];

        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.DOWN);
        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.UP);
        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.NORTH);
        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.SOUTH);
        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.WEST);
        addSideConditionally(world, x, y, z, block, tessellator, icon, ForgeDirection.EAST);

        tessellator.addTranslation(-x, -y, -z);
        return true;
    }

}
