package mcjty.rftools.blocks.monitor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MonitorSetup {
    public static RFMonitorBlock monitorBlock;
    public static LiquidMonitorBlock liquidMonitorBlock;

    public static void init() {
        monitorBlock = new RFMonitorBlock();
        liquidMonitorBlock = new LiquidMonitorBlock();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        monitorBlock.initModel();
        liquidMonitorBlock.initModel();
    }
}
