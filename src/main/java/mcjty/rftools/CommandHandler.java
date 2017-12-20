package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.lib.network.Arguments;
import mcjty.rftools.blocks.builder.BuilderTools;
import mcjty.rftools.blocks.logic.counter.CounterTileEntity;
import mcjty.rftools.blocks.security.SecurityTools;
import mcjty.rftools.blocks.shaper.ShaperTools;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.items.storage.StorageTools;
import mcjty.rftools.items.teleportprobe.PorterTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class CommandHandler {

    public static final String CMD_COMPACT = "compact";
    public static final String CMD_CLEAR_GRID = "clearGrid";
    public static final String CMD_CYCLE_STORAGE = "cycleStorage";
    public static final String CMD_REQUEST_STORAGE_INFO = "requestStorageInfo";
    public static final String CMD_CLEAR_TARGET = "clearTarget";
    public static final String CMD_SET_TARGET = "setTarget";
    public static final String CMD_GET_TARGETS = "getTargets";
    public static final String CMD_FORCE_TELEPORT = "forceTeleport";
    public static final String CMD_CYCLE_DESTINATION = "cycleDestination";
    public static final String CMD_GET_DESTINATION_INFO = "getDestinationInfo";
    public static final String CMD_GET_RF_IN_RANGE = "getRfInRange";
    public static final String CMD_OPENGUI = "openGui";
    public static final String CMD_REQUEST_SHAPE_DATA = "requestShapeData";
    public static final String CMD_REQUEST_SCAN_DIRTY = "requestScanDirty";
    public static final String CMD_REQUEST_LOCATOR_ENERGY = "requestLocatorEnergy";
    public static final String CMD_GET_CHAMBER_INFO = "getChamberInfo";
    public static final String CMD_GET_SECURITY_INFO = "getSecurityInfo";
    public static final String CMD_GET_SECURITY_NAME = "getSecurityName";
    public static final String CMD_CRAFT_FROM_GRID = "craftFromGrid";
    public static final String CMD_REQUEST_GRID_SYNC = "requestGridSync";
    public static final String CMD_GET_COUNTER_INFO = "getCounterInfo";

    public static void registerCommands() {
        McJtyLib.registerCommand(RFTools.MODID, CMD_COMPACT, (player, arguments) -> {
            StorageTools.compact(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CLEAR_GRID, (player, arguments) -> {
            StorageTools.clearGrid(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CYCLE_STORAGE, (player, arguments) -> {
            StorageTools.cycleStorage(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_STORAGE_INFO, (player, arguments) -> {
            StorageTools.returnStorageInfo(player, arguments.getInt(), arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CLEAR_TARGET, (player, arguments) -> {
            PorterTools.clearTarget(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_SET_TARGET, (player, arguments) -> {
            PorterTools.setTarget(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_TARGETS, (player, arguments) -> {
            PorterTools.returnTargets(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_FORCE_TELEPORT, (player, arguments) -> {
            PorterTools.forceTeleport(player, arguments.getInt(), arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CYCLE_DESTINATION, (player, arguments) -> {
            PorterTools.cycleDestination(player, arguments.getBoolean());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_DESTINATION_INFO, (player, arguments) -> {
            PorterTools.returnDestinationInfo(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_RF_IN_RANGE, (player, arguments) -> {
            RFToolsTools.returnRfInRange(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_OPENGUI, (player, arguments) -> {
            ShaperTools.openGui(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_SHAPE_DATA, (player, arguments) -> {
            ShaperTools.requestExtraShapeData(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_SCAN_DIRTY, (player, arguments) -> {
            ShaperTools.requestScanDirty(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_LOCATOR_ENERGY, (player, arguments) -> {
            ShaperTools.requestLocatorEnergyConsumption(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_CHAMBER_INFO, (player, arguments) -> {
            BuilderTools.returnChamberInfo(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_SECURITY_INFO, (player, arguments) -> {
            SecurityTools.returnSecurityInfo(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_SECURITY_NAME, (player, arguments) -> {
            SecurityTools.returnSecurityName(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CRAFT_FROM_GRID, (player, arguments) -> {
            StorageCraftingTools.craftFromGrid(player, arguments.getInt(), arguments.getBoolean(), arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_GRID_SYNC, (player, arguments) -> {
            StorageCraftingTools.requestGridSync(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_COUNTER_INFO, (player, arguments) -> {
            WorldServer world = DimensionManager.getWorld(arguments.getInt());
            int cnt = -1;
            if (world != null) {
                TileEntity te = world.getTileEntity(arguments.getBlockPos());
                if (te instanceof CounterTileEntity) {
                    CounterTileEntity tileEntity = (CounterTileEntity) te;
                    cnt = tileEntity.getCurrent();
                    RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_COUNTER_INFO,
                            Arguments.builder().value(cnt));
                }
            }
            return true;
        });
    }
}
