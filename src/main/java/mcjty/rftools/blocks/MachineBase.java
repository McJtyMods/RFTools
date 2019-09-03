package mcjty.rftools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;



public class MachineBase extends Block {
    public MachineBase() {
        super(Block.Properties.create(Material.IRON));
        setRegistryName("machine_base");
//        setCreativeTab(RFTools.setup.getTab());
    }

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.3F, 1.0F);

    // @todo 1.14
//    @Override
//    public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos) {
//        return BLOCK_AABB;
//    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public boolean isValidPosition(BlockState state, IWorldReader reader, BlockPos pos) {
        return false;
    }
}
