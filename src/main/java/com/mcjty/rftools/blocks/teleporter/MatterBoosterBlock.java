package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class MatterBoosterBlock extends Block {

    private IIcon iconFront;
    private IIcon iconSide;

    public MatterBoosterBlock() {
        super(Material.iron);
        setBlockName("matterBoosterBlock");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconFront = iconRegister.registerIcon(RFTools.MODID + ":" + "machineMatterBooster");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        ForgeDirection dir = BlockTools.determineOrientation(x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientation(meta, dir), 2);
    }


    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientation(meta);
        if (side == k.ordinal()) {
            return iconFront;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.SOUTH.ordinal()) {
            return iconFront;
        } else {
            return iconSide;
        }
    }

}
