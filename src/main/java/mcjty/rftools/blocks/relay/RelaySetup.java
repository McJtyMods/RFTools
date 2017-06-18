package mcjty.rftools.blocks.relay;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class RelaySetup {
    public static RelayBlock relayBlock;

    public static void init() {
        relayBlock = new RelayBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        relayBlock.initModel();
    }


    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;

    }
}
