package mcjty.rftools.blocks.shield;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class CamoShieldBlock extends AbstractShieldBlock {

    public static final String CAMO = "camo";
    public static final CamoProperty CAMOID = new CamoProperty("camoid");

    @Override
    @SideOnly(Side.CLIENT)
    public void initModel() {
        // To make sure that our ISBM model is chosen for all states we use this custom state mapper:
        StateMapperBase ignoreState = new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
                return CamoBakedModel.modelFacade;
            }
        };
        ModelLoader.setCustomStateMapper(this, ignoreState);
    }


    @Override
    protected void init() {
        setRegistryName("camo_shield_block");
        setUnlocalizedName("rftools.camo_shield_block");
    }

    @Override
    protected void initTE() {
//        GameRegistry.registerTileEntity(TickShieldSolidBlockTileEntity.class, RFTools.MODID + "_" + getRegistryName());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TickShieldSolidBlockTileEntity();
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true; // delegated to CamoBakedModel#getQuads
    }

    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        Block block = world.getBlockState(pos.offset(side)).getBlock();
        if (block == ShieldSetup.camoShieldBlock || block == ShieldSetup.noTickCamoShieldBlock) {
            return false;
        }
        return true;
    }

    @Nullable
    protected IBlockState getMimicBlock(IBlockAccess blockAccess, BlockPos pos) {
        TileEntity te = blockAccess.getTileEntity(pos);
        if (te instanceof NoTickShieldBlockTileEntity) {
            return ((NoTickShieldBlockTileEntity) te).getMimicBlock();
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public void initColorHandler(BlockColors blockColors) {
        blockColors.registerBlockColorHandler((state, world, pos, tintIndex) -> {
            IBlockState mimicBlock = getMimicBlock(world, pos);
            return mimicBlock != null ? blockColors.colorMultiplier(mimicBlock, world, pos, tintIndex) : -1;
        }, this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        IProperty<?>[] listedProperties = new IProperty[] { };
        IUnlistedProperty<?>[] unlistedProperties = new IUnlistedProperty[] { CAMOID };
        return new ExtendedBlockState(this, listedProperties, unlistedProperties);
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
        IBlockState mimicBlock = getMimicBlock(world, pos);
        if (mimicBlock != null) {
            return extendedBlockState.withProperty(CAMOID, new CamoBlockId(mimicBlock));
        } else {
            return extendedBlockState;
        }
    }
}
