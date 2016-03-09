package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.shield.filters.*;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public abstract class AbstractShieldBlock extends Block implements ITileEntityProvider {

    public static final int META_ITEMS = 1;             // If set then blocked for items
    public static final int META_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int META_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int META_PLAYERS = 8;           // If set the blocked for (some) players


    public AbstractShieldBlock() {
        super(Material.glass);
        init();
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setCreativeTab(RFTools.tabRfTools);
        GameRegistry.registerBlock(this);
        initTE();
    }

    protected abstract void init();

    protected abstract void initTE();

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public boolean canEntityDestroy(IBlockAccess world, BlockPos pos, Entity entity) {
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
    public int getMobilityFlag() {
        return 2;
    }

    @Override
    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        int cdData = shieldBlockTileEntity.getCollisionData();

        if (cdData == 0) {
            // No collision for anything.
            return;
        }
        if ((cdData & META_HOSTILE) != 0) {
            if (entity instanceof IMob) {
                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
                    super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PASSIVE) != 0) {
            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
                    super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
                }
                return;
            }
        }
        if ((cdData & META_PLAYERS) != 0) {
            if (entity instanceof EntityPlayer) {
                if (checkPlayerCD(world, pos, (EntityPlayer) entity)) {
                    super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
                }
            }
        }
        if ((cdData & META_ITEMS) != 0) {
            if (!(entity instanceof EntityLivingBase)) {
                if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
                    super.addCollisionBoxesToList(world, pos, state, mask, list, entity);
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
                        } else if (name.equals(entity.getDisplayName())) {
                            return (filter.getAction() & ShieldFilter.ACTION_SOLID) != 0;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
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

//    @Override
//    public void registerBlockIcons(IIconRegister iconRegister) {
//        icon = iconRegister.registerIcon(RFTools.MODID + ":shieldtexture");
//        icons[0] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield0");
//        icons[1] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield1");
//        icons[2] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield2");
//        icons[3] = iconRegister.registerIcon(RFTools.MODID + ":shield/shield3");
//    }
//


    // Subclasses can call this to override the slightly more expensive version in this class.
    protected boolean blockShouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return super.shouldSideBeRendered(world, pos, side);
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
        BlockPos thispos = pos.offset(side.getOpposite());

        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(thispos);
        if (shieldBlockTileEntity == null) {
            return super.shouldSideBeRendered(world, pos, side);
        }
        Block block = shieldBlockTileEntity.getBlock();
        if (block == null) {
            return super.shouldSideBeRendered(world, pos, side);
        } else {
            return block.shouldSideBeRendered(world, pos, side);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new NoTickShieldBlockTileEntity();
    }
}
