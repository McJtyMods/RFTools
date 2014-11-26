package com.mcjty.rftools.dimension;

import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.blocks.teleporter.TeleportDestination;
import com.mcjty.rftools.blocks.teleporter.TeleportDestinations;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
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

        World newWorld = WorldProvider.getProviderForDimension(id).worldObj;
        System.out.println("newWorld = " + newWorld);
        World newWorld2 = DimensionManager.getWorld(id);
        System.out.println("newWorld2 = " + newWorld2);
        newWorld2.setBlock(0, 70, 0, ModBlocks.matterReceiverBlock, 0, 2);

        TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
        TeleportDestination destination = destinations.addDestination(new Coordinate(0, 70, 0), id);
        destination.setName(name);
        destinations.save(world);

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
