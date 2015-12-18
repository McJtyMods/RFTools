package mcjty.rftools.blocks.generator;


import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

public class CoalGeneratorBlock extends GenericRFToolsBlock<CoalGeneratorTileEntity, CoalGeneratorContainer, GuiCoalGenerator> {

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public CoalGeneratorBlock() {
        super(Material.iron, CoalGeneratorTileEntity.class, CoalGeneratorContainer.class, GuiCoalGenerator.class, "coalgenerator", true);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_COALGENERATOR;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        CoalGeneratorTileEntity te = (CoalGeneratorTileEntity) world.getTileEntity(pos);
        Boolean working = te.isWorking();
        return state.withProperty(WORKING, working);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, FACING, WORKING);
    }


}
