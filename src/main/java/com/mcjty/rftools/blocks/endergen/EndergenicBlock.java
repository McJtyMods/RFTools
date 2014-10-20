package com.mcjty.rftools.blocks.endergen;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class EndergenicBlock extends GenericContainerBlock {

    private IIcon icon;
    private IIcon iconUp;

    public EndergenicBlock(Material material) {
        super(material, EndergenicTileEntity.class);
        setBlockName("endergenicBlock");
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_ENDERGENIC;
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        icon = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenic");
        iconUp = iconRegister.registerIcon(RFTools.MODID + ":" + "machineEndergenicUp");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return getIcon(side, 0);
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconUp;
        } else {
            return icon;
        }
    }

}
