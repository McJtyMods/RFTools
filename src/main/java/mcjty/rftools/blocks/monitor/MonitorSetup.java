package mcjty.rftools.blocks.monitor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockFlags;
import mcjty.lib.container.GenericContainer;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.setup.GuiProxy;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


public class MonitorSetup {
    public static BaseBlock monitorBlock;
    public static BaseBlock liquidMonitorBlock;

    @ObjectHolder("rftools:liquid_monitor")
    public static TileEntityType<?> TYPE_LIQUID_MONITOR;

    public static void init() {
        monitorBlock = ModBlocks.builderFactory.<RFMonitorBlockTileEntity> builder("rf_monitor")
                .tileEntityClass(RFMonitorBlockTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_OUTPUT)
                .guiId(GuiProxy.GUI_RF_MONITOR)
                .property(RFMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.rf_monitor")
                .build();

        liquidMonitorBlock = ModBlocks.builderFactory.<LiquidMonitorBlockTileEntity> builder("liquid_monitor")
                .tileEntityClass(LiquidMonitorBlockTileEntity.class)
                .emptyContainer()
                .flags(BlockFlags.REDSTONE_OUTPUT)
                .guiId(GuiProxy.GUI_LIQUID_MONITOR)
                .property(LiquidMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.liquid_monitor")
                .build();
    }

    @SideOnly(Side.CLIENT)
    public static void initClient() {
        monitorBlock.initModel();
        monitorBlock.setGuiFactory(GuiRFMonitor::new);
        liquidMonitorBlock.initModel();
        liquidMonitorBlock.setGuiFactory(GuiLiquidMonitor::new);
    }
}
