package com.mcjty.rftools.dimension;

import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import com.mcjty.rftools.network.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldManager;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.HashMap;
import java.util.Map;

public class RfToolsDimensionManager extends WorldSavedData {
    public static final String DIMMANAGER_NAME = "RFToolsDimensionManager";
    private static RfToolsDimensionManager instance = null;

    private final Map<Integer, DimensionDescriptor> dimensions = new HashMap<Integer, DimensionDescriptor>();
    private final Map<DimensionDescriptor, Integer> dimensionToID = new HashMap<DimensionDescriptor, Integer>();
    private final Map<Integer, DimensionInformation> dimensionInformation = new HashMap<Integer, DimensionInformation>();

    public void syncFromServer(Map<Integer, DimensionDescriptor> dimensions, Map<DimensionDescriptor, Integer> dimensionToID, Map<Integer, DimensionInformation> dimensionInformation) {
        this.dimensions.clear();
        this.dimensions.putAll(dimensions);
        this.dimensionToID.clear();
        this.dimensionToID.putAll(dimensionToID);
        this.dimensionInformation.clear();
        this.dimensionInformation.putAll(dimensionInformation);
    }

    public RfToolsDimensionManager(String identifier) {
        super(identifier);
    }

    public static void clearInstance() {
        instance = null;
    }

    public void save(World world) {
        world.mapStorage.setData(DIMMANAGER_NAME, this);
        markDirty();

        if (!world.isRemote) {
            // Sync to client.
            PacketHandler.INSTANCE.sendToAll(new PacketSyncDimensionInfo(dimensions, dimensionToID, dimensionInformation));
        }
    }

    public Map<Integer, DimensionDescriptor> getDimensions() {
        return dimensions;
    }

    public void registerDimensions() {
        RFTools.log("Registering RFTools dimensions");
        for (Map.Entry<Integer, DimensionDescriptor> me : dimensions.entrySet()) {
            int id = me.getKey();
            RFTools.log("    Dimension: " + id);
            DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
            DimensionManager.registerDimension(id, id);
        }
    }

    public static RfToolsDimensionManager getDimensionManager(World world) {
//        if (world.isRemote) {
//            return null;
//        }
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

    public DimensionInformation getDimensionInformation(int id) {
        return dimensionInformation.get(id);
    }

    /**
     * Get a world for a dimension, possibly loading it from the configuration manager.
     * @param id
     * @return
     */
    public World getWorldForDimension(int id) {
        World w = DimensionManager.getWorld(id);
        if (w == null) {
            WorldServer worldServer = MinecraftServer.getServer().getConfigurationManager().getServerInstance().worldServerForDimension(id);
            w = worldServer;
        }
        return w;
    }

    public void removeDimension(int id) {
        DimensionDescriptor descriptor = dimensions.get(id);
        dimensions.remove(id);
        dimensionToID.remove(descriptor);
        dimensionInformation.remove(id);
    }

    public int createNewDimension(World world, DimensionDescriptor descriptor, String name) {
        int id = DimensionManager.getNextFreeDimId();
        DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
        DimensionManager.registerDimension(id, id);
        System.out.println("id = " + id + " for " + name);

        dimensions.put(id, descriptor);
        dimensionToID.put(descriptor, id);

        DimensionInformation dimensionInfo = new DimensionInformation(name, descriptor);
        dimensionInformation.put(id, dimensionInfo);

        save(world);
        return id;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        dimensions.clear();
        dimensionToID.clear();
        dimensionInformation.clear();
        NBTTagList lst = tagCompound.getTagList("dimensions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("id");
            DimensionDescriptor descriptor = new DimensionDescriptor(tc);
            dimensions.put(id, descriptor);
            dimensionToID.put(descriptor, id);

            String name = tc.getString("name");
            DimensionInformation dimensionInfo = new DimensionInformation(name, descriptor);
            dimensionInformation.put(id, dimensionInfo);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();
            Integer id = me.getKey();
            tc.setInteger("id", id);
            DimensionInformation dimensionInfo = dimensionInformation.get(id);
            tc.setString("name", dimensionInfo.getName());
            me.getValue().writeToNBT(tc);
            lst.appendTag(tc);
        }
        tagCompound.setTag("dimensions", lst);
    }
}
