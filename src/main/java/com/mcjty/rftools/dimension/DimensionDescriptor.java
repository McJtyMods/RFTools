package com.mcjty.rftools.dimension;

import com.mcjty.rftools.items.dimlets.DimletType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;

import java.util.*;

/**
 * A unique descriptor of a dimension.
 */
public class DimensionDescriptor {
    private final Map<DimletType, List<Integer>> attributeMap;

    public DimensionDescriptor(Map<DimletType, List<Integer>> input) {
        attributeMap = new HashMap<DimletType, List<Integer>>();
        for (Map.Entry<DimletType,List<Integer>> me : input.entrySet()) {
            List<Integer> ids = new ArrayList<Integer>(me.getValue());
            Collections.sort(ids);
            attributeMap.put(me.getKey(), ids);
        }
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

    }
}
