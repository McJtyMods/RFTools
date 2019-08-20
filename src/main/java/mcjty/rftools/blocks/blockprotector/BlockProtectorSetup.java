package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.MinecraftForge;



public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    public static void init() {
        if(!BlockProtectorConfiguration.enabled.get()) return;
        blockProtectorBlock = new BlockProtectorBlock();
        MinecraftForge.EVENT_BUS.register(BlockProtectorEventHandlers.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!BlockProtectorConfiguration.enabled.get()) return;
        blockProtectorBlock.initModel();
    }
}
