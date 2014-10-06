package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class MatterReceiverBlock extends Block {

    private IIcon iconSide;
    private IIcon iconBottom;

    public MatterReceiverBlock(Material material) {
        super(material);
        setBlockName("matterReceiverBlock");
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + "machineReceiver");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

}
