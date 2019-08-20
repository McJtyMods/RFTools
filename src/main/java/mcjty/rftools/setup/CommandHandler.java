package mcjty.rftools.setup;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.ClientCommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.builder.BuilderTools;
import mcjty.rftools.blocks.logic.counter.CounterTileEntity;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.security.SecurityTools;
import mcjty.rftools.blocks.shaper.ShaperTools;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTools;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.items.storage.StorageTools;
import mcjty.rftools.items.teleportprobe.PorterTools;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.servernet.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;

public class CommandHandler {

    public static final String CMD_COMPACT = "compact";
    public static final String CMD_CLEAR_GRID = "clearGrid";
    public static final String CMD_CYCLE_STORAGE = "cycleStorage";

    public static final String CMD_REQUEST_SCANNER_CONTENTS = "requestScannerContents";
    public static final String CMD_SCANNER_SEARCH = "scannerSearch";
    public static final Key<Integer> PARAM_SCANNER_DIM = new Key<>("scannerdim", Type.INTEGER);
    public static final Key<BlockPos> PARAM_SCANNER_POS = new Key<>("scannerpos", Type.BLOCKPOS);
    public static final Key<BlockPos> PARAM_INV_POS = new Key<>("invpos", Type.BLOCKPOS);
    public static final Key<String> PARAM_SEARCH_TEXT = new Key<>("text", Type.STRING);

    public static final String CMD_REQUEST_STORAGE_INFO = "requestStorageInfo";
    public static final Key<Integer> PARAM_DIMENSION = new Key<>("dimension", Type.INTEGER);

    public static final String CMD_CLEAR_TARGET = "clearTarget";
    public static final String CMD_SET_TARGET = "setTarget";
    public static final Key<Integer> PARAM_TARGET = new Key<>("target", Type.INTEGER);

    public static final String CMD_GET_TARGETS = "getTargets";
    public static final String CMD_FORCE_TELEPORT = "forceTeleport";

    public static final String CMD_CYCLE_DESTINATION = "cycleDestination";
    public static final Key<Boolean> PARAM_NEXT = new Key<>("next", Type.BOOLEAN);

    public static final String CMD_GET_DESTINATION_INFO = "getDestinationInfo";
    public static final String CMD_GET_RF_IN_RANGE = "getRfInRange";
    public static final String CMD_REQUEST_SHAPE_DATA = "requestShapeData";
    public static final String CMD_REQUEST_SCAN_DIRTY = "requestScanDirty";
    public static final String CMD_REQUEST_LOCATOR_ENERGY = "requestLocatorEnergy";
    public static final String CMD_GET_CHAMBER_INFO = "getChamberInfo";

    public static final String CMD_GET_SECURITY_INFO = "getSecurityInfo";
    public static final Key<Integer> PARAM_ID = new Key<>("id", Type.INTEGER);

    public static final String CMD_GET_SECURITY_NAME = "getSecurityName";

    public static final String CMD_CRAFT_FROM_GRID = "craftFromGrid";
    public static final Key<Integer> PARAM_COUNT = new Key<>("count", Type.INTEGER);
    public static final Key<Boolean> PARAM_TEST = new Key<>("test", Type.BOOLEAN);

    public static final String CMD_REQUEST_GRID_SYNC = "requestGridSync";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);

    public static final String CMD_GET_COUNTER_INFO = "getCounterInfo";

    public static void registerCommands() {
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_SCANNER_CONTENTS, (player, arguments) -> {
            StorageScannerTools.requestContents(player, arguments.get(PARAM_SCANNER_DIM), arguments.get(PARAM_SCANNER_POS), arguments.get(PARAM_INV_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_SCANNER_SEARCH, (player, arguments) -> {
            StorageScannerTools.scannerSearch(player, arguments.get(PARAM_SCANNER_DIM), arguments.get(PARAM_SCANNER_POS), arguments.get(PARAM_SEARCH_TEXT));
            return true;
        });
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
            StorageTools.returnStorageInfo(player, arguments.get(PARAM_DIMENSION), arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CLEAR_TARGET, (player, arguments) -> {
            PorterTools.clearTarget(player, arguments.get(PARAM_TARGET));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_SET_TARGET, (player, arguments) -> {
            PorterTools.setTarget(player, arguments.get(PARAM_TARGET));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_TARGETS, (player, arguments) -> {
            PorterTools.returnTargets(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_FORCE_TELEPORT, (player, arguments) -> {
            PorterTools.forceTeleport(player, arguments.get(PARAM_DIMENSION), arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CYCLE_DESTINATION, (player, arguments) -> {
            PorterTools.cycleDestination(player, arguments.get(PARAM_NEXT));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_DESTINATION_INFO, (player, arguments) -> {
            PorterTools.returnDestinationInfo(player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_RF_IN_RANGE, (player, arguments) -> {
            RFToolsTools.returnRfInRange(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_SHAPE_DATA, (player, arguments) -> {
            ShaperTools.requestExtraShapeData(player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_SCAN_DIRTY, (player, arguments) -> {
            ShaperTools.requestScanDirty(player, arguments.get(PARAM_ID));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_LOCATOR_ENERGY, (player, arguments) -> {
            ShaperTools.requestLocatorEnergyConsumption(player, arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_CHAMBER_INFO, (player, arguments) -> {
            BuilderTools.returnChamberInfo(player);
            return true;
        });
        if(SecurityConfiguration.enabled.get()) {
            McJtyLib.registerCommand(RFTools.MODID, CMD_GET_SECURITY_INFO, (player, arguments) -> {
                SecurityTools.returnSecurityInfo(player, arguments.get(PARAM_ID));
                return true;
            });
            McJtyLib.registerCommand(RFTools.MODID, CMD_GET_SECURITY_NAME, (player, arguments) -> {
                SecurityTools.returnSecurityName(player, arguments.get(PARAM_ID));
                return true;
            });
        }
        McJtyLib.registerCommand(RFTools.MODID, CMD_CRAFT_FROM_GRID, (player, arguments) -> {
            StorageCraftingTools.craftFromGrid(player, arguments.get(PARAM_COUNT), arguments.get(PARAM_TEST), arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUEST_GRID_SYNC, (player, arguments) -> {
            StorageCraftingTools.requestGridSync(player, arguments.get(PARAM_POS));
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GET_COUNTER_INFO, (player, arguments) -> {
            ServerWorld world = DimensionManager.getWorld(arguments.get(PARAM_DIMENSION));
            if (world != null) {
                TileEntity te = world.getTileEntity(arguments.get(PARAM_POS));
                if (te instanceof CounterTileEntity) {
                    CounterTileEntity tileEntity = (CounterTileEntity) te;
                    int cnt = tileEntity.getCurrent();
                    RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_COUNTER_INFO,
                            TypedMap.builder().put(ClientCommandHandler.PARAM_COUNTER, cnt));
                }
            }
            return true;
        });
    }

}
