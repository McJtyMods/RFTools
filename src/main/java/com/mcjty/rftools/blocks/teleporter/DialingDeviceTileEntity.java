package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.DimensionDescriptor;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class DialingDeviceTileEntity extends GenericEnergyHandlerTileEntity {

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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    private TeleportDestination findDestination(Coordinate coordinate, int dimension) {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        TeleportDestination teleportDestination = destinations.getDestination(coordinate, dimension);
        if (teleportDestination != null) {
            return teleportDestination;
        }

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
        Map<Integer,DimensionDescriptor> dimensions = dimensionManager.getDimensions();
        if (dimensions.containsKey(dimension)) {
            if (new Coordinate(0, 70, 0).equals(coordinate)) {
                return new TeleportDestination(coordinate, dimension);
            }
        }
        return null;
    }

    /**
     * return true if the given destination is a dimension destination (destination for
     * a dimension where the matter receiver hasn't been able to be generated yet).
     * @param destination
     * @return
     */
    private boolean isDimensionDestination(TeleportDestination destination) {
        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
        Map<Integer,DimensionDescriptor> dimensions = dimensionManager.getDimensions();
        if (dimensions.containsKey(destination.getDimension())) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
            return !destinations.isDestinationValid(destination);
        }
        return false;
    }

    private List<TeleportDestinationClientInfo> searchReceivers() {
        // First check all dimensions and possibly force the generation of chunk 0 to get the
        // matter receiver there.

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
        Map<Integer,DimensionDescriptor> dimensions = dimensionManager.getDimensions();
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            System.out.println("Try to load dimension: me.getKey() = " + me.getKey());
            WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(me.getKey());
            ChunkProviderServer providerServer = worldServerForDimension.theChunkProviderServer;
            if (!providerServer.chunkExists(0, 0)) {
                providerServer.loadChunk(0, 0);
                worldServerForDimension.getBlock(8, 70, 8);
                providerServer.unloadChunksIfNotNearSpawn(0, 0);
            }
        }

        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);

        List<TeleportDestinationClientInfo> list = new ArrayList<TeleportDestinationClientInfo>(destinations.getValidDestinations());
        return list;
    }

//    private List<TeleportDestinationClientInfo> searchReceivers() {
//        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
//
//        // Contains all destination we already added to the list. This is to prevent duplicates.
//        Set<TeleportDestination> duplicateChecker = new HashSet<TeleportDestination>();
//
//        List<TeleportDestinationClientInfo> list = new ArrayList<TeleportDestinationClientInfo>(destinations.getValidDestinations());
//        for (TeleportDestinationClientInfo c : list) {
//            duplicateChecker.add(new TeleportDestination(c.getCoordinate(), c.getDimension()));
//        }
//
//        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
//        Map<Integer,DimensionDescriptor> dimensions = dimensionManager.getDimensions();
//        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
//            Integer id = me.getKey();
//            TeleportDestination c = new TeleportDestination(new Coordinate(0, 70, 0), id);
//            if (!duplicateChecker.contains(c)) {
//                duplicateChecker.add(c);
//                TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(c);
//                World w = DimensionManager.getWorld(id);
//                String dimName = null;
//                if (w != null) {
//                    dimName = DimensionManager.getProvider(id).getDimensionName();
//                } else {
//                    DimensionInformation info = dimensionManager.getDimensionInformation(id);
//                    dimName = info.getName();
//                }
//                if (dimName == null || dimName.trim().isEmpty()) {
//                    dimName = "Id " + id;
//                }
//                destinationClientInfo.setDimensionName(dimName);
//                list.add(destinationClientInfo);
//            }
//        }
//        return list;
//    }

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

    private void clearBeam(Coordinate c, World world) {
        Block b = world.getBlock(c.getX(), c.getY()+1, c.getZ());
        if (ModBlocks.teleportBeamBlock.equals(b)) {
            world.setBlockToAir(c.getX(), c.getY()+1, c.getZ());
        }
    }


    // Make a beam on top of a given coordinate.
    public static boolean makeBeam(Coordinate c, World world, int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = world.getBlock(c.getX(), c.getY()+dy, c.getZ());
            if ((!b.isAir(world, c.getX(), c.getY()+dy, c.getZ())) && !ModBlocks.teleportBeamBlock.equals(b)) {
                if (dy <= errory) {
                    // Everything below errory must be free.
                    return false;
                } else {
                    // Everything higher then errory doesn't have to be free.
                    break;
                }
            }
        }
        Block b = world.getBlock(c.getX(), c.getY()+1, c.getZ());
        if (b.isAir(world, c.getX(), c.getY()+1, c.getZ()) || ModBlocks.teleportBeamBlock.equals(b)) {
            world.setBlock(c.getX(), c.getY()+1, c.getZ(), ModBlocks.teleportBeamBlock, 0, 2);
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

        if (!transmitterTileEntity.checkAccess(player)) {
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_NOACCESS;
        }

        if (coordinate == null) {
            clearBeam(transmitter, transWorld);
            transmitterTileEntity.setTeleportDestination(null);
            return DialingDeviceTileEntity.DIAL_INTERRUPTED;
        }

        TeleportDestination teleportDestination = findDestination(coordinate, dimension);
        if (teleportDestination == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        Coordinate c = teleportDestination.getCoordinate();
        World recWorld = RfToolsDimensionManager.getDimensionManager(worldObj).getWorldForDimension(teleportDestination.getDimension());
        if (recWorld == null) {
            recWorld = MinecraftServer.getServer().worldServerForDimension(teleportDestination.getDimension());
            if (recWorld == null) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }
        }

        if (!isDimensionDestination(teleportDestination)) {
            // Only do this if not an rftools dimension.
            TileEntity tileEntity = recWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (!(tileEntity instanceof MatterReceiverTileEntity)) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }
            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;
            if (!matterReceiverTileEntity.checkAccess(player)) {
                return DialingDeviceTileEntity.DIAL_RECEIVER_NOACCESS;
            }
        }

        int cost = TeleportConfiguration.rfPerDial;
        cost = (int) (cost * (2.0f - getInfusedFactor()) / 2.0f);

        if (getEnergyStored(ForgeDirection.DOWN) < cost) {
            return DialingDeviceTileEntity.DIAL_DIALER_POWER_LOW_MASK;
        }

        if (!makeBeam(transmitter, transWorld, 1, 4, 2)) {
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

        World w = RfToolsDimensionManager.getDimensionManager(worldObj).getWorldForDimension(dim);
        if (w == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }

        if (isDimensionDestination(new TeleportDestination(c, dim))) {
            // This is a dimension destination. That means a destination where the matter receiver
            // hasn't been realized yet. We can't check the status but we assume it is ok.
            return DialingDeviceTileEntity.DIAL_OK;
        } else {
            TileEntity tileEntity = w.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (!(tileEntity instanceof MatterReceiverTileEntity)) {
                return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
            }

            MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

            return matterReceiverTileEntity.checkStatus();
        }
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETRECEIVERS.equals(command)) {
            return searchReceivers();
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
