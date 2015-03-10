package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ActivityProbeBlock extends Block {

    private IIcon icon;

    public ActivityProbeBlock() {
        super(Material.iron);
        setBlockName("activityProbeBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":machineActivityProbe");
    }

    @Override
    public int onBlockPlaced(World world, int x, int y, int z, int side, float sx, float sy, float sz, int meta) {
        int rc = super.onBlockPlaced(world, x, y, z, side, sx, sy, sz, meta);
        if (!world.isRemote) {
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
            DimensionInformation information = dimensionManager.getDimensionInformation(world.provider.dimensionId);
            if (information != null) {
                information.addProbe();
            }
            dimensionManager.save(world);
        }
        return rc;
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        super.breakBlock(world, x, y, z, block, meta);
        if (!world.isRemote) {
            RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
            DimensionInformation information = dimensionManager.getDimensionInformation(world.provider.dimensionId);
            if (information != null) {
                information.removeProbe();
            }
            dimensionManager.save(world);
        }
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
