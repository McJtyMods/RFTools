package mcjty.rftools.blocks.storage;

import mcjty.varia.GlobalCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

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
        world.mapStorage.setData(RFTOOLS_REMOTE_STORAGE, this);
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
        instance = (RemoteStorageIdRegistry) world.mapStorage.loadData(RemoteStorageIdRegistry.class, RFTOOLS_REMOTE_STORAGE);
        if (instance == null) {
            instance = new RemoteStorageIdRegistry(RFTOOLS_REMOTE_STORAGE);
        }
        return instance;
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
