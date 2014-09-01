package com.mcjty.rftools.blocks.crafter;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CrafterBlock extends BlockContainer {

    private IIcon iconFront;
    private IIcon iconSide;

    public CrafterBlock(Material material) {
        super(material);
        setBlockName("crafterBlock");
    }

    @Override
    public TileEntity createNewTileEntity(World world, int i) {
        return null;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new CrafterBlockTileEntity();
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof CrafterBlockTileEntity)) {
            return true;
        }
        if (world.isRemote) {
            return true;
        }
        player.openGui(RFTools.instance, RFTools.GUI_CRAFTER, world, x, y, z);
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        int l = BlockTools.determineOrientation(world, x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, l), 2);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconFront = iconRegister.registerIcon(RFTools.MODID + ":" + "machineCrafter");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        int k = BlockTools.getOrientation(meta);
        if (side == k) {
            return iconFront;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
//        int k = getOrientation(meta);
        if (side == 3) {
            return iconFront;
        } else {
            return iconSide;
        }
    }

}
