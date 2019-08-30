package mcjty.rftools.blocks.screens;

import mcjty.lib.McJtyLib;
import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class ScreenHitBlock extends BaseBlock {

    public ScreenHitBlock() {
        super("screen_hitblock", new BlockBuilder()
                .properties(Properties.create(Material.IRON).hardnessAndResistance(-1.0F, 3600000.0F)
                    .sound(SoundType.METAL))
                .tileEntitySupplier(ScreenHitTileEntity::new));
    }

    @Override
    public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
        BlockPos screenPos = getScreenBlockPos(worldIn, pos);
        if(screenPos == null) return ItemStack.EMPTY;
        BlockState screenState = worldIn.getBlockState(screenPos);
        return screenState.getBlock().getItem(worldIn, screenPos, screenState);
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        BlockPos pos = data.getPos();
//        ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
//        int dx = screenHitTileEntity.getDx();
//        int dy = screenHitTileEntity.getDy();
//        int dz = screenHitTileEntity.getDz();
//        Block block = world.getBlockState(pos.add(dx, dy, dz)).getBlock();
//        if (block instanceof ScreenBlock) {
//            ((ScreenBlock) block).addProbeInfoScreen(mode, probeInfo, player, world, pos.add(dx, dy, dz));
//        }
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currenttip, accessor, config);
//        BlockPos pos = accessor.getPosition();
//        World world = accessor.getWorld();
//        ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
//        int dx = screenHitTileEntity.getDx();
//        int dy = screenHitTileEntity.getDy();
//        int dz = screenHitTileEntity.getDz();
//        BlockPos rpos = pos.add(dx, dy, dz);
//        BlockState state = world.getBlockState(rpos);
//        Block block = state.getBlock();
//        if (block instanceof ScreenBlock) {
//            TileEntity te = world.getTileEntity(rpos);
//            if (te instanceof ScreenTileEntity) {
//                RayTraceResult mouseOver = accessor.getMOP();
//                ScreenTileEntity screenTileEntity = (ScreenTileEntity) te;
//                ScreenTileEntity.ModuleRaytraceResult hit = screenTileEntity.getHitModule(mouseOver.hitVec.x - pos.getX() - dx, mouseOver.hitVec.y - pos.getY() - dy, mouseOver.hitVec.z - pos.getZ() - dz, mouseOver.sideHit, state.getValue(ScreenBlock.HORIZONTAL_FACING));
//                ((ScreenBlock) block).getWailaBodyScreen(currenttip, accessor.getPlayer(), screenTileEntity, hit);
//            }
//        }
//        return currenttip;
//    }

//    @Override
//    public void initModel() {
//        McJtyLib.proxy.initTESRItemStack(Item.getItemFromBlock(this), 0, ScreenTileEntity.class);
//        super.initModel();
//    }


    @Override
    public void onBlockClicked(BlockState s, World world, BlockPos pos, PlayerEntity player) {
        if (world.isRemote) {
            ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
            int dx = screenHitTileEntity.getDx();
            int dy = screenHitTileEntity.getDy();
            int dz = screenHitTileEntity.getDz();
            BlockState state = world.getBlockState(pos.add(dx, dy, dz));
            Block block = state.getBlock();
            if (block != ScreenSetup.screenBlock && block != ScreenSetup.creativeScreenBlock) {
                return;
            }

            RayTraceResult mouseOver = McJtyLib.proxy.getClientMouseOver();
            ScreenTileEntity screenTileEntity = (ScreenTileEntity) world.getTileEntity(pos.add(dx, dy, dz));
            if (mouseOver instanceof BlockRayTraceResult) {
                screenTileEntity.hitScreenClient(mouseOver.getHitVec().x - pos.getX() - dx, mouseOver.getHitVec().y - pos.getY() - dy, mouseOver.getHitVec().z - pos.getZ() - dz,
                        ((BlockRayTraceResult) mouseOver).getFace(), state.get(BlockStateProperties.HORIZONTAL_FACING));
            }
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        return activate(world, pos, state, player, hand, result);
    }

    public boolean activate(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, BlockRayTraceResult result) {
        pos = getScreenBlockPos(world, pos);
        if (pos == null) {
            return false;
        }
        Block block = world.getBlockState(pos).getBlock();
        return ((ScreenBlock) block).activate(world, pos, state, player, hand, result);
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rot) {
        // Doesn't make sense to rotate a potentially 3x3 screen,
        // and is incompatible with our special wrench actions.
        return state;
    }

    public BlockPos getScreenBlockPos(IBlockReader world, BlockPos pos) {
        ScreenHitTileEntity screenHitTileEntity = (ScreenHitTileEntity) world.getTileEntity(pos);
        int dx = screenHitTileEntity.getDx();
        int dy = screenHitTileEntity.getDy();
        int dz = screenHitTileEntity.getDz();
        pos = pos.add(dx, dy, dz);
        Block block = world.getBlockState(pos).getBlock();
        if (block != ScreenSetup.screenBlock && block != ScreenSetup.creativeScreenBlock) {
            return null;
        }
        return pos;
    }

    public static final AxisAlignedBB BLOCK_AABB = new AxisAlignedBB(0.5F - 0.5F, 0.0F, 0.5F - 0.5F, 0.5F + 0.5F, 1.0F, 0.5F + 0.5F);
    public static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 1.0F - 0.125F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.125F);
    public static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(1.0F - 0.125F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.125F, 1.0F, 1.0F);
    public static final AxisAlignedBB UP_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.125F, 1.0F);
    public static final AxisAlignedBB DOWN_AABB = new AxisAlignedBB(0.0F, 1.0F - 0.125F, 0.0F, 1.0F, 1.0F, 1.0F);

    // @todo 1.14
//    @Override
//    public AxisAlignedBB getBoundingBox(BlockState state, IBlockReader source, BlockPos pos) {
//        Direction facing = state.getValue(BaseBlock.FACING);
//        if (facing == Direction.NORTH) {
//            return NORTH_AABB;
//        } else if (facing == Direction.SOUTH) {
//            return SOUTH_AABB;
//        } else if (facing == Direction.WEST) {
//            return WEST_AABB;
//        } else if (facing == Direction.EAST) {
//            return EAST_AABB;
//        } else if (facing == Direction.UP) {
//            return UP_AABB;
//        } else if (facing == Direction.DOWN) {
//            return DOWN_AABB;
//        } else {
//            return BLOCK_AABB;
//        }
//    }
//
//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isBlockNormalCube(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isFullBlock(BlockState state) {
//        return false;
//    }
//
//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        return false;
    }

    @Override
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(BlockStateProperties.FACING);
    }

    // @todo 1.14
//    @Override
//    public int quantityDropped(Random random) {
//        return 0;
//    }


    @Override
    public PushReaction getPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }
}
