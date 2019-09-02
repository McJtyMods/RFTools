package mcjty.rftools.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;



public class MachineFrame extends Block {
    public MachineFrame() {
        super(Block.Properties.create(Material.IRON));
        setRegistryName("machine_frame");
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }


    @Override
    public boolean isValidPosition(BlockState state, IWorldReader reader, BlockPos pos) {
        return false;
    }
}
