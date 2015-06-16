package mcjty.rftools.blocks.teleporter;

import mcjty.rftools.playerprops.PlayerExtendedProperties;
import mcjty.rftools.RFTools;
import mcjty.rftools.dimension.RfToolsDimensionManager;
import mcjty.varia.Coordinate;
import mcjty.varia.GlobalCoordinate;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class TeleportDestinations extends WorldSavedData {
    public static final String TPDESTINATIONS_NAME = "TPDestinations";
    private static TeleportDestinations instance = null;

    private final Map<GlobalCoordinate,TeleportDestination> destinations = new HashMap<GlobalCoordinate,TeleportDestination>();
    private final Map<Integer,GlobalCoordinate> destinationById = new HashMap<Integer, GlobalCoordinate>();
    private final Map<GlobalCoordinate,Integer> destinationIdByCoordinate = new HashMap<GlobalCoordinate, Integer>();
    private int lastId = 0;

    public TeleportDestinations(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(TPDESTINATIONS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.destinations.clear();
            instance.destinationById.clear();
            instance.destinationIdByCoordinate.clear();
            instance = null;
        }
    }

    public void cleanupInvalid(World world) {
        Set<GlobalCoordinate> keys = new HashSet<GlobalCoordinate>(destinations.keySet());
        for (GlobalCoordinate key : keys) {
            World transWorld = RfToolsDimensionManager.getDimensionManager(world).getWorldForDimension(key.getDimension());
            boolean removed = false;
            if (transWorld == null) {
                RFTools.log("Receiver on dimension " + key.getDimension() + " removed because world can't be loaded!");
                removed = true;
            } else {
                Coordinate c = key.getCoordinate();
                TileEntity te;
                try {
                    te = transWorld.getTileEntity(c.getX(), c.getY(), c.getZ());
                } catch (Exception e) {
                    te = null;
                }
                if (!(te instanceof MatterReceiverTileEntity)) {
                    RFTools.log("Receiver at " + c + " on dimension " + key.getDimension() + " removed because there is no receiver there!");
                    removed = true;
                }
            }
            if (removed) {
                destinations.remove(key);
            }
        }
    }

    public static TeleportDestinations getDestinations(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (TeleportDestinations) world.mapStorage.loadData(TeleportDestinations.class, TPDESTINATIONS_NAME);
        if (instance == null) {
            instance = new TeleportDestinations(TPDESTINATIONS_NAME);
        }
        return instance;
    }


    // Server side only
    public Collection<TeleportDestinationClientInfo> getValidDestinations(String playerName) {
        PlayerExtendedProperties properties = null;
        if (playerName != null) {
            List list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
            for (Object player : list) {
                EntityPlayerMP entityplayermp = (EntityPlayerMP) player;
                if (playerName.equals(entityplayermp.getDisplayName())) {
                    properties = PlayerExtendedProperties.getProperties(entityplayermp);
                    break;
                }
            }
        }

        List<TeleportDestinationClientInfo> result = new ArrayList<TeleportDestinationClientInfo>();
        for (TeleportDestination destination : destinations.values()) {
            TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(destination);
            Coordinate c = destination.getCoordinate();
            World world = DimensionManager.getWorld(destination.getDimension());
            if (world != null) {
                String dimName = DimensionManager.getProvider(destination.getDimension()).getDimensionName();
                if (dimName == null || dimName.trim().isEmpty()) {
                    dimName = "Id " + destination.getDimension();
                }
                destinationClientInfo.setDimensionName(dimName);
                TileEntity te = world.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof MatterReceiverTileEntity) {
                    MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
                    if (playerName != null && !matterReceiverTileEntity.checkAccess(playerName)) {
                        // No access.
                        continue;
                    }
                }
            }
            if (properties != null) {
                destinationClientInfo.setFavorite(properties.getFavoriteDestinationsProperties().isDestinationFavorite(new GlobalCoordinate(c, destination.getDimension())));
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
        Set<GlobalCoordinate> keysToRemove = new HashSet<GlobalCoordinate>();
        for (Map.Entry<GlobalCoordinate, TeleportDestination> entry : destinations.entrySet()) {
            if (entry.getKey().getDimension() == dimension) {
                keysToRemove.add(entry.getKey());
            }
        }
        for (GlobalCoordinate key : keysToRemove) {
            removeDestination(key.getCoordinate(), key.getDimension());
        }
    }

    public void removeDestination(Coordinate coordinate, int dimension) {
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

    public TeleportDestination getDestination(Coordinate coordinate, int dimension) {
        return destinations.get(new GlobalCoordinate(coordinate, dimension));
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        destinations.clear();
        destinationById.clear();
        destinationIdByCoordinate.clear();
        lastId = tagCompound.getInteger("lastId");
        readDestinationsFromNBT(tagCompound);
    }

    private void readDestinationsFromNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = tagCompound.getTagList("destinations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            Coordinate c = new Coordinate(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            int dim = tc.getInteger("dim");
            String name = tc.getString("name");

            TeleportDestination destination = new TeleportDestination(c, dim);
            destination.setName(name);
            GlobalCoordinate gc = new GlobalCoordinate(c, dim);
            destinations.put(gc, destination);

            int id;
            if (tc.hasKey("id")) {
                id = tc.getInteger("id");
                destinationById.put(id, gc);
                destinationIdByCoordinate.put(gc, id);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        writeDestinationsToNBT(tagCompound, destinations.values(), destinationIdByCoordinate);
        tagCompound.setInteger("lastId", lastId);
    }

    private static void writeDestinationsToNBT(NBTTagCompound tagCompound, Collection<TeleportDestination> destinations,
                                              Map<GlobalCoordinate, Integer> coordinateToInteger) {
        NBTTagList lst = new NBTTagList();
        for (TeleportDestination destination : destinations) {
            NBTTagCompound tc = new NBTTagCompound();
            Coordinate c = destination.getCoordinate();
            tc.setInteger("x", c.getX());
            tc.setInteger("y", c.getY());
            tc.setInteger("z", c.getZ());
            tc.setInteger("dim", destination.getDimension());
            tc.setString("name", destination.getName());
            if (coordinateToInteger != null) {
                Integer id = coordinateToInteger.get(new GlobalCoordinate(c, destination.getDimension()));
                if (id != null) {
                    tc.setInteger("id", id);
                }
            }
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
    }
}
