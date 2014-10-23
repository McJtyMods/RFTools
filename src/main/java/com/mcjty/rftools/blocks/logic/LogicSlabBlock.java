package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.render.ModRenderers;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * The superclass for all logic slabs in RFTools.
 */
public abstract class LogicSlabBlock extends GenericBlock {

    public LogicSlabBlock(Material material, String name, Class<? extends TileEntity> tileEntityClass) {
        super(material, tileEntityClass);
        setBlockName(name);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
    }


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        boolean wrenchUsed = testWrenchUsage(x, y, z, player);
        if (wrenchUsed) {
            int meta = world.getBlockMetadata(x, y, z);
            ForgeDirection dir = BlockTools.getOrientationHoriz(meta);
            dir = dir.getRotation(ForgeDirection.UP);
            world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientationHoriz(meta, dir), 2);
            return true;
        } else {
            return super.onBlockActivated(world, x, y, z, player, side, sx, sy, sz);
        }
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entityLivingBase, ItemStack itemStack) {
        ForgeDirection dir = BlockTools.determineOrientationHoriz(x, y, z, entityLivingBase);
        int meta = world.getBlockMetadata(x, y, z);
        int power = world.isBlockProvidingPowerTo(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, dir.ordinal());
        meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
        world.setBlockMetadataWithNotify(x, y, z, BlockTools.setOrientationHoriz(meta, dir), 2);
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        int power = world.isBlockProvidingPowerTo(x + k.offsetX, y + k.offsetY, z + k.offsetZ, k.getOpposite().ordinal());
        meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return ModRenderers.RENDERID_LOGICSLAB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }


    @Override
    public boolean isOpaqueCube() {
        return false;
    }



    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        if (side == k.ordinal()) {
            return BlockTools.getRedstoneSignalOut(meta) ? 15 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        int meta = blockAccess.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        if (side == k.getOpposite().ordinal()) {
            return BlockTools.getRedstoneSignalOut(meta) ? 15 : 0;
        } else {
            return 0;
        }
    }

    @Override
    public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconInd;
        } else {
            return iconSide;
        }
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        if (side == ForgeDirection.UP.ordinal()) {
            return iconInd;
        } else {
            return iconSide;
        }
    }

}
