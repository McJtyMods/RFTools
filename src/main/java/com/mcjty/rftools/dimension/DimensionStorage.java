package com.mcjty.rftools.dimension;

import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class DimensionStorage extends WorldSavedData {
    public static final String DIMSTORAGE_NAME = "RFToolsDimensionStorage";
    private static DimensionStorage instance = null;

    private final Map<Integer,Integer> energy = new HashMap<Integer, Integer>();

    public static void clearInstance() {
        instance = null;
    }

    public DimensionStorage(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(DIMSTORAGE_NAME, this);
        markDirty();
    }

    public void syncFromServer(Map<Integer, Integer> energy) {
        this.energy.clear();
        this.energy.putAll(energy);
    }

    public static DimensionStorage getDimensionStorage(World world) {
        if (instance != null) {
            return instance;
        }
        instance = (DimensionStorage) world.mapStorage.loadData(DimensionStorage.class, DIMSTORAGE_NAME);
        if (instance == null) {
            instance = new DimensionStorage(DIMSTORAGE_NAME);
        }
        return instance;
    }

    public int getEnergyLevel(int id) {
        if (energy.containsKey(id)) {
            return energy.get(id);
        } else {
            return 0;
        }
    }

    public void setEnergyLevel(int id, int energyLevel) {
        energy.put(id, energyLevel);
    }

    public void removeDimension(int id) {
        energy.remove(id);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        energy.clear();
        NBTTagList lst = tagCompound.getTagList("dimensions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("id");
            int rf = tc.getInteger("energy");
            energy.put(id, rf);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer,Integer> me : energy.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("id", me.getKey());
            tc.setInteger("energy", me.getValue());
            lst.appendTag(tc);
        }
        tagCompound.setTag("dimensions", lst);
    }
}
