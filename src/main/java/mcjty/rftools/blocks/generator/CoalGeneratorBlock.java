package mcjty.rftools.blocks.generator;


import mcjty.lib.container.GenericGuiContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorBlock extends GenericRFToolsBlock<CoalGeneratorTileEntity, CoalGeneratorContainer> {

    public static final PropertyBool WORKING = PropertyBool.create("working");

    public CoalGeneratorBlock() {
        super(Material.iron, CoalGeneratorTileEntity.class, CoalGeneratorContainer.class, "coalgenerator", true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Class<? extends GenericGuiContainer> getGuiClass() {
        return GuiCoalGenerator.class;
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
