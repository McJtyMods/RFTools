package com.mcjty.rftools.dimension;

import com.mcjty.rftools.CommonProxy;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.description.DimensionDescriptor;
import com.mcjty.rftools.dimension.network.PacketCheckDimletConfig;
import com.mcjty.rftools.dimension.network.PacketSyncDimensionInfo;
import com.mcjty.rftools.dimension.world.GenericWorldProvider;
import com.mcjty.rftools.items.dimlets.DimletKey;
import com.mcjty.rftools.items.dimlets.DimletMapping;
import com.mcjty.rftools.items.dimlets.KnownDimletConfiguration;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRegisterDimensions;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.Constants;

import java.io.File;
import java.util.*;

public class RfToolsDimensionManager extends WorldSavedData {
    public static final String DIMMANAGER_NAME = "RFToolsDimensionManager";
    private static RfToolsDimensionManager instance = null;

    private final Map<Integer, DimensionDescriptor> dimensions = new HashMap<Integer, DimensionDescriptor>();
    private final Map<DimensionDescriptor, Integer> dimensionToID = new HashMap<DimensionDescriptor, Integer>();
    private final Map<Integer, DimensionInformation> dimensionInformation = new HashMap<Integer, DimensionInformation>();

    private final Set<Integer> reclaimedIds = new HashSet<Integer>();

    public void syncFromServer(Map<Integer, DimensionDescriptor> dims, Map<Integer, DimensionInformation> dimInfo) {
        RFTools.log("RfToolsDimensionManager.syncFromServer");
        if (dims.isEmpty() || dimInfo.isEmpty()) {
            RFTools.log("This should not happen! Dimension information from server is empty? Trying to adapt...");
        }

        for (Map.Entry<Integer, DimensionDescriptor> entry : dims.entrySet()) {
            int id = entry.getKey();
            DimensionDescriptor descriptor = entry.getValue();
            if (dimensions.containsKey(id)) {
                dimensionToID.remove(dimensions.get(id));
            }
            dimensions.put(id, descriptor);
            dimensionToID.put(descriptor, id);
        }

        for (Map.Entry<Integer, DimensionInformation> entry : dimInfo.entrySet()) {
            int id = entry.getKey();
            DimensionInformation info = entry.getValue();
            dimensionInformation.put(id, info);
        }
    }

    public RfToolsDimensionManager(String identifier) {
        super(identifier);
    }

    public static void clearInstance() {
        if (instance != null) {
            instance.dimensions.clear();
            instance.dimensionToID.clear();
            instance.dimensionInformation.clear();
            instance.reclaimedIds.clear();
            instance = null;
        }
    }

    public static void cleanupDimensionInformation() {
        if (instance != null) {
            RFTools.log("Cleaning up RFTools dimensions");
            unregisterDimensions();
            instance.getDimensions().clear();
            instance.dimensionToID.clear();
            instance.dimensionInformation.clear();
            instance.reclaimedIds.clear();
            instance = null;
        }
    }

    public static void unregisterDimensions() {
        for (Map.Entry<Integer, DimensionDescriptor> me : instance.getDimensions().entrySet()) {
            int id = me.getKey();
            if (DimensionManager.isDimensionRegistered(id)) {
                RFTools.log("    Unregister dimension: " + id);
                try {
                    DimensionManager.unregisterDimension(id);
                } catch (Exception e) {
                    // We ignore this error.
                    RFTools.log("        Could not unregister dimension: " + id);
                }
                try {
                    DimensionManager.unregisterProviderType(id);
                } catch (Exception e) {
                    // We ignore this error.
                    RFTools.log("        Could not unregister provider: " + id);
                }
            } else {
                RFTools.log("    Already unregistered! Dimension: " + id);
            }
        }
    }

    public void save(World world) {
        world.mapStorage.setData(DIMMANAGER_NAME, this);
        markDirty();

        syncDimInfoToClients(world);
    }

    public void reclaimId(int id) {
        reclaimedIds.add(id);
    }

    /**
     * Check if the client dimlet id's match with the server.
     * This is executed on the server to the clients.
     */
    public void checkDimletConfig(EntityPlayer player) {
        if (!player.getEntityWorld().isRemote) {
            // Send over dimlet configuration to the client so that the client can check that the id's match.
            RFTools.log("Send validation data to the client");
            DimletMapping mapping = DimletMapping.getDimletMapping(player.getEntityWorld());
            Map<Integer, DimletKey> dimlets = new HashMap<Integer, DimletKey>();
            for (Integer id : mapping.getIds()) {
                dimlets.put(id, mapping.getKey(id));
            }

            PacketHandler.INSTANCE.sendTo(new PacketCheckDimletConfig(dimlets), (EntityPlayerMP) player);
        }
    }

    /**
     * Here the information from the server arrives. This code is executed on the client.
     */
    public void checkDimletConfigFromServer(Map<Integer, DimletKey> dimlets, World world) {
        RFTools.log("Getting dimlet mapping from server");
        DimletMapping mapping = DimletMapping.getDimletMapping(world);
        mapping.overrideServerMapping(dimlets);

        KnownDimletConfiguration.init(world);
        KnownDimletConfiguration.initCrafting(world);
    }


    public void syncDimInfoToClients(World world) {
        if (!world.isRemote) {
            // Sync to clients.
            RFTools.log("Sync dimension info to clients!");
            PacketHandler.INSTANCE.sendToAll(new PacketSyncDimensionInfo(dimensions, dimensionInformation));
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
            registerDimensionToServerAndClient(id);
        }
    }

    private void registerDimensionToServerAndClient(int id) {
        DimensionManager.registerProviderType(id, GenericWorldProvider.class, false);
        DimensionManager.registerDimension(id, id);
        PacketHandler.INSTANCE.sendToAll(new PacketRegisterDimensions(id));
    }

    public static RfToolsDimensionManager getDimensionManager(World world) {
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
     */
    public static World getWorldForDimension(int id) {
        World w = DimensionManager.getWorld(id);
        if (w == null) {
            w = MinecraftServer.getServer().getConfigurationManager().getServerInstance().worldServerForDimension(id);
        }
        return w;
    }

    public void removeDimension(int id) {
        DimensionDescriptor descriptor = dimensions.get(id);
        dimensions.remove(id);
        dimensionToID.remove(descriptor);
        dimensionInformation.remove(id);
        if (DimensionManager.isDimensionRegistered(id)) {
            DimensionManager.unregisterDimension(id);
        }
        DimensionManager.unregisterProviderType(id);
    }

    public void recoverDimension(World world, int id, DimensionDescriptor descriptor, String name) {
        if (!DimensionManager.isDimensionRegistered(id)) {
            registerDimensionToServerAndClient(id);
        }

        dimensions.put(id, descriptor);
        dimensionToID.put(descriptor, id);

        DimensionInformation dimensionInfo = new DimensionInformation(name, descriptor, world);
        dimensionInformation.put(id, dimensionInfo);

        save(world);
        touchSpawnChunk(id);
    }

    public int createNewDimension(World world, DimensionDescriptor descriptor, String name) {
        int id = 0;
        while (!reclaimedIds.isEmpty()) {
            int rid = reclaimedIds.iterator().next();
            reclaimedIds.remove(rid);
            if (!DimensionManager.isDimensionRegistered(rid)) {
                id = rid;
                break;
            }
        }
        if (id == 0) {
            id = DimensionManager.getNextFreeDimId();
        }

        registerDimensionToServerAndClient(id);
        RFTools.log("id = " + id + " for " + name + ", descriptor = " + descriptor.getDescriptionString());

        dimensions.put(id, descriptor);
        dimensionToID.put(descriptor, id);

        DimensionInformation dimensionInfo = new DimensionInformation(name, descriptor, world);
        dimensionInformation.put(id, dimensionInfo);

        save(world);

        touchSpawnChunk(id);
        return id;
    }

    private void touchSpawnChunk(int id) {
        // Make sure world generation kicks in for at least one chunk so that our matter receiver
        // is generated and registered.
        WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(id);
        ChunkProviderServer providerServer = worldServerForDimension.theChunkProviderServer;
        if (!providerServer.chunkExists(0, 0)) {
            try {
                providerServer.loadChunk(0, 0);
                providerServer.populate(providerServer, 0, 0);
                providerServer.unloadChunksIfNotNearSpawn(0, 0);
            } catch (Exception e) {
                RFTools.logError("Something went wrong during creation of the dimension!");
                e.printStackTrace();
                // We catch this exception to make sure our dimension tab is at least ok.
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        dimensions.clear();
        dimensionToID.clear();
        dimensionInformation.clear();
        reclaimedIds.clear();
        NBTTagList lst = tagCompound.getTagList("dimensions", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            int id = tc.getInteger("id");
            DimensionDescriptor descriptor = new DimensionDescriptor(tc);
            dimensions.put(id, descriptor);
            dimensionToID.put(descriptor, id);

            DimensionInformation dimensionInfo = new DimensionInformation(descriptor, tc);
            dimensionInformation.put(id, dimensionInfo);
        }

        int[] lstIds = tagCompound.getIntArray("reclaimedIds");
        for (int id : lstIds) {
            reclaimedIds.add(id);
        }

    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (Map.Entry<Integer,DimensionDescriptor> me : dimensions.entrySet()) {
            NBTTagCompound tc = new NBTTagCompound();

            Integer id = me.getKey();
            tc.setInteger("id", id);
            me.getValue().writeToNBT(tc);
            DimensionInformation dimensionInfo = dimensionInformation.get(id);
            dimensionInfo.writeToNBT(tc);

            lst.appendTag(tc);
        }
        tagCompound.setTag("dimensions", lst);

        List<Integer> ids = new ArrayList<Integer>(reclaimedIds);
        int[] lstIds = new int[ids.size()];
        for (int i = 0 ; i < ids.size() ; i++) {
            lstIds[i] = ids.get(i);
        }
        tagCompound.setIntArray("reclaimedIds", lstIds);
    }
}
