package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

public class TeleportBeamBlock extends Block {

    private IIcon icon;

    public TeleportBeamBlock(Material material) {
        super(material);
        setBlockName("teleportBeamBlock");
    }

    @Override
    public int getRenderType() {
        return super.getRenderType();
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTeleporter");
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
