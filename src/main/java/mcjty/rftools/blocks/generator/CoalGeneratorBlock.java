package mcjty.rftools.blocks.generator;


import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.GenericRFToolsBlock;
import net.minecraft.block.material.Material;

public class CoalGeneratorBlock extends GenericRFToolsBlock<CoalGeneratorTileEntity, CoalGeneratorContainer, GuiCoalGenerator> {

    public CoalGeneratorBlock() {
        super(Material.iron, CoalGeneratorTileEntity.class, CoalGeneratorContainer.class, GuiCoalGenerator.class, "coalgenerator", true);
    }

    @Override
    public int getGuiID() {
        return RFTools.GUI_COALGENERATOR;
    }

}
