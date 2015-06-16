package mcjty.rftools.blocks.teleporter;

import cpw.mods.fml.common.Optional;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import mcjty.rftools.blocks.dimlets.DimletConfiguration;
import mcjty.rftools.dimension.DimensionStorage;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.rftools.network.Argument;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayerMP;
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

@Optional.InterfaceList({
        @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers"),
        @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral", modid = "ComputerCraft")})
public class DialingDeviceTileEntity extends GenericEnergyReceiverTileEntity implements SimpleComponent, IPeripheral {

    public static final String CMD_TELEPORT = "tp";
    public static final String CMD_GETRECEIVERS = "getReceivers";
    public static final String CLIENTCMD_GETRECEIVERS = "getReceivers";
    public static final String CMD_DIAL = "dial";
    public static final String CMD_DIALONCE = "dialOnce";
    public static final String CMD_FAVORITE = "favorite";
    public static final String CMD_SHOWFAVORITE = "showFavorite";
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
    public static final String COMPONENT_NAME = "dialing_device";

    private boolean showOnlyFavorites = false;

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
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x + 1, y, z))) {
            return true;
        }
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x - 1, y, z))) {
            return true;
        }
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x, y + 1, z))) {
            return true;
        }
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x, y - 1, z))) {
            return true;
        }
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x, y, z + 1))) {
            return true;
        }
        if (TeleporterSetup.matterBoosterBlock.equals(world.getBlock(x, y, z - 1))) {
            return true;
        }
        return false;
    }


    public static boolean isDestinationAnalyzerAvailable(World world, int x, int y, int z) {
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x + 1, y, z))) {
            return true;
        }
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x - 1, y, z))) {
            return true;
        }
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x, y + 1, z))) {
            return true;
        }
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x, y - 1, z))) {
            return true;
        }
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x, y, z + 1))) {
            return true;
        }
        if (TeleporterSetup.destinationAnalyzerBlock.equals(world.getBlock(x, y, z - 1))) {
            return true;
        }
        return false;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String getType() {
        return COMPONENT_NAME;
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public String[] getMethodNames() {
        return new String[] { "getTransmitterCount", "getTransmitter", "getReceiverCount", "getReceiver", "getReceiverName", "getTransmitterName",
            "dial", "dialOnce", "interrupt", "getDialed" };
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                return new Object[] { transmitterInfos.size() };
            }
            case 1: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                int idx = ((Double) arguments[0]).intValue();
                TransmitterInfo ti = transmitterInfos.get(idx);
                return new Object[] { ti.getCoordinate().getX(), ti.getCoordinate().getY(), ti.getCoordinate().getZ() };
            }
            case 2: {
                List<TeleportDestinationClientInfo> receivers = searchReceivers(null);
                return new Object[] { receivers.size() };
            }
            case 3: {
                List<TeleportDestinationClientInfo> receivers = searchReceivers(null);
                int idx = ((Double) arguments[0]).intValue();
                TeleportDestinationClientInfo ti = receivers.get(idx);
                return new Object[] { ti.getDimension(), ti.getCoordinate().getX(), ti.getCoordinate().getY(), ti.getCoordinate().getZ() };
            }
            case 4: {
                List<TeleportDestinationClientInfo> receivers = searchReceivers(null);
                int idx = ((Double) arguments[0]).intValue();
                TeleportDestinationClientInfo ti = receivers.get(idx);
                TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
                TeleportDestination destination = destinations.getDestination(ti.getCoordinate(), ti.getDimension());
                if (destination == null) {
                    return null;
                }
                return new Object[] { destination.getName() };
            }
            case 5: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                int idx = ((Double) arguments[0]).intValue();
                TransmitterInfo ti = transmitterInfos.get(idx);
                return new Object[] { ti.getName() };
            }
            case 6: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                List<TeleportDestinationClientInfo> receivers = searchReceivers(null);

                int transIdx = ((Double) arguments[0]).intValue();
                int recIdx = ((Double) arguments[1]).intValue();

                TransmitterInfo trans = transmitterInfos.get(transIdx);
                TeleportDestinationClientInfo rec = receivers.get(recIdx);

                int transDim = worldObj.provider.dimensionId;
                int result = dial(null, trans.getCoordinate(), transDim, rec.getCoordinate(), rec.getDimension(), false);
                return new Object[] { result };
            }
            case 7: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                List<TeleportDestinationClientInfo> receivers = searchReceivers(null);

                int transIdx = ((Double) arguments[0]).intValue();
                int recIdx = ((Double) arguments[1]).intValue();

                TransmitterInfo trans = transmitterInfos.get(transIdx);
                TeleportDestinationClientInfo rec = receivers.get(recIdx);

                int transDim = worldObj.provider.dimensionId;
                int result = dial(null, trans.getCoordinate(), transDim, rec.getCoordinate(), rec.getDimension(), true);
                return new Object[] { result };
            }
            case 8: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                int transIdx = ((Double) arguments[0]).intValue();
                TransmitterInfo trans = transmitterInfos.get(transIdx);

                int transDim = worldObj.provider.dimensionId;
                int result = dial(null, trans.getCoordinate(), transDim, null, -1, false);
                if (result == DIAL_INVALID_DESTINATION_MASK) {
                    result = 0;
                }
                return new Object[] { result };
            }
            case 9: {
                List<TransmitterInfo> transmitterInfos = searchTransmitters();
                int transIdx = ((Double) arguments[0]).intValue();
                TransmitterInfo trans = transmitterInfos.get(transIdx);
                TeleportDestination teleportDestination = trans.getTeleportDestination();
                if (teleportDestination == null) {
                    return null;
                }
                Coordinate c = teleportDestination.getCoordinate();
                return new Object[] { teleportDestination.getDimension(), c.getX(), c.getY(), c.getZ() };
            }
        }
        return new Object[0];
    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void attach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public void detach(IComputerAccess computer) {

    }

    @Override
    @Optional.Method(modid = "ComputerCraft")
    public boolean equals(IPeripheral other) {
        return false;
    }

    @Override
    @Optional.Method(modid = "OpenComputers")
    public String getComponentName() {
        return COMPONENT_NAME;
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

        int result = dial(null, transC, transDim, recC, recDim, false);
        return new Object[] { result };
    }

    @Callback
    @Optional.Method(modid = "OpenComputers")
    public Object[] dialOnce(Context context, Arguments args) throws Exception {
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

        int result = dial(null, transC, transDim, recC, recDim, true);
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

        int result = dial(null, transC, transDim, null, -1, false);
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
                TeleportDestination teleportDestination = info.getTeleportDestination();
                if (teleportDestination == null) {
                    return null;
                }
                Map<String,Integer> coordinate = new HashMap<String, Integer>();
                coordinate.put("dim", teleportDestination.getDimension());
                Coordinate c = teleportDestination.getCoordinate();
                coordinate.put("x", c.getX());
                coordinate.put("y", c.getY());
                coordinate.put("z", c.getZ());
                return new Object[] { coordinate };
            }
        }
        return null;
    }

    public boolean isShowOnlyFavorites() {
        return showOnlyFavorites;
    }

    public void setShowOnlyFavorites(boolean showOnlyFavorites) {
        this.showOnlyFavorites = showOnlyFavorites;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        showOnlyFavorites = tagCompound.getBoolean("showFav");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setBoolean("showFav", showOnlyFavorites);
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
            if (yy >= 0 && yy < worldObj.getHeight()) {
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

    // Server side only.
    private void changeFavorite(String playerName, Coordinate receiver, int dimension, boolean favorite) {
        List list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for (Object p : list) {
            EntityPlayerMP entityplayermp = (EntityPlayerMP) p;
            if (playerName.equals(entityplayermp.getDisplayName())) {
                PlayerExtendedProperties properties = PlayerExtendedProperties.getProperties(entityplayermp);
                properties.getFavoriteDestinationsProperties().setDestinationFavorite(new GlobalCoordinate(receiver, dimension), favorite);
                return;
            }
        }
    }


    // Server side only
    private int dial(String player, Coordinate transmitter, int transDim, Coordinate coordinate, int dimension, boolean once) {
        return TeleportationTools.dial(worldObj, this, player, transmitter, transDim, coordinate, dimension, once);
    }

    // Server side only
    private int checkStatus(Coordinate c, int dim) {
        int cost = TeleportConfiguration.rfPerCheck;
        cost = (int) (cost * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }
        consumeEnergy(cost);

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
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return rc;
        }
        if (CMD_FAVORITE.equals(command)) {
            String player = args.get("player").getString();
            Coordinate receiver = args.get("receiver").getCoordinate();
            int dimension = args.get("dimension").getInteger();
            boolean favorite = args.get("favorite").getBoolean();
            changeFavorite(player, receiver, dimension, favorite);
            return true;
        } else if (CMD_SHOWFAVORITE.equals(command)) {
            boolean favorite = args.get("favorite").getBoolean();
            setShowOnlyFavorites(favorite);
            return true;
        }

        return false;
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

            return dial(player, transmitter, transDim, c, dim, false);
        } else if (CMD_DIALONCE.equals(command)) {
            String player = args.get("player").getString();
            Coordinate transmitter = args.get("trans").getCoordinate();
            int transDim = args.get("transDim").getInteger();
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();

            return dial(player, transmitter, transDim, c, dim, true);
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
