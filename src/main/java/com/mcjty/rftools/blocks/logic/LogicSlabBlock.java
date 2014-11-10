package com.mcjty.rftools.blocks.logic;

import com.mcjty.container.GenericBlock;
import com.mcjty.rftools.blocks.BlockTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * The superclass for all logic slabs in RFTools.
 */
public abstract class LogicSlabBlock extends GenericBlock {

    public static int RENDERID_LOGICSLAB;

    public LogicSlabBlock(Material material, String name, Class<? extends TileEntity> tileEntityClass) {
        super(material, tileEntityClass);
        setBlockName(name);
        setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);
        setHorizRotation(true);
    }


    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float sx, float sy, float sz) {
        return onBlockActivatedDefaultWrench(world, x, y, z, player);
    }

    /**
     * Returns the signal strength at one input of the block. Args: world, X, Y, Z, side
     */
    private int getInputStrength(World world, int x, int y, int z, int side) {
        int dir = Direction.facingToDirection[side];
        int xoffset = x + Direction.offsetX[dir];
        int zoffset = z + Direction.offsetZ[dir];
        int power = world.getIndirectPowerLevelTo(xoffset, y, zoffset, side);
        int wirePower = world.getBlock(xoffset, y, zoffset) == Blocks.redstone_wire ? world.getBlockMetadata(xoffset, y, zoffset) : 0;
        return power >= 15 ? power : Math.max(power, wirePower);
    }



    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        int power = getInputStrength(world, x, y, z, k.ordinal());
        meta = BlockTools.setRedstoneSignalIn(meta, power > 0);
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public int getRenderType() {
        return RENDERID_LOGICSLAB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        return null;
    }


    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    /*
    *  -1: UP
    *   0: NORTH
    *   1: EAST
    *   2: SOUTH
    *   3: WEST
*/

    @Override
    public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
        int meta = world.getBlockMetadata(x, y, z);
        ForgeDirection k = BlockTools.getOrientationHoriz(meta);
        switch (k) {
            case NORTH:
            case SOUTH:
                return side == 0 || side == 2;      // Can connect for north and south.
            case WEST:
            case EAST:
                return side == 1 || side == 3;      // Can connect for east and west.
            default:
                return false;
        }
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
