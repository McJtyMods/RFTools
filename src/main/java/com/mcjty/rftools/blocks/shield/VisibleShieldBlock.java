package com.mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class VisibleShieldBlock extends AbstractShieldBlock implements ITileEntityProvider {

    public VisibleShieldBlock(Material material) {
        super(material);
        setBlockName("visibleShieldBlock");
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }


    @Override
    public int getRenderBlockPass() {
        return 1;
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        CamoBlockShieldTileEntity camoBlockShieldTileEntity = (CamoBlockShieldTileEntity) blockAccess.getTileEntity(x, y, z);
        Block block = camoBlockShieldTileEntity.getBlock();
        if (block == null) {
            return icon;
        } else {
            return block.getIcon(blockAccess, x, y, z, side);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new CamoBlockShieldTileEntity();
    }

}
