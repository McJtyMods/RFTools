package mcjty.rftools.blocks.builder;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public class SupportBlock extends Block {

    public static final int STATUS_OK = 0;
    public static final int STATUS_WARN = 1;
    public static final int STATUS_ERROR = 2;

    public SupportBlock() {
        super(Material.glass);
        setUnlocalizedName("support_block");
        setRegistryName("support_block");
        setCreativeTab(RFTools.tabRfTools);
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float sidex, float sidey, float sidez) {
        if (!world.isRemote) {
            // Find all connected blocks and remove them.
            Deque<BlockPos> todo = new ArrayDeque<>();
            todo.add(pos);
            removeBlock(world, todo);
        }
        return super.onBlockActivated(world, pos, state, player, side, sidex, sidey, sidez);
    }

    private void removeBlock(World world, Deque<BlockPos> todo) {
        while (!todo.isEmpty()) {
            BlockPos c = todo.pollFirst();
            int x = c.getX();
            int y = c.getY();
            int z = c.getZ();
            world.setBlockToAir(c);
            if (world.getBlockState(c.west()).getBlock() == this) {
                todo.push(c.west());
            }
            if (world.getBlockState(c.east()).getBlock() == this) {
                todo.push(c.east());
            }
            if (world.getBlockState(c.down()).getBlock() == this) {
                todo.push(c.down());
            }
            if (world.getBlockState(c.up()).getBlock() == this) {
                todo.push(c.up());
            }
            if (world.getBlockState(c.south()).getBlock() == this) {
                todo.push(c.south());
            }
            if (world.getBlockState(c.north()).getBlock() == this) {
                todo.push(c.north());
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        IBlockState state = blockAccess.getBlockState(pos);
        Block block = state.getBlock();
        IBlockState state2 = blockAccess.getBlockState(pos.offset(side));
        Block block2 = state2.getBlock();
        if (block.getMetaFromState(state) != block2.getMetaFromState(state2)) {
            return true;
        }

        if (block == this) {
            return false;
        }

        return block != this && super.shouldSideBeRendered(blockAccess, pos, side);
    }


}
