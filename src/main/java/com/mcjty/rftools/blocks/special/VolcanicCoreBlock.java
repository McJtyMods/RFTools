package com.mcjty.rftools.blocks.special;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class VolcanicCoreBlock extends Block implements ITileEntityProvider {

    private IIcon icon;

    public VolcanicCoreBlock() {
        super(Material.rock);
        setBlockName("volcanicCoreBlock");
        setCreativeTab(RFTools.tabRfTools);
        setHardness(50.0F);
        setResistance(2000.0F);
        setStepSound(soundTypePiston);
        setHarvestLevel("pickaxe", 3);
    }

    @Override
    public boolean isBurning(IBlockAccess world, int x, int y, int z) {
        return true;
    }

    @Override
    public int getLightValue() {
        return 15;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new VolcanicCoreTileEntity();
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":volcanicCore");
    }

    @Override
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
