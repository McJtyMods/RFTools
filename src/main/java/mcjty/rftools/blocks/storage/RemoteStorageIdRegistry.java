package mcjty.rftools.blocks.storage;

import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;

public class RemoteStorageIdRegistry extends WorldSavedData {
    public static final String RFTOOLS_REMOTE_STORAGE = "RFToolsRemoteStorage";
    private static RemoteStorageIdRegistry instance = null;

    private int lastId = 0;
    private Map<Integer,GlobalCoordinate> storages = new HashMap<Integer, GlobalCoordinate>();
    private Map<Integer,Long> lastPublishTime = new HashMap<Integer, Long>();

    public RemoteStorageIdRegistry(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.getMapStorage().setData(RFTOOLS_REMOTE_STORAGE, this);
        markDirty();
    }

    public static void clearInstance() {
        instance = null;
    }

    public static RemoteStorageIdRegistry getRegistry(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (RemoteStorageIdRegistry) world.getMapStorage().loadData(RemoteStorageIdRegistry.class, RFTOOLS_REMOTE_STORAGE);
        if (instance == null) {
            instance = new RemoteStorageIdRegistry(RFTOOLS_REMOTE_STORAGE);
        }
        return instance;
    }

    public static RemoteStorageTileEntity getRemoteStorage(World world, int id) {
        RemoteStorageIdRegistry registry = RemoteStorageIdRegistry.getRegistry(world);
        if (registry == null) {
            return null;
        }
        GlobalCoordinate coordinate = registry.getStorage(id);
        if (coordinate == null) {
            return null;
        }
        World w = DimensionManager.getWorld(coordinate.getDimension());
        if (w == null) {
            return null;
        }
        BlockPos c = coordinate.getCoordinate();
        boolean exists = w.getChunkProvider().chunkExists(c.getX() >> 4, c.getZ() >> 4);
        if (!exists) {
            return null;
        }
        TileEntity te = w.getTileEntity(c);
        if (te instanceof RemoteStorageTileEntity) {
            RemoteStorageTileEntity remoteStorageTileEntity = (RemoteStorageTileEntity) te;
            int index = remoteStorageTileEntity.findRemoteIndex(id);
            if (index == -1) {
                return null;
            }
            if (remoteStorageTileEntity.isGlobal(index) || world.provider.getDimensionId() == coordinate.getDimension()) {
                return remoteStorageTileEntity;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }



    public void publishStorage(int id, GlobalCoordinate coordinate) {
        long time = System.currentTimeMillis();
        storages.put(id, coordinate);
        lastPublishTime.put(id, time);
    }

    public GlobalCoordinate getStorage(int id) {
        long time = System.currentTimeMillis();
        if (!storages.containsKey(id)) {
            return null;
        }
        long t = lastPublishTime.get(id);
        if (time > t+500) {
            // Too long ago.
            return null;
        }
        return storages.get(id);
    }

    public int getNewId() {
        lastId++;
        return lastId;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("lastId", lastId);
    }
}
