package mcjty.rftools.shapes;

import mcjty.lib.tools.WorldTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ScanDataManager extends WorldSavedData {

    public static final String SCANDATA_NETWORK_NAME = "RFToolsScanData";
    private static ScanDataManager instance = null;

    private int lastId = 0;

    private final Map<Integer,Scan> scans = new HashMap<>();

    public ScanDataManager(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        WorldTools.saveData(world, SCANDATA_NETWORK_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.scans.clear();
            instance = null;
        }
    }

    public static ScanDataManager getScans(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = mcjty.lib.tools.WorldTools.loadData(world, ScanDataManager.class, SCANDATA_NETWORK_NAME);
        if (instance == null) {
            instance = new ScanDataManager(SCANDATA_NETWORK_NAME);
        }
        return instance;
    }

    @Nonnull
    public Scan getOrCreateScan(int id) {
        Scan scan = scans.get(id);
        if (scan == null) {
            scan = new Scan();
            scans.put(id, scan);
        }
        return scan;
    }


    public Scan getScan(int id) {
        return scans.get(id);
    }

    public void deleteScan(int id) {
        scans.remove(id);
    }

    public int newScan() {
        lastId++;
        return lastId;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        scans.clear();
        NBTTagList lst = tagCompound.getTagList("scans", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("scan");
            Scan value = new Scan();
            value.setData(tc.getByteArray("data"));
            scans.put(id, value);
        }
        lastId = tagCompound.getInteger("lastId");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer, Scan> entry : scans.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("scan", entry.getKey());
            tc.setByteArray("data", entry.getValue().getData());
            lst.appendTag(tc);
        }
        tagCompound.setTag("scans", lst);
        tagCompound.setInteger("lastId", lastId);
        return tagCompound;
    }

    public static class Scan {
        private byte[] data;
        private final static byte[] EMPTY = new byte[0];

        public byte[] getData() {
            if (data == null) {
                return EMPTY;
            }
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
