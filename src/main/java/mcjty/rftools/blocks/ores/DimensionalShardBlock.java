package mcjty.rftools.blocks.ores;

import mcjty.lib.compat.CompatBlock;
import mcjty.rftools.Achievements;
import mcjty.rftools.RFTools;
import mcjty.rftools.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class DimensionalShardBlock extends CompatBlock {

    public static enum OreType implements IStringSerializable {
        ORE_OVERWORLD("overworld"),
        ORE_NETHER("nether"),
        ORE_END("end");

        private final String name;

        OreType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }

    public static final PropertyEnum<OreType> ORETYPE = PropertyEnum.create("oretype", OreType.class);

    public DimensionalShardBlock() {
        super(Material.ROCK);
        setHardness(3.0f);
        setResistance(5.0f);
        setHarvestLevel("pickaxe", 2);
        setUnlocalizedName("rftools.dimensional_shard_ore");
        setRegistryName("dimensional_shard_ore");
        setLightLevel(0.5f);
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.register(this);
        GameRegistry.register(new DimensionalShardItemBlock(this), getRegistryName());
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "oretype=overworld"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 1, new ModelResourceLocation(getRegistryName(), "oretype=nether"));
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 2, new ModelResourceLocation(getRegistryName(), "oretype=end"));
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            for (int i = 0 ; i < 10 ; i++) {
                world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f, rand.nextGaussian() / 3.0f, rand.nextGaussian() / 3.0f, rand.nextGaussian() / 3.0f);
            }
        }
    }

    @Override
    protected void clGetSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        super.clGetSubBlocks(itemIn, tab, subItems);
        subItems.add(new ItemStack(this, 1, 0));
        subItems.add(new ItemStack(this, 1, 1));
        subItems.add(new ItemStack(this, 1, 2));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.dimensionalShardItem;
    }

    @Override
    public int quantityDropped(Random random) {
        return 2 + random.nextInt(3);
    }

    @Override
    public int quantityDroppedWithBonus(int bonus, Random random) {
        int j = random.nextInt(bonus + 2) - 1;
        if (j < 0) {
            j = 0;
        }

        return this.quantityDropped(random) * (j + 1);
    }

    private Random rand = new Random();

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        // @todo Check @@@@@@@@@@
        return rand.nextInt(7-3) + 3;
//        return MathHelper.getRandomIntegerInRange(rand, 3, 7);
    }

    @Override
    public void onBlockHarvested(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player) {
        super.onBlockHarvested(worldIn, pos, state, player);
        if (player != null) {
            // @todo achievements
//            Achievements.trigger(player, Achievements.specialOres);
        }
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(ORETYPE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ORETYPE, OreType.values()[meta]);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ORETYPE);
    }
}
