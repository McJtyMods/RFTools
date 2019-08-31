package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

import static mcjty.rftools.blocks.shield.ShieldSetup.TYPE_SHIELD_SOLID;


public class SolidShieldBlock extends AbstractShieldBlock {

    public static final IntegerProperty ICON_TOPDOWN = IntegerProperty.create("icontopdown", 0, 5);
    public static final IntegerProperty ICON_SIDE = IntegerProperty.create("iconside", 0, 5);

    public SolidShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(registryName, unlocName, opaque);
    }

    // @todo 1.14
//    public void initBlockColors() {
//        Minecraft.getInstance().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
//            if (pos != null && worldIn != null) {
//                TileEntity te = worldIn.getTileEntity(pos);
//                if (te instanceof NoTickShieldBlockTileEntity) {
//                    NoTickShieldBlockTileEntity tileEntity = (NoTickShieldBlockTileEntity) te;
//                    return tileEntity.getShieldColor();
//                }
//            }
//            return 0xffffff;
//        }, this);
//    }


    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TickShieldSolidBlockTileEntity(TYPE_SHIELD_SOLID);
    }

    // @todo 1.14
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    // @todo 1.14
//    @Override
//    public boolean shouldSideBeRendered(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
//        Block block = world.getBlockState(pos.offset(side)).getBlock();
//        if (block instanceof SolidShieldBlock) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public BlockState getActualState(BlockState state, IBlockReader world, BlockPos pos) {
//        TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
//        if (te instanceof NoTickShieldBlockTileEntity) {
//            NoTickShieldBlockTileEntity tileEntity = (NoTickShieldBlockTileEntity) te;
//            ShieldRenderingMode mode = tileEntity.getShieldRenderingMode();
//            if (mode == ShieldRenderingMode.MODE_TRANSP) {
//                return state.withProperty(ICON_TOPDOWN, 4).withProperty(ICON_SIDE, 4);
//            } else if (mode == ShieldRenderingMode.MODE_SOLID) {
//                return state.withProperty(ICON_TOPDOWN, 5).withProperty(ICON_SIDE, 5);
//            }
//        }
//
//
//        int x = pos.getX();
//        int y = pos.getY();
//        int z = pos.getZ();
//        int topdown = (z & 0x1) * 2 + (x & 0x1);
//        int side = (y & 0x1) * 2 + ((x+z) & 0x1);
//        return state.withProperty(ICON_TOPDOWN, topdown).withProperty(ICON_SIDE, side);
//    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ICON_TOPDOWN).add(ICON_SIDE);
    }
}
