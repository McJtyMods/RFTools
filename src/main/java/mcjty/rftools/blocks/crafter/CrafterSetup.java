package mcjty.rftools.blocks.crafter;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CrafterSetup {

    public static CrafterBlock crafterBlock1;
    public static CrafterBlock crafterBlock2;
    public static CrafterBlock crafterBlock3;

    public static void init() {
        if(!CrafterConfiguration.enabled.get()) return;
        crafterBlock1 = new CrafterBlock("crafter1", CrafterBlockTileEntity1.class);
        crafterBlock2 = new CrafterBlock("crafter2", CrafterBlockTileEntity2.class);
        crafterBlock3 = new CrafterBlock("crafter3", CrafterBlockTileEntity3.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!CrafterConfiguration.enabled.get()) return;
        crafterBlock1.initModel();
        crafterBlock2.initModel();
        crafterBlock3.initModel();
    }
}
