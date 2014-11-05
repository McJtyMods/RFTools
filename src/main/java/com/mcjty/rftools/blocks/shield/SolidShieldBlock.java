package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class SolidShieldBlock extends AbstractShieldBlock {

    public SolidShieldBlock(Material material) {
        super(material);
        setBlockName("solidShieldBlock");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        ShieldBlockTileEntity shieldBlockTileEntity = (ShieldBlockTileEntity) blockAccess.getTileEntity(x, y, z);
        Block block = shieldBlockTileEntity.getBlock();
        if (block == null) {
            return icon;
        } else {
            return block.getIcon(blockAccess, x, y, z, side);
        }
    }
}
