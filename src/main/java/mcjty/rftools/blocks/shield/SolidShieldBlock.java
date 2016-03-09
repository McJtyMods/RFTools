package mcjty.rftools.blocks.shield;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolidShieldBlock extends AbstractShieldBlock {

    public static final PropertyInteger ICON_TOPDOWN = PropertyInteger.create("icontopdown", 0, 3);
    public static final PropertyInteger ICON_SIDE = PropertyInteger.create("iconside", 0, 3);

    @Override
    protected void init() {
        setRegistryName("solid_shield_block");
        setUnlocalizedName("solid_shield_block");
    }

    @Override
    protected void initTE() {
        GameRegistry.registerTileEntity(TickShieldBlockTileEntity.class, RFTools.MODID + "_" + getRegistryName());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TickShieldBlockTileEntity();
    }

    @Override
    public boolean isBlockNormalCube() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isFullBlock() {
        return false;
    }

    @Override
    public boolean isFullCube() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    @Override
    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos).getBlock();
        if (block == ShieldSetup.solidShieldBlock || block == ShieldSetup.noTickSolidShieldBlock) {
            return false;
        }
        return true;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x+z) & 0x1);
        return state.withProperty(ICON_TOPDOWN, topdown).withProperty(ICON_SIDE, side);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, ICON_TOPDOWN, ICON_SIDE);
    }
}
