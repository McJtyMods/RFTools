package mcjty.rftools.blocks.storage;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.WorldTools;
import mcjty.lib.worlddata.AbstractWorldData;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import java.util.HashMap;
import java.util.Map;

public class RemoteStorageIdRegistry extends AbstractWorldData<RemoteStorageIdRegistry> {

    private static final String RFTOOLS_REMOTE_STORAGE = "RFToolsRemoteStorage";

    private int lastId = 0;
    private Map<Integer,GlobalCoordinate> storages = new HashMap<>();
    private Map<Integer,Long> lastPublishTime = new HashMap<>();

    public RemoteStorageIdRegistry(String name) {
        super(name);
    }

    @Override
    public void clear() {
        storages.clear();
        lastPublishTime.clear();
        lastId = 0;
    }

    public static RemoteStorageIdRegistry getRegistry(World world) {
        return getData(world, RemoteStorageIdRegistry.class, RFTOOLS_REMOTE_STORAGE);
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
        boolean exists = WorldTools.chunkLoaded(w, c);
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
            if (remoteStorageTileEntity.isGlobal(index) || world.provider.getDimension() == coordinate.getDimension()) {
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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }
}
