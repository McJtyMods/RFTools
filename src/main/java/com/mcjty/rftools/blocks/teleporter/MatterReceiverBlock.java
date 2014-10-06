package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.container.GenericContainerBlock;
import com.mcjty.rftools.RFTools;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MatterReceiverBlock extends GenericContainerBlock {

    private IIcon iconSide;
    private IIcon iconBottom;

    public MatterReceiverBlock(Material material) {
        super(material, MatterReceiverTileEntity.class);
        setBlockName("matterReceiverBlock");
    }

    @Override
    public String getFrontIconName() {
        return null;
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_MATTER_RECEIVER;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        // We don't want what GenericContainerBlock does.
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
