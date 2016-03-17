package mcjty.rftools.blocks.logic;

import mcjty.lib.container.GenericItemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import static mcjty.rftools.blocks.logic.LogicFacing.*;

public class LogicItemBlock extends GenericItemBlock {

    public LogicItemBlock(Block block) {
        super(block);
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState) {
        boolean rc = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicTileEntity) {
            float dx = Math.abs(0.5f - hitX);
            float dy = Math.abs(0.5f - hitY);
            float dz = Math.abs(0.5f - hitZ);

            side = side.getOpposite();
//            System.out.println("LogicItemBlock.placeBlockAt");
//            System.out.println("  side = " + side);
            LogicFacing facing;
            switch (side) {
                case DOWN:
                    if (dx < dz) {
                        facing = hitZ < 0.5 ? DOWN_TOSOUTH : DOWN_TONORTH;
                    } else {
                        facing = hitX < 0.5 ? DOWN_TOEAST : DOWN_TOWEST;
                    }
                    break;
                case UP:
                    if (dx < dz) {
                        facing = hitZ < 0.5 ? UP_TOSOUTH : UP_TONORTH;
                    } else {
                        facing = hitX < 0.5 ? UP_TOEAST : UP_TOWEST;
                    }
                    break;
                case NORTH:
                    if (dx < dy) {
                        facing = hitY < 0.5 ? NORTH_TOUP : NORTH_TODOWN;
                    } else {
                        facing = hitX < 0.5 ? NORTH_TOEAST : NORTH_TOWEST;
                    }
                    break;
                case SOUTH:
                    if (dx < dy) {
                        facing = hitY < 0.5 ? SOUTH_TOUP : SOUTH_TODOWN;
                    } else {
                        facing = hitX < 0.5 ? SOUTH_TOEAST : SOUTH_TOWEST;
                    }
                    break;
                case WEST:
                    if (dy < dz) {
                        facing = hitZ < 0.5 ? WEST_TOSOUTH : WEST_TONORTH;
                    } else {
                        facing = hitY < 0.5 ? WEST_TOUP : WEST_TODOWN;
                    }
                    break;
                case EAST:
                    if (dy < dz) {
                        facing = hitZ < 0.5 ? EAST_TOSOUTH : EAST_TONORTH;
                    } else {
                        facing = hitY < 0.5 ? EAST_TOUP : EAST_TODOWN;
                    }
                    break;
                default:
                    facing = DOWN_TOWEST;
                    break;
            }
//            System.out.println("  facing = " + facing);
//            System.out.println("  facing.getOutputSide() = " + facing.getOutputSide());
            LogicTileEntity logicTileEntity = (LogicTileEntity) te;
            logicTileEntity.setFacing(facing);
            world.setBlockState(pos, newState.getBlock().getDefaultState().withProperty(LogicSlabBlock.META_INTERMEDIATE, facing.getMeta()), 3);
        }
        return rc;
    }
}
