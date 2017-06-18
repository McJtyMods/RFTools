package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class ShieldTemplateBlock extends Block {

    public static boolean activateBlock(Block block, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    public static Collection<IProperty<?>> getPropertyKeys(IBlockState state) {
        return state.getPropertyKeys();
    }

    public void clAddInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, null, tooltip, null);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        clAddInformation(stack, null, tooltip, false);  // @todo WRONG
    }

    protected void clAddCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> list, Entity entity) {
        super.addCollisionBoxToList(state, world, pos, entityBox, list, entity, false);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean p_185477_7_) {
        clAddCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn);
    }

    protected void clOnNeighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {

    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos p_189540_5_) {
        clOnNeighborChanged(state, worldIn, pos, blockIn);
    }

    protected boolean clOnBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return clOnBlockActivated(worldIn, pos, state, playerIn, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return clGetStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    protected IBlockState clGetStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> tab) {
        clGetSubBlocks(Item.getItemFromBlock(this), itemIn, tab);
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

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName() + "_blue", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 1, new ModelResourceLocation(getRegistryName() + "_red", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 2, new ModelResourceLocation(getRegistryName() + "_green", "inventory"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 3, new ModelResourceLocation(getRegistryName() + "_yellow", "inventory"));
    }

    public static final PropertyEnum<TemplateColor> COLOR = PropertyEnum.<TemplateColor>create("color", TemplateColor.class);

    public ShieldTemplateBlock() {
        super(Material.GLASS);
        setUnlocalizedName("rftools.shield_template_block");
        setRegistryName("shield_template_block");
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.register(this);
        GameRegistry.register(new ShieldTemplateItemBlock(this), getRegistryName());
        setDefaultState(this.blockState.getBaseState().withProperty(COLOR, TemplateColor.BLUE));
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(COLOR).ordinal();
    }

    @SideOnly(Side.CLIENT)
    protected void clGetSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (TemplateColor enumdyecolor : TemplateColor.values()) {
            subItems.add(new ItemStack(itemIn, 1, enumdyecolor.ordinal()));
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLOR, TemplateColor.values()[meta & 3]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLOR).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, COLOR);
    }

    public static class ShieldTemplateItemBlock extends ItemBlock {
        public ShieldTemplateItemBlock(Block block) {
            super(block);
        }

        @Override
        public int getMetadata(int damage) {
            return damage;
        }
    }
}
