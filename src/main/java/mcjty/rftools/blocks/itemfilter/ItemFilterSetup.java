package mcjty.rftools.blocks.itemfilter;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemFilterSetup {
    public static ItemFilterBlock itemFilterBlock;

    public static void init() {
        itemFilterBlock = new ItemFilterBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        itemFilterBlock.initModel();
    }
}
