package com.mcjty.rftools.dimension;

import com.mcjty.rftools.items.dimlets.DimletEntry;
import com.mcjty.rftools.items.dimlets.DimletType;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

import java.util.*;

/**
 * A unique descriptor of a dimension.
 */
public class DimensionDescriptor {
    private final Map<DimletType, List<Integer>> attributeMap;
    private final int rfCreateCost;
    private final int rfMaintainCost;
    private final int tickCost;

    public DimensionDescriptor(Map<DimletType, List<Integer>> input) {
        attributeMap = new HashMap<DimletType, List<Integer>>();
        for (Map.Entry<DimletType,List<Integer>> me : input.entrySet()) {
            List<Integer> ids = new ArrayList<Integer>(me.getValue());
            Collections.sort(ids);
            attributeMap.put(me.getKey(), ids);
        }
        rfCreateCost = calculateCreationRfCost();
        rfMaintainCost = calculateMaintenanceRfCost();
        tickCost = calculateTickCost();
    }

    public DimensionDescriptor(NBTTagCompound tagCompound) {
        attributeMap = new HashMap<DimletType, List<Integer>>();
        for (DimletType type : DimletType.values()) {
            int[] dimlets = null;
            if (tagCompound.hasKey(type.getName())) {
                NBTTagIntArray tagIntArray = (NBTTagIntArray) tagCompound.getTag(type.getName());
                if (tagIntArray != null) {
                    dimlets = tagIntArray.func_150302_c();
                }
            }
            List<Integer> ids = new ArrayList<Integer>();
            if (dimlets != null) {
                for (int id : dimlets) {
                    ids.add(id);
                }
            }
            attributeMap.put(type, ids);
        }
        rfCreateCost = calculateCreationRfCost();
        rfMaintainCost = calculateMaintenanceRfCost();
        tickCost = calculateTickCost();
    }

    public int getRfCreateCost() {
        return rfCreateCost;
    }

    public int getRfMaintainCost() {
        return rfMaintainCost;
    }

    public int getTickCost() {
        return tickCost;
    }

    public List<Integer> getDimletsByType(DimletType type) {
        return attributeMap.get(type);
    }

    public void writeToNBT(NBTTagCompound tagCompound) {
        for (Map.Entry<DimletType,List<Integer>> me : attributeMap.entrySet()) {
            int[] arr = new int[me.getValue().size()];
            for (int i = 0 ; i < me.getValue().size() ; i++) {
                arr[i] = me.getValue().get(i);
            }
            NBTTagIntArray tagList = new NBTTagIntArray(arr);

            tagCompound.setTag(me.getKey().getName(), tagList);
        }
        tagCompound.setInteger("rfCreateCost", rfCreateCost);
        tagCompound.setInteger("rfMaintainCost", rfMaintainCost);
        tagCompound.setInteger("tickCost", tickCost);
        tagCompound.setInteger("ticksLeft", tickCost);

    }

    private int calculateCreationRfCost() {
        int rf = KnownDimletConfiguration.baseDimensionCreationCost;
        for (DimletType type : attributeMap.keySet()) {
            List<Integer> ids = attributeMap.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getRfCreateCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeRfCreateCost.get(type);
                    }
                    rf += cost;
                }
            }
        }
        return rf;
    }

    private int calculateMaintenanceRfCost() {
        int rf = KnownDimletConfiguration.baseDimensionMaintenanceCost;
        for (DimletType type : attributeMap.keySet()) {
            List<Integer> ids = attributeMap.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getRfMaintainCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeRfMaintainCost.get(type);
                    }
                    rf += cost;
                }
            }
        }
        return rf;
    }

    private int calculateTickCost() {
        int ticks = KnownDimletConfiguration.baseDimensionTickCost;
        for (DimletType type : attributeMap.keySet()) {
            List<Integer> ids = attributeMap.get(type);
            for (Integer id : ids) {
                DimletEntry entry = KnownDimletConfiguration.idToDimlet.get(id);
                if (entry != null) {
                    int cost = entry.getTickCost();
                    if (cost == -1) {
                        cost = KnownDimletConfiguration.typeTickCost.get(type);
                    }
                    ticks += cost;
                }
            }
        }
        return ticks;
    }
}
