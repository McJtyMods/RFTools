package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SolidShieldBlock extends AbstractShieldBlock implements ITileEntityProvider {

    public SolidShieldBlock(Material material) {
        super(material);
        setBlockName("solidShieldBlock");
    }



    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        SolidShieldTileEntity solidShieldTileEntity = (SolidShieldTileEntity) blockAccess.getTileEntity(x, y, z);
        Block block = solidShieldTileEntity.getBlock();
        if (block == null) {
            return icon;
        } else {
            return block.getIcon(blockAccess, x, y, z, side);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new SolidShieldTileEntity();
    }
}
