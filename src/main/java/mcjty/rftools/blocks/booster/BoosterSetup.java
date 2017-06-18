package mcjty.rftools.blocks.booster;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BoosterSetup {
    public static BoosterBlock boosterBlock;

    public static void init() {
        boosterBlock = new BoosterBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        boosterBlock.initModel();
    }

    public static void initCrafting() {

    }
}
