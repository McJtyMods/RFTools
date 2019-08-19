package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolidShieldBlock extends AbstractShieldBlock {

    public static final PropertyInteger ICON_TOPDOWN = PropertyInteger.create("icontopdown", 0, 5);
    public static final PropertyInteger ICON_SIDE = PropertyInteger.create("iconside", 0, 5);

    public SolidShieldBlock(String registryName, String unlocName, boolean opaque) {
        super(registryName, unlocName, opaque);
    }

    @SideOnly(Side.CLIENT)
    public void initBlockColors() {
        Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if (pos != null && worldIn != null) {
                TileEntity te = worldIn.getTileEntity(pos);
                if (te instanceof NoTickShieldBlockTileEntity) {
                    NoTickShieldBlockTileEntity tileEntity = (NoTickShieldBlockTileEntity) te;
                    return tileEntity.getShieldColor();
                }
            }
            return 0xffffff;
        }, this);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TickShieldSolidBlockTileEntity();
    }

    @Override
    public boolean isFullBlock(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(BlockState state, IBlockAccess world, BlockPos pos, Direction side) {
        Block block = world.getBlockState(pos.offset(side)).getBlock();
        if (block instanceof SolidShieldBlock) {
            return false;
        }
        return true;
    }

    @Override
    public BlockState getActualState(BlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity te = world instanceof ChunkCache ? ((ChunkCache)world).getTileEntity(pos, Chunk.EnumCreateEntityType.CHECK) : world.getTileEntity(pos);
        if (te instanceof NoTickShieldBlockTileEntity) {
            NoTickShieldBlockTileEntity tileEntity = (NoTickShieldBlockTileEntity) te;
            ShieldRenderingMode mode = tileEntity.getShieldRenderingMode();
            if (mode == ShieldRenderingMode.MODE_TRANSP) {
                return state.withProperty(ICON_TOPDOWN, 4).withProperty(ICON_SIDE, 4);
            } else if (mode == ShieldRenderingMode.MODE_SOLID) {
                return state.withProperty(ICON_TOPDOWN, 5).withProperty(ICON_SIDE, 5);
            }
        }


        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x+z) & 0x1);
        return state.withProperty(ICON_TOPDOWN, topdown).withProperty(ICON_SIDE, side);
    }

    @Override
    public BlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(BlockState state) {
        return 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ICON_TOPDOWN, ICON_SIDE);
    }
}
