package com.mcjty.rftools.items.dimlets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.*;

/**
 * Mapping between id's and dimlets. Persisted in the world data.
 */
public class DimletMapping extends WorldSavedData {
    public static final String DIMLETMAPPING_NAME = "RFToolsDimletMapping";
    private static DimletMapping instance = null;


    // This map keeps track of all known dimlets by id. Also the reverse map.
    private final Map<Integer, DimletEntry> idToDimlet = new HashMap<Integer, DimletEntry>();
    private final Map<DimletKey, Integer> dimletToID = new HashMap<DimletKey, Integer>();

    public DimletMapping(String identifier) {
        super(identifier);
    }

    public static <T> void remapIdsInMap(Map<Integer, Integer> mapFromTo, Map<Integer,T> baseMap) {
        Map<Integer,T> oldMap = new HashMap<Integer, T>(baseMap);
        baseMap.clear();
        for (Map.Entry<Integer, T> entry : oldMap.entrySet()) {
            Integer id = entry.getKey();
            T de = entry.getValue();
            if (mapFromTo.containsKey(id)) {
                baseMap.put(mapFromTo.get(id), de);
            } else {
                baseMap.put(id, de);
            }
        }
    }

    public static <T> void remapIdsInMapReversed(Map<Integer, Integer> mapFromTo, Map<T,Integer> baseMap) {
        Map<T,Integer> oldMap = new HashMap<T,Integer>(baseMap);
        baseMap.clear();
        for (Map.Entry<T, Integer> entry : oldMap.entrySet()) {
            Integer id = entry.getValue();
            T de = entry.getKey();
            if (mapFromTo.containsKey(id)) {
                baseMap.put(de, mapFromTo.get(id));
            } else {
                baseMap.put(de, id);
            }
        }
    }

    public static void remapIdsInSet(Map<Integer, Integer> mapFromTo, Set<Integer> baseSet) {
        Set<Integer> oldSet = new HashSet<Integer>(baseSet);
        baseSet.clear();
        for (Integer id : oldSet) {
            if (mapFromTo.containsKey(id)) {
                baseSet.add(mapFromTo.get(id));
            } else {
                baseSet.add(id);
            }
        }
    }

    public static void remapIdsInList(Map<Integer, Integer> mapFromTo, List<Integer> baseList) {
        List<Integer> oldList = new ArrayList<Integer>(baseList);
        baseList.clear();
        for (Integer id : oldList) {
            if (mapFromTo.containsKey(id)) {
                baseList.add(mapFromTo.get(id));
            } else {
                baseList.add(id);
            }
        }
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

    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {

    }

    public void remapIds(Map<Integer, Integer> mapFromTo) {
        remapIdsInMap(mapFromTo, idToDimlet);
        remapIdsInMapReversed(mapFromTo, dimletToID);
    }

    public void registerDimletEntry(int id, DimletEntry dimletEntry) {
        idToDimlet.put(id, dimletEntry);
        dimletToID.put(dimletEntry.getKey(), id);
    }

    public int getId(DimletType type, String name) {
        return dimletToID.get(new DimletKey(type, name));
    }

    public int getId(DimletKey key) {
        return dimletToID.get(key);
    }

    public DimletEntry getEntry(int id) {
        return idToDimlet.get(id);
    }

    public Set<Map.Entry<Integer, DimletEntry>> getEntries() {
        return idToDimlet.entrySet();
    }

    public Set<Integer> getKeys() {
        return idToDimlet.keySet();
    }
}