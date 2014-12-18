package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class ActivityProbeBlock extends Block {

    protected IIcon icon;

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
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
