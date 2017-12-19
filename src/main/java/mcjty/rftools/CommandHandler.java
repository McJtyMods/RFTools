package mcjty.rftools;

import mcjty.lib.McJtyLib;
import mcjty.rftools.blocks.builder.BuilderTools;
import mcjty.rftools.blocks.security.SecurityTools;
import mcjty.rftools.blocks.shaper.ShaperTools;
import mcjty.rftools.craftinggrid.StorageCraftingTools;
import mcjty.rftools.items.storage.StorageTools;
import mcjty.rftools.items.teleportprobe.PorterTools;
import mcjty.rftools.varia.RFToolsTools;

public class CommandHandler {

    public static final String CMD_COMPACT = "compact";
    public static final String CMD_CLEARGRID = "clearGrid";
    public static final String CMD_CYCLESTORAGE = "cycleStorage";
    public static final String CMD_CLEARTARGET = "clearTarget";
    public static final String CMD_SETTARGET = "setTarget";
    public static final String CMD_GETTARGETS = "getTargets";
    public static final String CMD_FORCETELEPORT = "forceTeleport";
    public static final String CMD_CYCLEDESTINATION = "cycleDestination";
    public static final String CMD_GETDESTINATIONINFO = "getDestinationInfo";
    public static final String CMD_GETRFINRANGE = "getRfInRange";
    public static final String CMD_OPENGUI = "openGui";
    public static final String CMD_REQUESTSHAPEDATA = "requestShapeData";
    public static final String CMD_REQUESTSCANDIRTY = "requestScanDirty";
    public static final String CMD_REQUESTLOCATORENERGY = "requestLocatorEnergy";
    public static final String CMD_GETCHAMBERINFO = "getChamberInfo";
    public static final String CMD_GETSECURITYINFO = "getSecurityInfo";
    public static final String CMD_GETSECURITYNAME = "getSecurityName";
    public static final String CMD_CRAFTFROMGRID = "craftFromGrid";
    public static final String CMD_REQUESTGRIDSYNC = "requestGridSync";

    public static void registerCommands() {
        McJtyLib.registerCommand(RFTools.MODID, CMD_COMPACT, (player, arguments) -> {
            StorageTools.compact(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CLEARGRID, (player, arguments) -> {
            StorageTools.clearGrid(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CYCLESTORAGE, (player, arguments) -> {
            StorageTools.cycleStorage(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CLEARTARGET, (player, arguments) -> {
            PorterTools.clearTarget(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_SETTARGET, (player, arguments) -> {
            PorterTools.setTarget(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETTARGETS, (player, arguments) -> {
            PorterTools.returnTargets(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_FORCETELEPORT, (player, arguments) -> {
            PorterTools.forceTeleport(player, arguments.getInt(), arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CYCLEDESTINATION, (player, arguments) -> {
            PorterTools.cycleDestination(player, arguments.getBoolean());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETDESTINATIONINFO, (player, arguments) -> {
            PorterTools.returnDestinationInfo(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETRFINRANGE, (player, arguments) -> {
            RFToolsTools.returnRfInRange(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_OPENGUI, (player, arguments) -> {
            ShaperTools.openGui(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUESTSHAPEDATA, (player, arguments) -> {
            ShaperTools.requestExtraShapeData(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUESTSCANDIRTY, (player, arguments) -> {
            ShaperTools.requestScanDirty(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUESTLOCATORENERGY, (player, arguments) -> {
            ShaperTools.requestLocatorEnergyConsumption(player, arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETCHAMBERINFO, (player, arguments) -> {
            BuilderTools.returnChamberInfo(player);
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETSECURITYINFO, (player, arguments) -> {
            SecurityTools.returnSecurityInfo(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_GETSECURITYNAME, (player, arguments) -> {
            SecurityTools.returnSecurityName(player, arguments.getInt());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_CRAFTFROMGRID, (player, arguments) -> {
            StorageCraftingTools.craftFromGrid(player, arguments.getInt(), arguments.getBoolean(), arguments.getBlockPos());
            return true;
        });
        McJtyLib.registerCommand(RFTools.MODID, CMD_REQUESTGRIDSYNC, (player, arguments) -> {
            StorageCraftingTools.requestGridSync(player, arguments.getBlockPos());
            return true;
        });
    }
}
