package mcjty.rftools.blocks.teleporter;

import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import mcjty.lib.worlddata.AbstractWorldData;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.*;

public class TeleportDestinations extends AbstractWorldData<TeleportDestinations> {

    private static final String TPDESTINATIONS_NAME = "TPDestinations";

    private final Map<GlobalCoordinate,TeleportDestination> destinations = new HashMap<>();
    private final Map<Integer,GlobalCoordinate> destinationById = new HashMap<>();
    private final Map<GlobalCoordinate,Integer> destinationIdByCoordinate = new HashMap<>();
    private int lastId = 0;

    public TeleportDestinations() {
        super(TPDESTINATIONS_NAME);
    }

//    @Override
//    public void clear() {
//        destinationById.clear();
//        destinationIdByCoordinate.clear();
//        destinations.clear();
//        lastId = 0;
//    }

    public static String getDestinationName(TeleportDestinations destinations, int receiverId) {
        GlobalCoordinate coordinate = destinations.getCoordinateForId(receiverId);
        String name;
        if (coordinate == null) {
            name = "?";
        } else {
            TeleportDestination destination = destinations.getDestination(coordinate);
            if (destination == null) {
                name = "?";
            } else {
                name = destination.getName();
                if (name == null || name.isEmpty()) {
                    name = BlockPosTools.toString(destination.getCoordinate()) + " (" + destination.getDimension() + ")";
                }
            }
        }
        return name;
    }

    public void cleanupInvalid() {
        Set<GlobalCoordinate> keys = new HashSet<>(destinations.keySet());
        for (GlobalCoordinate key : keys) {
            World transWorld = mcjty.lib.varia.TeleportationTools.getWorldForDimension(key.getDimension());
            boolean removed = false;
            if (transWorld == null) {
                Logging.log("Receiver on dimension " + key.getDimension() + " removed because world can't be loaded!");
                removed = true;
            } else {
                BlockPos c = key.getCoordinate();
                TileEntity te;
                try {
                    te = transWorld.getTileEntity(c);
                } catch (Exception e) {
                    te = null;
                }
                if (!(te instanceof MatterReceiverTileEntity)) {
                    Logging.log("Receiver at " + c + " on dimension " + key.getDimension() + " removed because there is no receiver there!");
                    removed = true;
                }
            }
            if (removed) {
                destinations.remove(key);
            }
        }
    }

    public static TeleportDestinations get() {
        return getData(TeleportDestinations::new, TPDESTINATIONS_NAME);
    }


    // Server side only
    public Collection<TeleportDestinationClientInfo> getValidDestinations(World worldObj, String playerName) {
        FavoriteDestinationsProperties properties = null;
        if (playerName != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            List<ServerPlayerEntity> list = server.getPlayerList().getPlayers();
            for (ServerPlayerEntity ServerPlayerEntity : list) {
                if (playerName.equals(ServerPlayerEntity.getName())) {  // @todo 1.14 use UUID?
                    properties = PlayerExtendedProperties.getFavoriteDestinations(ServerPlayerEntity);
                    break;
                }
            }
        }

        List<TeleportDestinationClientInfo> result = new ArrayList<>();
        for (TeleportDestination destination : destinations.values()) {
            TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(destination);
            BlockPos c = destination.getCoordinate();
            World world = WorldTools.getWorld(destination.getDimension());
            String dimName = null;
            if (world != null) {
                dimName = world.getDimension().getType().getRegistryName().getPath();// @todo 1.14DimensionManager.getProvider(destination.getDimension()).getDimensionType().getName();
            }

            // @todo
//            DimensionInformation information = RfToolsDimensionManager.getDimensionManager(getWorld()).getDimensionInformation(destination.getDimension());
//            if (information != null) {
//                dimName = information.getModuleName();
//            }
            if (dimName == null || dimName.trim().isEmpty()) {
                dimName = "Id " + destination.getDimension();
            } else {
                dimName = dimName + " (" + destination.getDimension() + ")";
            }
            destinationClientInfo.setDimensionName(dimName);

            if (world != null) {
                TileEntity te = world.getTileEntity(c);
                if (te instanceof MatterReceiverTileEntity) {
                    MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
                    if (playerName != null && !matterReceiverTileEntity.checkAccess(playerName)) {
                        // No access.
                        continue;
                    }
                }
            }
            if (properties != null) {
                destinationClientInfo.setFavorite(properties.isDestinationFavorite(new GlobalCoordinate(c, destination.getDimension())));
            }
            result.add(destinationClientInfo);
        }

        Collections.sort(result);

        return result;
    }

    /**
     * Check if the teleport destination is still valid.
     * @param destination
     * @return
     */
    public boolean isDestinationValid(TeleportDestination destination) {
        GlobalCoordinate key = new GlobalCoordinate(destination.getCoordinate(), destination.getDimension());
        return destinations.containsKey(key);
    }

    // Set an old id to a new position (after moving a receiver).
    public void assignId(GlobalCoordinate key, int id) {
        destinationById.put(id, key);
        destinationIdByCoordinate.put(key, id);
    }

    public int getNewId(GlobalCoordinate key) {
        if (destinationIdByCoordinate.containsKey(key)) {
            return destinationIdByCoordinate.get(key);
        }
        lastId++;
        destinationById.put(lastId, key);
        destinationIdByCoordinate.put(key, lastId);
        return lastId;
    }

    // Get the id from a coordinate.
    public Integer getIdForCoordinate(GlobalCoordinate key) {
        return destinationIdByCoordinate.get(key);
    }

    public GlobalCoordinate getCoordinateForId(int id) {
        return destinationById.get(id);
    }

    public TeleportDestination addDestination(GlobalCoordinate key) {
        if (!destinations.containsKey(key)) {
            TeleportDestination teleportDestination = new TeleportDestination(key.getCoordinate(), key.getDimension());
            destinations.put(key, teleportDestination);
        }
        return destinations.get(key);
    }

    public void removeDestinationsInDimension(int dimension) {
        Set<GlobalCoordinate> keysToRemove = new HashSet<>();
        for (Map.Entry<GlobalCoordinate, TeleportDestination> entry : destinations.entrySet()) {
            if (entry.getKey().getDimension() == dimension) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (GlobalCoordinate key : keysToRemove) {
            removeDestination(key.getCoordinate(), key.getDimension());
        }
    }

    public void removeDestination(BlockPos coordinate, int dimension) {
        if (coordinate == null) {
            return;
        }
        GlobalCoordinate key = new GlobalCoordinate(coordinate, dimension);
        destinations.remove(key);
        Integer id = destinationIdByCoordinate.get(key);
        if (id != null) {
            destinationById.remove(id);
            destinationIdByCoordinate.remove(key);
        }
    }

    public TeleportDestination getDestination(GlobalCoordinate coordinate) {
        return destinations.get(coordinate);
    }

    public TeleportDestination getDestination(BlockPos coordinate, int dimension) {
        return destinations.get(new GlobalCoordinate(coordinate, dimension));
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        destinations.clear();
        destinationById.clear();
        destinationIdByCoordinate.clear();
        lastId = tagCompound.getInt("lastId");
        readDestinationsFromNBT(tagCompound);
    }

    private void readDestinationsFromNBT(CompoundNBT tagCompound) {
        ListNBT lst = tagCompound.getList("destinations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.size() ; i++) {
            CompoundNBT tc = lst.getCompound(i);
            BlockPos c = new BlockPos(tc.getInt("x"), tc.getInt("y"), tc.getInt("z"));
            int dim = tc.getInt("dim");
            String name = tc.getString("name");

            TeleportDestination destination = new TeleportDestination(c, dim);
            destination.setName(name);
            GlobalCoordinate gc = new GlobalCoordinate(c, dim);
            destinations.put(gc, destination);

            int id;
            if (tc.contains("id")) {
                id = tc.getInt("id");
                destinationById.put(id, gc);
                destinationIdByCoordinate.put(gc, id);
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        writeDestinationsToNBT(tagCompound, destinations.values(), destinationIdByCoordinate);
        tagCompound.putInt("lastId", lastId);
        return tagCompound;
    }

    private static void writeDestinationsToNBT(CompoundNBT tagCompound, Collection<TeleportDestination> destinations,
                                              Map<GlobalCoordinate, Integer> coordinateToInteger) {
        ListNBT lst = new ListNBT();
        for (TeleportDestination destination : destinations) {
            CompoundNBT tc = new CompoundNBT();
            BlockPos c = destination.getCoordinate();
            tc.putInt("x", c.getX());
            tc.putInt("y", c.getY());
            tc.putInt("z", c.getZ());
            tc.putInt("dim", destination.getDimension());
            tc.putString("name", destination.getName());
            if (coordinateToInteger != null) {
                Integer id = coordinateToInteger.get(new GlobalCoordinate(c, destination.getDimension()));
                if (id != null) {
                    tc.putInt("id", id);
                }
            }
            lst.add(tc);
        }
        tagCompound.put("destinations", lst);
    }
}
