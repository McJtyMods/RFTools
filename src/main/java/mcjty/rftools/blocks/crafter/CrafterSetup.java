package mcjty.rftools.blocks.crafter;


import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class CrafterSetup {

    public static CrafterBlock crafterBlock1;
    public static CrafterBlock crafterBlock2;
    public static CrafterBlock crafterBlock3;

    @ObjectHolder("rftools:crafter1")
    public static TileEntityType<?> TYPE_CRAFTER1;
    @ObjectHolder("rftools:crafter2")
    public static TileEntityType<?> TYPE_CRAFTER2;
    @ObjectHolder("rftools:crafter3")
    public static TileEntityType<?> TYPE_CRAFTER3;

    public static void init() {
        if(!CrafterConfiguration.enabled.get()) return;
        crafterBlock1 = new CrafterBlock("crafter1", CrafterBlockTileEntity1::new);
        crafterBlock2 = new CrafterBlock("crafter2", CrafterBlockTileEntity2::new);
        crafterBlock3 = new CrafterBlock("crafter3", CrafterBlockTileEntity3::new);
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        if(!CrafterConfiguration.enabled.get()) return;
//        crafterBlock1.initModel();
//        crafterBlock2.initModel();
//        crafterBlock3.initModel();
//    }
}
