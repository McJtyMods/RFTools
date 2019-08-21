package mcjty.rftools.blocks.teleporter;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ToolType;

import javax.annotation.Nullable;


public class MatterBoosterBlock extends Block {

    public MatterBoosterBlock() {
        super(Block.Properties.create(Material.IRON)
                .sound(SoundType.METAL)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
                .hardnessAndResistance(2.0f, 6.0f));
        setRegistryName("matter_booster");
//        setCreativeTab(RFTools.setup.getTab());
//        setDefaultState(this.blockState.getBaseState().withProperty(FACING, Direction.NORTH));
    }

//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        BlockPos pos = context.getPos();
        PlayerEntity placer = context.getPlayer();
        return super.getStateForPlacement(context).with(BlockStateProperties.FACING, getFacingFromEntity(pos, placer));
    }

    public static Direction getFacingFromEntity(BlockPos clickedBlock, LivingEntity entityIn) {
        if (MathHelper.abs((float) entityIn.posX - clickedBlock.getX()) < 2.0F && MathHelper.abs((float) entityIn.posZ - clickedBlock.getZ()) < 2.0F) {
            double d0 = entityIn.posY + entityIn.getEyeHeight();

            if (d0 - clickedBlock.getY() > 2.0D) {
                return Direction.UP;
            }

            if (clickedBlock.getY() - d0 > 0.0D) {
                return Direction.DOWN;
            }
        }

        return entityIn.getHorizontalFacing().getOpposite();
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.FACING);
    }
}
