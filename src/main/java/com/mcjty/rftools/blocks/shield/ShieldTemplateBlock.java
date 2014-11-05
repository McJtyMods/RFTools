package com.mcjty.rftools.blocks.shield;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class ShieldTemplateBlock extends Block {

    private IIcon icon;

    public ShieldTemplateBlock(Material material) {
        super(material);
        setBlockName("shieldTemplateBlock");
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
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "shieldTemplate");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return icon;
    }
}
