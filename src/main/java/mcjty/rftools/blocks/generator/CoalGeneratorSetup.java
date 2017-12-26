package mcjty.rftools.blocks.generator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorSetup {
    public static CoalGeneratorBlock coalGeneratorBlock;

    public static void init() {
        if(CoalGeneratorConfiguration.enabled)
            coalGeneratorBlock = new CoalGeneratorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(CoalGeneratorConfiguration.enabled)
            coalGeneratorBlock.initModel();
    }
}
