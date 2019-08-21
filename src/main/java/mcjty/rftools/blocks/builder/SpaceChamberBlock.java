package mcjty.rftools.blocks.builder;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;


public class SpaceChamberBlock extends Block {

    public SpaceChamberBlock() {
        super(Block.Properties.create(Material.IRON)
                .hardnessAndResistance(2.0f, 6.0f)
                .harvestLevel(0)
                .harvestTool(ToolType.PICKAXE)
                .sound(SoundType.METAL));
        setRegistryName("space_chamber");
//        setCreativeTab(RFTools.setup.getTab());
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

    // @todo 1.14
//    @Override
//    public boolean isBlockNormalCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.TRANSLUCENT;
//    }
}
