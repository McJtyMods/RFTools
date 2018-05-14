package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.rftools.blocks.builder.BuilderTileEntity;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import mcjty.rftools.blocks.logic.counter.CounterTileEntity;
import mcjty.rftools.blocks.security.SecurityCardItem;
import mcjty.rftools.blocks.security.SecurityConfiguration;
import mcjty.rftools.blocks.shaper.GuiLocator;
import mcjty.rftools.blocks.storage.ModularStorageBlock;
import mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
import mcjty.rftools.blocks.teleporter.MatterTransmitterBlock;
import mcjty.rftools.shapes.ScanDataManagerClient;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class ClientCommandHandler {

    public static final String CMD_RETURN_SCANNER_CONTENTS = "returnScannerContents";
    public static final Key<List<ItemStack>> PARAM_STACKS = new Key<>("stacks", Type.ITEMSTACK_LIST);

    public static final String CMD_RETURN_DESTINATION_INFO = "returnDestinationInfo";
    public static final Key<Integer> PARAM_ID = new Key<>("id", Type.INTEGER);
    public static final Key<String> PARAM_NAME = new Key<>("name", Type.STRING);

    public static final String CMD_RETURN_SECURITY_NAME = "returnSecurityName";

    public static final String CMD_RETURN_SCAN_DIRTY = "returnScanDirty";
    public static final Key<Integer> PARAM_SCANID = new Key<>("scanid", Type.INTEGER);
    public static final Key<Integer> PARAM_COUNTER = new Key<>("counter", Type.INTEGER);

    public static final String CMD_RETURN_ENERGY_CONSUMPTION = "returnEnergyConsumption";
    public static final Key<Integer> PARAM_ENERGY = new Key<>("energy", Type.INTEGER);

    public static final String CMD_RETURN_STORAGE_INFO = "returnStorageInfo";

    public static final String CMD_POSITION_TO_CLIENT = "positionToClient";
    public static final Key<BlockPos> PARAM_POS = new Key<>("pos", Type.BLOCKPOS);
    public static final Key<BlockPos> PARAM_SCAN = new Key<>("scan", Type.BLOCKPOS);

    public static final String CMD_FLASH_ENDERGENIC = "flashEndergenic";
    public static final Key<Integer> PARAM_GOODCOUNTER = new Key<>("goodcounter", Type.INTEGER);
    public static final Key<Integer> PARAM_BADCOUNTER = new Key<>("badcounter", Type.INTEGER);

    public static final String CMD_RETURN_COUNTER_INFO = "returnCounterInfo";

    public static void registerCommands() {
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_SCANNER_CONTENTS, (player, arguments) -> {
            GuiStorageScanner.fromServer_inventory = arguments.get(PARAM_STACKS);
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_DESTINATION_INFO, (player, arguments) -> {
            MatterTransmitterBlock.setDestinationInfo(arguments.get(PARAM_ID), arguments.get(PARAM_NAME));
            return true;
        });
        if(SecurityConfiguration.enabled) {
            McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_SECURITY_NAME, (player, arguments) -> {
                SecurityCardItem.channelNameFromServer = arguments.get(PARAM_NAME);
                return true;
            });
        }
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_SCAN_DIRTY, (player, arguments) -> {
            ScanDataManagerClient.getScansClient().getOrCreateScan(arguments.get(PARAM_SCANID))
                    .setDirtyCounter(arguments.get(PARAM_COUNTER));
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_ENERGY_CONSUMPTION, (player, arguments) -> {
            GuiLocator.energyConsumption = arguments.get(PARAM_ENERGY);
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_STORAGE_INFO, (player, arguments) -> {
            ModularStorageBlock.cntReceived = arguments.get(PARAM_COUNTER);
            ModularStorageBlock.nameModuleReceived = arguments.get(PARAM_NAME);
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_FLASH_ENDERGENIC, (player, arguments) -> {
            BlockPos pos = arguments.get(PARAM_POS);
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(pos);
            if (te instanceof EndergenicTileEntity) {
                EndergenicTileEntity tileEntity = (EndergenicTileEntity) te;
                tileEntity.syncCountersFromServer(arguments.get(PARAM_GOODCOUNTER), arguments.get(PARAM_BADCOUNTER));
            }
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_POSITION_TO_CLIENT, (player, arguments) -> {
            BlockPos tePos = arguments.get(PARAM_POS);
            BlockPos scanPos = arguments.get(PARAM_SCAN);
            TileEntity te = RFTools.proxy.getClientWorld().getTileEntity(tePos);
            if (te instanceof BuilderTileEntity) {
                BuilderTileEntity.setScanLocationClient(tePos, scanPos);
            }
            return true;
        });
        McJtyLib.registerClientCommand(RFTools.MODID, CMD_RETURN_COUNTER_INFO, (player, arguments) -> {
            CounterTileEntity.cntReceived = arguments.get(PARAM_COUNTER);
            return true;
        });
    }
}
