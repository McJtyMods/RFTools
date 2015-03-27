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

public class DestinationAnalyzerBlock extends Block {

    private IIcon iconInd;
    private IIcon iconSide;
    private IIcon iconTop;
    private IIcon iconBottom;

    public DestinationAnalyzerBlock() {
        super(Material.iron);
        setBlockName("destinationAnalyzerBlock");
        setCreativeTab(RFTools.tabRfTools);
        setHardness(2.0f);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
    }

    @Override
    public void registerBlockIcons(IIconRegister iconRegister) {
        iconInd = iconRegister.registerIcon(RFTools.MODID + ":" + "machineDestinationAnalyzer");
        iconSide = iconRegister.registerIcon(RFTools.MODID + ":" + "machineSide");
        iconTop = iconRegister.registerIcon(RFTools.MODID + ":" + "machineTop");
        iconBottom = iconRegister.registerIcon(RFTools.MODID + ":" + "machineBottom");
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
        if (iconInd != null && side == k.ordinal()) {
            return iconInd;
        } else if (iconTop != null && side == BlockTools.getTopDirection(k).ordinal()) {
            return iconTop;
        } else if (iconBottom != null && side ==  BlockTools.getBottomDirection(k).ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (iconInd != null && side == ForgeDirection.SOUTH.ordinal()) {
            return iconInd;
        } else if (iconTop != null && side == ForgeDirection.UP.ordinal()) {
            return iconTop;
        } else if (iconBottom != null && side == ForgeDirection.DOWN.ordinal()) {
            return iconBottom;
        } else {
            return iconSide;
        }
    }

}
