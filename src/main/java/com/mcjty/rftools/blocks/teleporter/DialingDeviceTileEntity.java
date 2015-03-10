package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.common.Optional;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Optional.InterfaceList(@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"))
public class DialingDeviceTileEntity extends GenericEnergyHandlerTileEntity implements SimpleComponent {

    public static final String CMD_TELEPORT = "tp";
    public static final String CMD_GETRECEIVERS = "getReceivers";
    public static final String CLIENTCMD_GETRECEIVERS = "getReceivers";
    public static final String CMD_DIAL = "dial";
    public static final String CLIENTCMD_DIAL = "dialResult";
    public static final String CMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CLIENTCMD_GETTRANSMITTERS = "getTransmitters";
    public static final String CMD_CHECKSTATUS = "checkStatus";
    public static final String CLIENTCMD_STATUS = "status";
    public static final int DIAL_RECEIVER_BLOCKED_MASK = 0x1;       // One value for blocked or not on receiver side
    public static final int DIAL_TRANSMITTER_BLOCKED_MASK = 0x2;    // One value for blocked or not on transmitter side
    public static final int DIAL_INVALID_DESTINATION_MASK = 0x4;    // The destination is somehow invalid
    public static final int DIAL_DIALER_POWER_LOW_MASK = 0x8;       // The dialer itself is low on power
    public static final int DIAL_RECEIVER_POWER_LOW_MASK = 0x10;    // The receiver is low on power
    public static final int DIAL_TRANSMITTER_NOACCESS = 0x20;       // No access to transmitter
    public static final int DIAL_RECEIVER_NOACCESS = 0x40;          // No access to receiver
    public static final int DIAL_INTERRUPTED = 0x80;                // The dial was interrupted
    public static final int DIAL_INVALID_SOURCE_MASK = 0x100;       // The source is somehow invalid
    public static final int DIAL_DIMENSION_POWER_LOW_MASK = 0x200;  // The destination dimension is low on power
    public static final int DIAL_OK = 0;                            // All is ok

    public DialingDeviceTileEntity() {
        super(TeleportConfiguration.DIALER_MAXENERGY, TeleportConfiguration.DIALER_RECEIVEPERTICK);
    }

    /**
     * Calculate the distance (in string form) between a transmitter and receiver.
     * @param world
     * @param transmitterInfo
     * @param teleportDestination
     * @return the distance or else 'dimension warp' in case it is another dimension.
     */
    public static String calculateDistance(World world, TransmitterInfo transmitterInfo, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return "dimension warp";
        } else {
            Coordinate c1 = transmitterInfo.getCoordinate();
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            return Integer.toString((int) dist);
        }
    }

    public static boolean isMatterBoosterAvailable(World world, int x, int y, int z) {
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x + 1, y, z))) {
            return true;
        }
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x - 1, y, z))) {
            return true;
        }
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x, y + 1, z))) {
            return true;
        }
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x, y - 1, z))) {
            return true;
        }
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x, y, z + 1))) {
            return true;
        }
        if (ModBlocks.matterBoosterBlock.equals(world.getBlock(x, y, z - 1))) {
            return true;
        }
        return false;
    }


    public static boolean isDestinationAnalyzerAvailable(World world, int x, int y, int z) {
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x + 1, y, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x - 1, y, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y + 1, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y - 1, z))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y, z + 1))) {
            return true;
        }
        if (ModBlocks.destinationAnalyzerBlock.equals(world.getBlock(x, y, z - 1))) {
            return true;
        }
        return false;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return "dialing_device";
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getTransmitters(Context context, Arguments args) throws Exception {
        List<TransmitterInfo> transmitterInfos = searchTransmitters();
        List<Map<String,Integer>> result = new ArrayList<Map<String, Integer>>();
        for (TransmitterInfo info : transmitterInfos) {
            Map<String,Integer> coordinate = new HashMap<String, Integer>();
            coordinate.put("x", info.getCoordinate().getX());
            coordinate.put("y", info.getCoordinate().getY());
            coordinate.put("z", info.getCoordinate().getZ());
            result.add(coordinate);
        }

        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getReceivers(Context context, Arguments args) throws Exception {
        List<TeleportDestinationClientInfo> receivers = searchReceivers(null);
        List<Map<String,Integer>> result = new ArrayList<Map<String, Integer>>();
        for (TeleportDestinationClientInfo info : receivers) {
            Map<String,Integer> coordinate = new HashMap<String, Integer>();
            coordinate.put("dim", info.getDimension());
            coordinate.put("x", info.getCoordinate().getX());
            coordinate.put("y", info.getCoordinate().getY());
            coordinate.put("z", info.getCoordinate().getZ());
            result.add(coordinate);
        }

        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getReceiverName(Context context, Arguments args) throws Exception {
        Map receiver = args.checkTable(0);
        if (!receiver.containsKey("x") || !receiver.containsKey("y") || !receiver.containsKey("z")) {
            throw new IllegalArgumentException("Receiver map doesn't contain the right x,y,z coordinate!");
        }
        if (!receiver.containsKey("dim")) {
            throw new IllegalArgumentException("Receiver map doesn't contain the right dimension!");
        }
        Coordinate recC = new Coordinate(((Double) receiver.get("x")).intValue(), ((Double) receiver.get("y")).intValue(), ((Double) receiver.get("z")).intValue());
        int recDim = ((Double) receiver.get("dim")).intValue();

        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination destination = destinations.getDestination(recC, recDim);
        if (destination == null) {
            return null;
        }

        return new Object[] { destination.getName() };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getTransmitterName(Context context, Arguments args) throws Exception {
        Map transmitter = args.checkTable(0);
        if (!transmitter.containsKey("x") || !transmitter.containsKey("y") || !transmitter.containsKey("z")) {
            throw new IllegalArgumentException("Transmitter map doesn't contain the right x,y,z coordinate!");
        }
        Coordinate transC = new Coordinate(((Double) transmitter.get("x")).intValue(), ((Double) transmitter.get("y")).intValue(), ((Double) transmitter.get("z")).intValue());

        for (TransmitterInfo info : searchTransmitters()) {
            if (transC.equals(info.getCoordinate())) {
                return new Object[] { info.getName() };
            }
        }
        return null;
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] dial(Context context, Arguments args) throws Exception {
        Map transmitter = args.checkTable(0);
        Map receiver = args.checkTable(1);
        if (!transmitter.containsKey("x") || !transmitter.containsKey("y") || !transmitter.containsKey("z")) {
            throw new IllegalArgumentException("Transmitter map doesn't contain the right x,y,z coordinate!");
        }
        if (!receiver.containsKey("x") || !receiver.containsKey("y") || !receiver.containsKey("z")) {
            throw new IllegalArgumentException("Receiver map doesn't contain the right x,y,z coordinate!");
        }
        if (!receiver.containsKey("dim")) {
            throw new IllegalArgumentException("Receiver map doesn't contain the right dimension!");
        }

        Coordinate transC = new Coordinate(((Double) transmitter.get("x")).intValue(), ((Double) transmitter.get("y")).intValue(), ((Double) transmitter.get("z")).intValue());
        int transDim = worldObj.provider.dimensionId;
        Coordinate recC = new Coordinate(((Double) receiver.get("x")).intValue(), ((Double) receiver.get("y")).intValue(), ((Double) receiver.get("z")).intValue());
        int recDim = ((Double) receiver.get("dim")).intValue();

        int result = dial(null, transC, transDim, recC, recDim);
        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] interrupt(Context context, Arguments args) throws Exception {
        Map transmitter = args.checkTable(0);
        if (!transmitter.containsKey("x") || !transmitter.containsKey("y") || !transmitter.containsKey("z")) {
            throw new IllegalArgumentException("Transmitter map doesn't contain the right x,y,z coordinate!");
        }

        Coordinate transC = new Coordinate(((Double) transmitter.get("x")).intValue(), ((Double) transmitter.get("y")).intValue(), ((Double) transmitter.get("z")).intValue());
        int transDim = worldObj.provider.dimensionId;

        int result = dial(null, transC, transDim, null, -1);
        if (result == DIAL_INVALID_DESTINATION_MASK) {
            result = 0;
        }
        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] getDialed(Context context, Arguments args) throws Exception {
        Map transmitter = args.checkTable(0);
        if (!transmitter.containsKey("x") || !transmitter.containsKey("y") || !transmitter.containsKey("z")) {
            throw new IllegalArgumentException("Transmitter map doesn't contain the right x,y,z coordinate!");
        }

        Coordinate transC = new Coordinate(((Double) transmitter.get("x")).intValue(), ((Double) transmitter.get("y")).intValue(), ((Double) transmitter.get("z")).intValue());

        List<TransmitterInfo> transmitterInfos = searchTransmitters();
        for (TransmitterInfo info : transmitterInfos) {
            if (info.getCoordinate().equals(transC)) {
                Map<String,Integer> coordinate = new HashMap<String, Integer>();
                coordinate.put("dim", info.getTeleportDestination().getDimension());
                Coordinate c = info.getTeleportDestination().getCoordinate();
                coordinate.put("x", c.getX());
                coordinate.put("y", c.getY());
                coordinate.put("z", c.getZ());
                return new Object[] { coordinate };
            }
        }
        return null;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    private TeleportDestination findDestination(Coordinate coordinate, int dimension) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        return destinations.getDestination(coordinate, dimension);
    }

    private List<TeleportDestinationClientInfo> searchReceivers(String playerName) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        return new ArrayList<TeleportDestinationClientInfo>(destinations.getValidDestinations(playerName));
    }

    public List<TransmitterInfo> searchTransmitters() {
        int x = xCoord;
        int y = yCoord;
        int z = zCoord;

        int hrange = TeleportConfiguration.horizontalDialerRange;
        int vrange = TeleportConfiguration.verticalDialerRange;

        List<TransmitterInfo> transmitters = new ArrayList<TransmitterInfo>();
        for (int dy = -vrange ; dy <= vrange ; dy++) {
            int yy = y + dy;
            if (yy >= 0 && yy < worldObj.getActualHeight()) {
                for (int dz = -hrange ; dz <= hrange; dz++) {
                    int zz = z + dz;
                    for (int dx = -hrange ; dx <= hrange ; dx++) {
                        int xx = x + dx;
                        if (dx != 0 || dy != 0 || dz != 0) {
                            Coordinate c = new Coordinate(xx, yy, zz);
                            TileEntity tileEntity = worldObj.getTileEntity(xx, yy, zz);
                            if (tileEntity != null) {
                                if (tileEntity instanceof MatterTransmitterTileEntity) {
                                    MatterTransmitterTileEntity matterTransmitterTileEntity = (MatterTransmitterTileEntity) tileEntity;
                                    transmitters.add(new TransmitterInfo(c, matterTransmitterTileEntity.getName(), matterTransmitterTileEntity.getTeleportDestination()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return transmitters;
    }

    // Check if there is room for a beam.
    public static boolean checkBeam(Coordinate c, World world, int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = world.getBlock(c.getX(), c.getY()+dy, c.getZ());
            if (!b.isAir(world, c.getX(), c.getY()+dy, c.getZ())) {
                if (dy <= errory) {
                    // Everything below errory must be free.
                    return false;
                } else {
                    // Everything higher then errory doesn't have to be free.
                    break;
                }
            }
        }
        return true;
    }

    // Server side only
    private int dial(String player, Coordinate transmitter, int transDim, Coordinate coordinate, int dimension) {
        World transWorld = RfToolsDimensionManager.getDimensionManager(worldObj).getWorldForDimension(transDim);
        if (transWorld == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_SOURCE_MASK;
        }
        MatterTransmitterTileEntity transmitterTileEntity = (MatterTransmitterTileEntity) transWorld.getTileEntity(transmitter.getX(), transmitter.getY(), transmitter.getZ());

        if (player != null && !transmitterTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_NOACCESS;
        }

        if (coordinate == null) {
            transmitterTileEntity.setTeleportDestination(null);
            return DialingDeviceTileEntity.DIAL_INTERRUPTED;
        }

        TeleportDestination teleportDestination = findDestination(coordinate, dimension);
        if (teleportDestination == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        Coordinate c = teleportDestination.getCoordinate();
        World recWorld = RfToolsDimensionManager.getWorldForDimension(teleportDestination.getDimension());
        if (recWorld == null) {
            recWorld = MinecraftServer.getServer().worldServerForDimension(teleportDestination.getDimension());
            if (recWorld == null) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }
        }

        // Only do this if not an rftools dimension.
        TileEntity tileEntity = recWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }
        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;
        matterReceiverTileEntity.updateDestination();       // Make sure destination is ok.
        if (player != null && !matterReceiverTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_NOACCESS;
        }

        int cost = TeleportConfiguration.rfPerDial;
        cost = (int) (cost * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }

        if (!checkBeam(transmitter, transWorld, 1, 4, 2)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_BLOCKED_MASK;
        }

        extractEnergy(ForgeDirection.DOWN, cost, false);
        transmitterTileEntity.setTeleportDestination(teleportDestination);

        return DialingDeviceTileEntity.DIAL_OK;
    }

    // Server side only
    private int checkStatus(Coordinate c, int dim) {
        int cost = TeleportConfiguration.rfPerCheck;
        cost = (int) (cost * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }
        extractEnergy(ForgeDirection.DOWN, cost, false);

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
        World w = dimensionManager.getWorldForDimension(dim);
        if (w == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        TileEntity tileEntity = w.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        if (dimensionManager.getDimensionInformation(dim) != null) {
            // This is an RFTools dimension. Check power.
            DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(w);
            int energyLevel = dimensionStorage.getEnergyLevel(dim);
            if (energyLevel < DimletConfiguration.DIMPOWER_WARN_TP) {
                return DialingDeviceTileEntity.DIAL_DIMENSION_POWER_LOW_MASK;
            }
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

        return matterReceiverTileEntity.checkStatus();
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETRECEIVERS.equals(command)) {
            String playerName = args.get("player").getString();
            return searchReceivers(playerName);
        } else if (CMD_GETTRANSMITTERS.equals(command)) {
            return searchTransmitters();
        }
        return null;
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_CHECKSTATUS.equals(command)) {
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();
            return checkStatus(c, dim);
        } else if (CMD_DIAL.equals(command)) {
            String player = args.get("player").getString();
            Coordinate transmitter = args.get("trans").getCoordinate();
            int transDim = args.get("transDim").getInteger();
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();

            return dial(player, transmitter, transDim, c, dim);
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETRECEIVERS.equals(command)) {
            GuiDialingDevice.fromServer_receivers = new ArrayList<TeleportDestinationClientInfo>(list);
            return true;
        } else if (CLIENTCMD_GETTRANSMITTERS.equals(command)) {
            GuiDialingDevice.fromServer_transmitters = new ArrayList<TransmitterInfo>(list);
            return true;
        }
        return false;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_STATUS.equals(command)) {
            GuiDialingDevice.fromServer_receiverStatus = result;
            return true;
        } else if (CLIENTCMD_DIAL.equals(command)) {
            GuiDialingDevice.fromServer_dialResult = result;
            return true;
        }
        return false;
    }
}
