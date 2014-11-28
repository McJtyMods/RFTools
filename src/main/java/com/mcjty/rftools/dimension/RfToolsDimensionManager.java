package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RfToolsDimensionManager extends WorldSavedData {
    public static final String DIMMANAGER_NAME = "RFToolsDimensionManager";
    private static RfToolsDimensionManager instance = null;

    private final Map<Integer, DimensionDescriptor> dimensions = new HashMap<Integer, DimensionDescriptor>();
    private final Map<DimensionDescriptor, Integer> dimensionToID = new HashMap<DimensionDescriptor, Integer>();

    public RfToolsDimensionManager(String identifier) {
        super(identifier);
    }

    public static void clearInstance() {
        instance = null;
    }

    public void save(World world) {
        world.mapStorage.setData(DIMMANAGER_NAME, this);
        markDirty();
    }

    public Map<Integer, DimensionDescriptor> getDimensions() {
        return dimensions;
    }

    public void registerDimensions() {
        RFTools.log("Registering RFTools dimensions");
        System.out.println("com.mcjty.rftools.dimension.RfToolsDimensionManager.registerDimensions");
        for (Map.Entry<Integer, DimensionDescriptor> me : dimensions.entrySet()) {
            int id = me.getKey();
            RFTools.log("    Dimension: "+id);
            System.out.println("id = " + id);
            DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
            DimensionManager.registerDimension(id, id);
        }
    }

    public static RfToolsDimensionManager getDimensionManager(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (RfToolsDimensionManager) world.mapStorage.loadData(RfToolsDimensionManager.class, DIMMANAGER_NAME);
        if (instance == null) {
            instance = new RfToolsDimensionManager(DIMMANAGER_NAME);
        }
        return instance;
    }

    public DimensionDescriptor getDimensionDescriptor(int id) {
        return dimensions.get(id);
    }

    public Integer getDimensionID(DimensionDescriptor descriptor) {
        return dimensionToID.get(descriptor);
    }

    public void createNewDimension(World world, DimensionDescriptor descriptor, String name) {
        int id = DimensionManager.getNextFreeDimId();
        DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
        DimensionManager.registerDimension(id, id);
        System.out.println("id = " + id + " for " + name);

        dimensions.put(id, descriptor);
        dimensionToID.put(descriptor, id);

        save(world);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        dimensions.clear();
        dimensionToID.clear();
        NBTTagList lst = tagCompound.getTagList("dimensions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("id");
            DimensionDescriptor descriptor = new DimensionDescriptor(tc);
            dimensions.put(id, descriptor);
            dimensionToID.put(descriptor, id);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("id", me.getKey());
            me.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("dimensions", lst);
    }
}
