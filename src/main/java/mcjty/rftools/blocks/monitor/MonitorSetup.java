package mcjty.rftools.blocks.monitor;

import mcjty.lib.builder.BlockFlags;
import mcjty.lib.blocks.GenericBlock;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MonitorSetup {
    public static GenericBlock<RFMonitorBlockTileEntity, GenericContainer> monitorBlock;
    public static GenericBlock<LiquidMonitorBlockTileEntity, GenericContainer> liquidMonitorBlock;

    public static void init() {
        monitorBlock = ModBlocks.builderFactory.<RFMonitorBlockTileEntity> builder("rf_monitor")
                .tileEntityClass(RFMonitorBlockTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_OUTPUT)
                .guiId(RFTools.GUI_RF_MONITOR)
                .property(RFMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.rf_monitor")
                .build();

        liquidMonitorBlock = ModBlocks.builderFactory.<LiquidMonitorBlockTileEntity> builder("liquid_monitor")
                .tileEntityClass(LiquidMonitorBlockTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_OUTPUT)
                .guiId(RFTools.GUI_LIQUID_MONITOR)
                .property(LiquidMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.liquid_monitor")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        monitorBlock.initModel();
        monitorBlock.setGuiClass(GuiRFMonitor.class);
        liquidMonitorBlock.initModel();
        liquidMonitorBlock.setGuiClass(GuiLiquidMonitor.class);
    }
}
