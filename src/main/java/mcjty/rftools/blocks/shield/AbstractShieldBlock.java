package mcjty.rftools.blocks.shield;

import mcjty.lib.McJtyRegister;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.shield.filters.*;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public abstract class AbstractShieldBlock extends Block implements ITileEntityProvider {

    public static final int META_ITEMS = 1;             // If set then blocked for items
    public static final int META_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int META_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int META_PLAYERS = 8;           // If set the blocked for (some) players


    public AbstractShieldBlock() {
        super(Material.GLASS);
        init();
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setCreativeTab(RFTools.tabRfTools);
        McJtyRegister.registerLater(this, RFTools.instance, ItemBlock.class, null);
        initTE();
    }

    public static boolean activateBlock(Block block, World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    public static Collection<IProperty<?>> getPropertyKeys(IBlockState state) {
        return state.getPropertyKeys();
    }

    protected abstract void init();

    protected abstract void initTE();

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public EnumPushReaction getMobilityFlag(IBlockState state) {
        return EnumPushReaction.BLOCK;
    }

    public void clAddCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> list, Entity entity) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        int cdData = shieldBlockTileEntity.getCollisionData();

        if (cdData == 0) {
            // No collision for anything.
            return;
        }
        if ((cdData & META_HOSTILE) != 0) {
            if (entity instanceof IMob) {
                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                    clAddCollisionBoxToList(state, world, pos, entityBox, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PASSIVE) != 0) {
            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                    clAddCollisionBoxToList(state, world, pos, entityBox, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PLAYERS) != 0) {
            if (entity instanceof EntityPlayer) {
                if (checkPlayerCD(world, pos, (EntityPlayer) entity)) {
                    clAddCollisionBoxToList(state, world, pos, entityBox, list, entity);
                }
            }
        }
        if ((cdData & META_ITEMS) != 0) {
            if (!(entity instanceof EntityLivingBase)) {
                if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
                    clAddCollisionBoxToList(state, world, pos, entityBox, list, entity);
                }
                return;
            }
        }
    }

    private boolean checkEntityCD(World world, BlockPos pos, String filterName) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        BlockPos shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock);
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (filterName.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    }
                }
            }
        }
        return false;
    }


    private boolean checkPlayerCD(World world, BlockPos pos, EntityPlayer entity) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        BlockPos shieldBlock = shieldBlockTileEntity.getShieldBlock();
        if (shieldBlock != null) {
            ShieldTEBase shieldTileEntity = (ShieldTEBase) world.getTileEntity(shieldBlock);
            if (shieldTileEntity != null) {
                List<ShieldFilter> filters = shieldTileEntity.getFilters();
                for (ShieldFilter filter : filters) {
                    if (DefaultFilter.DEFAULT.equals(filter.getFilterName())) {
                        return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                    } else if (PlayerFilter.PLAYER.equals(filter.getFilterName())) {
                        PlayerFilter playerFilter = (PlayerFilter) filter;
                        String name = playerFilter.getName();
                        if ((name == null || name.isEmpty())) {
                            return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                        } else if (name.equals(entity.getName())) {
                            return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) {
            NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
            int cdData = shieldBlockTileEntity.getCollisionData();
            if ((cdData & META_ITEMS) == 0) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY-1, entity.posZ);
            }
        }

        // Possibly check for damage.
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos thispos, EnumFacing side) {
        BlockPos pos = thispos.offset(side);

        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(thispos);
        if (shieldBlockTileEntity == null) {
            return super.shouldSideBeRendered(state, world, pos, side);
        }
        Block block = shieldBlockTileEntity.getBlock();
        if (block == null) {
            return super.shouldSideBeRendered(state, world, pos, side);
        } else {
            return block.shouldSideBeRendered(state, world, pos, side);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldBlockTileEntity();
    }

    public void clAddInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, null, tooltip, null);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
        clAddInformation(stack, null, tooltip, false);  // @todo WRONG
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

    @SideOnly(Side.CLIENT)
    protected void clGetSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        super.getSubBlocks(tab, (NonNullList<ItemStack>) subItems);
    }
}
