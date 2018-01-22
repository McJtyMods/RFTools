package mcjty.rftools.blocks.blockprotector;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockProtectorSetup {
    public static BlockProtectorBlock blockProtectorBlock;

    public static void init() {
        if(!BlockProtectorConfiguration.enabled) return;
        blockProtectorBlock = new BlockProtectorBlock();
        MinecraftForge.EVENT_BUS.register(BlockProtectorEventHandlers.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!BlockProtectorConfiguration.enabled) return;
        blockProtectorBlock.initModel();
    }
}
