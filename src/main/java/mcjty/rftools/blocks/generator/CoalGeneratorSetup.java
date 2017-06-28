package mcjty.rftools.blocks.generator;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CoalGeneratorSetup {
    public static CoalGeneratorBlock coalGeneratorBlock;

    public static void init() {
        coalGeneratorBlock = new CoalGeneratorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        coalGeneratorBlock.initModel();
    }
}
