package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TeleportDestinations extends WorldSavedData {
    public static final String TPDESTINATIONS_NAME = "TPDestinations";

    private final List<TeleportDestination> destinations = new ArrayList<TeleportDestination>();

    public TeleportDestinations(String identifier) {
        super(identifier);
    }

    public static void saveDestinations(World world, TeleportDestinations destinations) {
        world.perWorldStorage.setData(TPDESTINATIONS_NAME, destinations);
    }

    public static TeleportDestinations getDestinations(World world) {
        TeleportDestinations destinations = (TeleportDestinations) world.perWorldStorage.loadData(TeleportDestinations.class, TPDESTINATIONS_NAME);
        if (destinations == null) {
            destinations = new TeleportDestinations(TPDESTINATIONS_NAME);
        }
        return destinations;
    }

    public List<TeleportDestination> getValidDestinations(String player) {
        List<TeleportDestination> validDestinations = new ArrayList<TeleportDestination>();
        for (TeleportDestination destination : destinations) {
            Set<String> allowedPlayers = destination.getAllowedPlayers();
            if (allowedPlayers == null || allowedPlayers.isEmpty()) {
                validDestinations.add(destination);
            } else {
                if (allowedPlayers.contains(player)) {
                    validDestinations.add(destination);
                }
            }
        }
        return validDestinations;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        destinations.clear();
        NBTTagList lst = tagCompound.getTagList("destinations", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < lst.tagCount() ; i++) {
            NBTTagCompound tc = lst.getCompoundTagAt(i);
            Coordinate c = new Coordinate(tc.getInteger("x"), tc.getInteger("y"), tc.getInteger("z"));
            int dim = tc.getInteger("dim");

            Set<String> allowedPlayers = readPlayerList(tc);

            TeleportDestination destination = new TeleportDestination(c, dim, allowedPlayers);
            destinations.add(destination);
        }
    }

    private Set<String> readPlayerList(NBTTagCompound tagCompound) {
        Set<String> allowedPlayers = new HashSet<String>();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < playerList.tagCount() ; i++) {
            NBTTagCompound tc = playerList.getCompoundTagAt(i);
            allowedPlayers.add(tc.getString("player"));
        }
        return allowedPlayers;
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        NBTTagList lst = new NBTTagList();
        for (TeleportDestination destination : destinations) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setInteger("x", destination.getCoordinate().getX());
            tc.setInteger("y", destination.getCoordinate().getY());
            tc.setInteger("z", destination.getCoordinate().getZ());
            tc.setInteger("dim", destination.getDimension());
            writePlayerList(tc, destination.getAllowedPlayers());
            lst.appendTag(tc);
        }
        tagCompound.setTag("destinations", lst);
    }

    private void writePlayerList(NBTTagCompound tagCompound, Set<String> allowedPlayers) {
        NBTTagList lst = new NBTTagList();
        for (String player : allowedPlayers) {
            NBTTagCompound tc = new NBTTagCompound();
            tc.setString("player", player);
            lst.appendTag(tc);
        }
        tagCompound.setTag("players", lst);
    }
}
