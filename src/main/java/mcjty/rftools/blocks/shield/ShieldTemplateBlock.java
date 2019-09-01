package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.IStringSerializable;

import java.util.Collection;

public class ShieldTemplateBlock extends Block {

//    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

    public static Collection<IProperty<?>> getPropertyKeys(BlockState state) {
        return state.getProperties();
    }

    public static enum TemplateColor implements IStringSerializable {
        BLUE("blue"), RED("red"), GREEN("green"), YELLOW("yellow");

        private final String name;

        TemplateColor(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName() + "_blue", "inventory"));
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 1, new ModelResourceLocation(getRegistryName() + "_red", "inventory"));
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 2, new ModelResourceLocation(getRegistryName() + "_green", "inventory"));
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 3, new ModelResourceLocation(getRegistryName() + "_yellow", "inventory"));
//    }

    public static final EnumProperty<TemplateColor> COLOR = EnumProperty.<TemplateColor>create("color", TemplateColor.class);

    public ShieldTemplateBlock() {
        super(Properties.create(Material.GLASS));
        setRegistryName("shield_template_block");
//        setCreativeTab(RFTools.setup.getTab());
        setDefaultState(this.getDefaultState().with(COLOR, TemplateColor.BLUE));
    }

    // @todo 1.14
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    // @todo 1.14
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(COLOR).ordinal();
//    }
//
//    @Override
//    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
//        for (TemplateColor enumdyecolor : TemplateColor.values()) {
//            items.add(new ItemStack(this, 1, enumdyecolor.ordinal()));
//        }
//    }


    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(COLOR);
    }
}
