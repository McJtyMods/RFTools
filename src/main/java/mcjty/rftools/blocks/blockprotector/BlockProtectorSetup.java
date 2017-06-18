package mcjty.rftools.blocks.blockprotector;

import mcjty.rftools.GeneralConfiguration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    public static void init() {
        blockProtectorBlock = new BlockProtectorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        blockProtectorBlock.initModel();
    }

    public static void initCrafting() {
        if (GeneralConfiguration.enableBlockProtectorRecipe) {

        }
    }
}
