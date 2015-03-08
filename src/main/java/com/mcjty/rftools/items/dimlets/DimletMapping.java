package com.mcjty.rftools.items.dimlets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mapping between id's and dimlets. Persisted in the world data.
 */
public class DimletMapping extends WorldSavedData {
    public static final String DIMLETMAPPING_NAME = "RFToolsDimletMapping";
    private static DimletMapping instance = null;

    // This map keeps track of all known dimlets by id. Also the reverse map.
    private final Map<Integer, DimletKey> idToDimlet = new HashMap<Integer, DimletKey>();
    private final Map<DimletKey, Integer> dimletToID = new HashMap<DimletKey, Integer>();

    public DimletMapping(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(DIMLETMAPPING_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        if (instance != null) {
            instance = null;
        }
    }

    public static boolean isInitialized() {
        if (DimletMapping.getInstance() == null) {
            return false;
        }
        return !DimletMapping.instance.idToDimlet.isEmpty();
    }

    public void clear() {
        idToDimlet.clear();
        dimletToID.clear();
    }

    public static DimletMapping getDimletMapping(World world) {
        if (instance != null) {
            return instance;
        }
        instance = (DimletMapping) world.mapStorage.loadData(DimletMapping.class, DIMLETMAPPING_NAME);
        if (instance == null) {
            instance = new DimletMapping(DIMLETMAPPING_NAME);
        }
        return instance;
    }

    public static DimletMapping getInstance() {
        return instance;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        idToDimlet.clear();
        dimletToID.clear();
        int[] ids = tagCompound.getIntArray("ids");
        int[] types = tagCompound.getIntArray("types");
        for (int i = 0 ; i < ids.length ; i++) {
            String s = tagCompound.getString("n" + i);
            DimletKey key = new DimletKey(DimletType.values()[types[i]], s);
            idToDimlet.put(ids[i], key);
            dimletToID.put(key, ids[i]);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        int[] ids = new int[idToDimlet.size()];
        int[] types = new int[idToDimlet.size()];
        int idx = 0;
        for (Map.Entry<Integer, DimletKey> entry : idToDimlet.entrySet()) {
            ids[idx] = entry.getKey();
            types[idx] = entry.getValue().getType().ordinal();
            tagCompound.setString("n" + idx, entry.getValue().getName());
            idx++;
        }
        tagCompound.setIntArray("ids", ids);
        tagCompound.setIntArray("types", types);
    }

    public void registerDimletKey(int id, DimletKey key) {
        idToDimlet.put(id, key);
        dimletToID.put(key, id);
    }

    public int getId(DimletType type, String name) {
        return dimletToID.get(new DimletKey(type, name));
    }

    public Integer getId(DimletKey key) {
        return dimletToID.get(key);
    }

    public DimletKey getKey(int id) {
        return idToDimlet.get(id);
    }

    public void removeId(int id) {
        DimletKey key = idToDimlet.get(id);
        if (key != null) {
            idToDimlet.remove(key);
        }
        idToDimlet.remove(id);
    }

    public Set<Map.Entry<Integer, DimletKey>> getEntries() {
        return idToDimlet.entrySet();
    }

    public Set<Integer> getIds() {
        return idToDimlet.keySet();
    }

    public Set<DimletKey> getKeys() {
        return dimletToID.keySet();
    }

    public void overrideServerMapping(Map<Integer, DimletKey> dimlets) {
        idToDimlet.clear();
        dimletToID.clear();
        for (Map.Entry<Integer, DimletKey> entry : dimlets.entrySet()) {
            idToDimlet.put(entry.getKey(), entry.getValue());
            dimletToID.put(entry.getValue(), entry.getKey());
        }
    }
}