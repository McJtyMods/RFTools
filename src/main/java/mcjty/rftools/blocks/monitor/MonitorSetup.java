package mcjty.rftools.blocks.monitor;

import mcjty.lib.blocks.BaseBlock;
import mcjty.lib.builder.BlockBuilder;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;


public class MonitorSetup {
    public static BaseBlock monitorBlock;
    public static BaseBlock liquidMonitorBlock;

    @ObjectHolder("rftools:liquid_monitor")
    public static TileEntityType<?> TYPE_LIQUID_MONITOR;

    @ObjectHolder("rftools:energy_monitor")
    public static TileEntityType<?> TYPE_ENERGY_MONITOR;

    public static void init() {
        monitorBlock = new BaseBlock("rf_monitor", new BlockBuilder()
                .tileEntitySupplier(RFMonitorBlockTileEntity::new)
//                .flags(BlockFlags.REDSTONE_OUTPUT)
//                .property(RFMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.rf_monitor"));

        liquidMonitorBlock = new BaseBlock("liquid_monitor", new BlockBuilder()
                .tileEntitySupplier(LiquidMonitorBlockTileEntity::new)
//                .flags(BlockFlags.REDSTONE_OUTPUT)
//                .property(LiquidMonitorBlockTileEntity.LEVEL)
                .info("message.rftools.shiftmessage")
                .infoExtended("message.rftools.liquid_monitor"));
    }

    // @todo 1.14
//    @SideOnly(Side.CLIENT)
//    public static void initClient() {
//        monitorBlock.initModel();
//        monitorBlock.setGuiFactory(GuiRFMonitor::new);
//        liquidMonitorBlock.initModel();
//        liquidMonitorBlock.setGuiFactory(GuiLiquidMonitor::new);
//    }
}
