package mcjty.rftools.blocks.teleporter;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.playerprops.FavoriteDestinationsProperties;
import mcjty.rftools.playerprops.PlayerExtendedProperties;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
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
        world.getMapStorage().setData(TPDESTINATIONS_NAME, this);
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
            World transWorld = TeleportationTools.getWorldForDimension(world, key.getDimension());
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

    public static TeleportDestinations getDestinations(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (TeleportDestinations) world.getMapStorage().loadData(TeleportDestinations.class, TPDESTINATIONS_NAME);
        if (instance == null) {
            instance = new TeleportDestinations(TPDESTINATIONS_NAME);
        }
        return instance;
    }


    // Server side only
    public Collection<TeleportDestinationClientInfo> getValidDestinations(World worldObj, String playerName) {
        FavoriteDestinationsProperties properties = null;
        if (playerName != null) {
            List<EntityPlayerMP> list = worldObj.getMinecraftServer().getPlayerList().getPlayerList();
            for (EntityPlayerMP entityplayermp : list) {
                if (playerName.equals(entityplayermp.getName())) {
                    properties = PlayerExtendedProperties.getFavoriteDestinations(entityplayermp);
                    break;
                }
            }
        }

        List<TeleportDestinationClientInfo> result = new ArrayList<TeleportDestinationClientInfo>();
        for (TeleportDestination destination : destinations.values()) {
            TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(destination);
            BlockPos c = destination.getCoordinate();
            World world = DimensionManager.getWorld(destination.getDimension());
            String dimName = null;
            if (world != null) {
                dimName = DimensionManager.getProvider(destination.getDimension()).getDimensionType().getName();
            }

            // @todo
//            DimensionInformation information = RfToolsDimensionManager.getDimensionManager(worldObj).getDimensionInformation(destination.getDimension());
//            if (information != null) {
//                dimName = information.getName();
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
            BlockPos c = new BlockPos(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
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
            BlockPos c = destination.getCoordinate();
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
