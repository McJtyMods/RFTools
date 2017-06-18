package mcjty.rftools.blocks.endergen;

import mcjty.rftools.GeneralConfiguration;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EndergenicSetup {
    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static void init() {
        endergenicBlock = new EndergenicBlock();
        pearlInjectorBlock = new PearlInjectorBlock();
        enderMonitorBlock = new EnderMonitorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        endergenicBlock.initModel();
        pearlInjectorBlock.initModel();
        enderMonitorBlock.initModel();
    }

    public static void initCrafting() {
        Block redstoneTorch = Blocks.REDSTONE_TORCH;
        if (GeneralConfiguration.enableEndergenRecipe) {

        }

    }
}
