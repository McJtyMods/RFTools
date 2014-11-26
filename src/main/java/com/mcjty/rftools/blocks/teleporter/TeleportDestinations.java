package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;

import java.util.*;

public class TeleportDestinations extends WorldSavedData {
    public static final String TPDESTINATIONS_NAME = "TPDestinations";
    private static TeleportDestinations instance = null;

    private final Map<TeleportDestinationKey,TeleportDestination> destinations = new HashMap<TeleportDestinationKey,TeleportDestination>();

    public TeleportDestinations(String identifier) {
        super(identifier);
    }

    public void save(World world) {
        world.mapStorage.setData(TPDESTINATIONS_NAME, this);
        markDirty();
    }

    public static void clearInstance() {
        instance = null;
    }

    public static TeleportDestinations getDestinations(World world) {
        if (world.isRemote) {
            return null;
        }
        if (instance != null) {
            return instance;
        }
        instance = (TeleportDestinations) world.mapStorage.loadData(TeleportDestinations.class, TPDESTINATIONS_NAME);
        if (instance == null) {
            instance = new TeleportDestinations(TPDESTINATIONS_NAME);
        }
        return instance;
    }


    // Server side only
    public Collection<TeleportDestinationClientInfo> getValidDestinations() {
        List<TeleportDestinationClientInfo> result = new ArrayList<TeleportDestinationClientInfo>();
        for (TeleportDestination destination : destinations.values()) {
            TeleportDestinationClientInfo destinationClientInfo = new TeleportDestinationClientInfo(destination);
            String dimName = DimensionManager.getProvider(destination.getDimension()).getDimensionName();
            destinationClientInfo.setDimensionName(dimName);
            result.add(destinationClientInfo);
        }
        return result;
    }

    /**
     * Check if the teleport destination is still valid.
     * @param destionation
     * @return
     */
    public boolean isDestinationValid(TeleportDestination destination) {
        TeleportDestinationKey key = new TeleportDestinationKey(destination.getCoordinate(), destination.getDimension());
        return destinations.containsKey(key);
    }

    public TeleportDestination addDestination(Coordinate coordinate, int dimension) {
        TeleportDestinationKey key = new TeleportDestinationKey(coordinate, dimension);
        if (!destinations.containsKey(key)) {
            TeleportDestination teleportDestination = new TeleportDestination(coordinate, dimension);
            destinations.put(key, teleportDestination);
        }
        return destinations.get(key);
    }

    public void removeDestination(Coordinate coordinate, int dimension) {
        TeleportDestinationKey key = new TeleportDestinationKey(coordinate, dimension);
        destinations.remove(key);
    }

    public TeleportDestination getDestination(Coordinate coordinate, int dimension) {
        return destinations.get(new TeleportDestinationKey(coordinate, dimension));
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        destinations.clear();
        NBTTagList lst = tagCompound.getTagList("destinations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            Coordinate c = new Coordinate(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            int dim = tc.getInteger("dim");
            String name = tc.getString("name");

            TeleportDestination destination = new TeleportDestination(c, dim);
            destination.setName(name);
            destinations.put(new TeleportDestinationKey(c, dim), destination);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (TeleportDestination destination : destinations.values()) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("x", destination.getCoordinate().getX());
            tc.setInteger("y", destination.getCoordinate().getY());
            tc.setInteger("z", destination.getCoordinate().getZ());
            tc.setInteger("dim", destination.getDimension());
            tc.setString("name", destination.getName());
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
    }
}
