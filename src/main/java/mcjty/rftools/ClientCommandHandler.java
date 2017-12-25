package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import mcjty.rftools.blocks.logic.counter.CounterBlock;
import mcjty.rftools.blocks.security.SecurityCardItem;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.GuiLocator;
import mcjty.rftools.blocks.storage.ModularStorageBlock;
import mcjty.rftools.blocks.teleporter.MatterTransmitterBlock;
import mcjty.rftools.shapes.ScanDataManagerClient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class ClientCommandHandler {

    public static final String CMD_RETURN_DESTINATION_INFO = "returnDestinationInfo";
    public static final String CMD_RETURN_SECURITY_NAME = "returnSecurityName";
    public static final String CMD_RETURN_SCAN_DIRTY = "returnScanDirty";
    public static final String CMD_RETURN_ENERGY_CONSUMPTION = "returnEnergyConsumption";
    public static final String CMD_RETURN_STORAGE_INFO = "returnStorageInfo";
    public static final String CMD_POSITION_TO_CLIENT = "positionToClient";
    public static final String CMD_FLASH_ENDERGENIC = "flashEndergenic";
    public static final String CMD_RETURN_COUNTER_INFO = "returnCounterInfo";

    public static void registerCommands() {
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_DESTINATION_INFO, (player, arguments) -> {
            MatterTransmitterBlock.setDestinationInfo(arguments.getInt(), arguments.getString());
            return true;
        });
        if(SecurityConfiguration.enabled) {
            McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_SECURITY_NAME, (player, arguments) -> {
                SecurityCardItem.channelNameFromServer = arguments.getString();
                return true;
            });
        }
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_SCAN_DIRTY, (player, arguments) -> {
            ScanDataManagerClient.getScansClient().getOrCreateScan(arguments.getInt())
                    .setDirtyCounter(arguments.getInt());
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_ENERGY_CONSUMPTION, (player, arguments) -> {
            GuiLocator.energyConsumption = arguments.getInt();
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_STORAGE_INFO, (player, arguments) -> {
            ModularStorageBlock.cntReceived = arguments.getInt();
            ModularStorageBlock.nameModuleReceived = arguments.getString();
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_FLASH_ENDERGENIC, (player, arguments) -> {
            BlockPos pos = arguments.getBlockPos();
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity tileEntity = (EndergenicTileEntity) te;
                tileEntity.syncCountersFromServer(arguments.getInt(), arguments.getInt());
            }
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_POSITION_TO_CLIENT, (player, arguments) -> {
            BlockPos tePos = arguments.getBlockPos();
            BlockPos scanPos = arguments.getBlockPos();
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(tePos);
            if (te instanceof BuilderTileEntity) {
                BuilderTileEntity.setScanLocationClient(tePos, scanPos);
            }
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_COUNTER_INFO, (player, arguments) -> {
            CounterBlock.cntReceived = arguments.getInt();
            return true;
        });
    }
}
