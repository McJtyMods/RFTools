package mcjty.rftools.blocks.shield;

import mcjty.rftools.blocks.shield.filters.DefaultFilter;
import mcjty.rftools.blocks.shield.filters.PlayerFilter;
import mcjty.rftools.blocks.shield.filters.ShieldFilter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public abstract class AbstractShieldBlock extends Block implements ITileEntityProvider {

    public static final int META_ITEMS = 1;             // If set then blocked for items
    public static final int META_PASSIVE = 2;           // If set the blocked for passive mobs
    public static final int META_HOSTILE = 4;           // If set the blocked for hostile mobs
    public static final int META_PLAYERS = 8;           // If set the blocked for (some) players
    public static final AxisAlignedBB COLLISION_BOX = new AxisAlignedBB(0.002, 0.002, 0.002, 0.998, 0.998, 0.998);

    public AbstractShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(Properties.create(Material.GLASS)
                .hardnessAndResistance(-1.0F, 3600000.0F)
                .noDrops()
            );
//        this.lightOpacity = opaque ? 255 : 0; // @todo 1.14
        setRegistryName(registryName);
    }


//    public static boolean activateBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
//        return block.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
//    }

    public static Collection<IProperty<?>> getPropertyKeys(BlockState state) {
        return state.getProperties();
    }

//    @SideOnly(Side.CLIENT)
//    public void initModel() {
//        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
//    }

    // @todo 1.14
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isBlockNormalCube(BlockState state) {
//        return false;
//    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    // @todo 1.14
//    @Override
//    public int quantityDropped(Random random) {
//        return 0;
//    }
//
//    @Override
//    public EnumPushReaction getMobilityFlag(BlockState state) {
//        return EnumPushReaction.BLOCK;
//    }
//
//    @Override
//    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockReader worldIn, BlockPos pos) {
//        return COLLISION_BOX;
//    }
//
//    @Override
//    public void addCollisionBoxToList(BlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> list, @Nullable Entity entity, boolean p_185477_7_) {
//        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
//        int cdData = shieldBlockTileEntity.getCollisionData();
//
//        if (cdData == 0) {
//            // No collision for anything.
//            return;
//        }
//        if ((cdData & META_HOSTILE) != 0) {
//            if (entity instanceof IMob) {
//                if (checkEntityCD(world, pos, HostileFilter.HOSTILE)) {
//                    super.addCollisionBoxToList(state, world, pos, entityBox, list, entity, p_185477_7_);
//                }
//                return;
//            }
//        }
//        if ((cdData & META_PASSIVE) != 0) {
//            if (entity instanceof IAnimals && !(entity instanceof IMob)) {
//                if (checkEntityCD(world, pos, AnimalFilter.ANIMAL)) {
//                    super.addCollisionBoxToList(state, world, pos, entityBox, list, entity, p_185477_7_);
//                }
//                return;
//            }
//        }
//        if ((cdData & META_PLAYERS) != 0) {
//            if (entity instanceof PlayerEntity) {
//                if (checkPlayerCD(world, pos, (PlayerEntity) entity)) {
//                    super.addCollisionBoxToList(state, world, pos, entityBox, list, entity, p_185477_7_);
//                }
//            }
//        }
//        if ((cdData & META_ITEMS) != 0) {
//            if (!(entity instanceof LivingEntity)) {
//                if (checkEntityCD(world, pos, ItemFilter.ITEM)) {
//                    super.addCollisionBoxToList(state, world, pos, entityBox, list, entity, p_185477_7_);
//                }
//                return;
//            }
//        }
//    }

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


    private boolean checkPlayerCD(World world, BlockPos pos, PlayerEntity entity) {
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
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(pos);
        if (!(entity instanceof LivingEntity)) {
            int cdData = shieldBlockTileEntity.getCollisionData();
            if ((cdData & META_ITEMS) == 0) {
                // Items should be able to pass through. We just move the entity to below this block.
                entity.setPosition(entity.posX, entity.posY-1, entity.posZ);
            }
        }

        shieldBlockTileEntity.handleDamage(entity);
    }

    // @todo 1.14
//    @Override
//    public boolean shouldSideBeRendered(BlockState state, IBlockReader world, BlockPos thispos, Direction side) {
//        BlockPos pos = thispos.offset(side);
//
//        NoTickShieldBlockTileEntity shieldBlockTileEntity = (NoTickShieldBlockTileEntity) world.getTileEntity(thispos);
//        if (shieldBlockTileEntity == null) {
//            return super.shouldSideBeRendered(state, world, pos, side);
//        }
//        BlockState mimic = shieldBlockTileEntity.getMimicBlock();
//        if (mimic == null) {
//            return super.shouldSideBeRendered(state, world, pos, side);
//        } else {
//            return mimic.getBlock().shouldSideBeRendered(state, world, pos, side);
//        }
//    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NoTickShieldBlockTileEntity();
    }
}
