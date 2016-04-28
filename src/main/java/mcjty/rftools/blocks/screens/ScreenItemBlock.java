package mcjty.rftools.blocks.screens;

import mcjty.lib.container.GenericBlock;
import mcjty.lib.container.GenericItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScreenItemBlock extends GenericItemBlock {
    public ScreenItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean rc = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        if (side == EnumFacing.DOWN || side == EnumFacing.UP) {
            return rc;
        }

        world.setBlockState(pos, newState.getBlock().getDefaultState().withProperty(GenericBlock.FACING, side), 3);
        return rc;
    }
}
