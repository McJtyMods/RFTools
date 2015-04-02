package mcjty.rftools.blocks.spaceprojector;

import mcjty.rftools.RFTools;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class SpaceChamberBlock extends Block {
    public SpaceChamberBlock() {
        super(Material.iron);
        setHardness(2.0f);
        setStepSound(soundTypeMetal);
        setHarvestLevel("pickaxe", 0);
        setBlockName("spaceChamberBlock");
        setCreativeTab(RFTools.tabRfTools);
    }
}
