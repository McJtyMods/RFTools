package mcjty.rftools.blocks.endergen;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EndergenicSetup {
    public static EndergenicBlock endergenicBlock;
    public static PearlInjectorBlock pearlInjectorBlock;
    public static EnderMonitorBlock enderMonitorBlock;

    public static void init() {
        if(!EndergenicConfiguration.enabled) return;
        endergenicBlock = new EndergenicBlock();
        pearlInjectorBlock = new PearlInjectorBlock();
        enderMonitorBlock = new EnderMonitorBlock();
        MinecraftForge.EVENT_BUS.register(EndergenicEventHandlers.class);
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        if(!EndergenicConfiguration.enabled) return;
        endergenicBlock.initModel();
        pearlInjectorBlock.initModel();
        enderMonitorBlock.initModel();
    }
}
