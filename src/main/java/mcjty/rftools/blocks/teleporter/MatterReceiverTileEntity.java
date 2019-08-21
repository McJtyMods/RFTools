package mcjty.rftools.blocks.teleporter;

import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftools.blocks.teleporter.TeleporterSetup.TYPE_MATTER_RECEIVER;

public class MatterReceiverTileEntity extends GenericTileEntity implements ITickableTileEntity {

    public static final String CMD_ADDPLAYER = "receiver.addPlayer";
    public static final String CMD_DELPLAYER = "receiver.delPlayer";
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);

    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    private String name = null;
    private boolean privateAccess = false;
    private Set<String> allowedPlayers = new HashSet<>();
    private int id = -1;

    public static final Key<String> VALUE_NAME = new Key<>("name", Type.STRING);
    public static final Key<Boolean> VALUE_PRIVATE = new Key<>("private", Type.BOOLEAN);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_NAME, this::getName, this::setName),
                new DefaultValue<>(VALUE_PRIVATE, this::isPrivateAccess, this::setPrivateAccess),
        };
    }

    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true,
            TeleportConfiguration.RECEIVER_MAXENERGY.get(), TeleportConfiguration.RECEIVER_RECEIVEPERTICK.get()));

    private BlockPos cachedPos;

    public MatterReceiverTileEntity() {
        super(TYPE_MATTER_RECEIVER);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public int getOrCalculateID() {
        if (id == -1) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());
            GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().getDimension().getType().getId());
            id = destinations.getNewId(gc);

            destinations.save();
            setId(id);
        }
        return id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
        markDirtyClient();
    }

    public void setName(String name) {
        this.name = name;
        TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());
        TeleportDestination destination = destinations.getDestination(getPos(), getWorld().getDimension().getType().getId());
        if (destination != null) {
            destination.setName(name);
            destinations.save();
        }

        markDirtyClient();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (!getPos().equals(cachedPos)) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());

            destinations.removeDestination(cachedPos, getWorld().getDimension().getType().getId());

            cachedPos = getPos();

            GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().getDimension().getType().getId());

            if (id == -1) {
                id = destinations.getNewId(gc);
            } else {
                destinations.assignId(gc, id);
            }
            destinations.addDestination(gc);
            destinations.save();

            markDirty();
        }
    }

    /**
     * This method is called after putting down a receiver that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());

        GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().getDimension().getType().getId());
        TeleportDestination destination = destinations.getDestination(gc.getCoordinate(), gc.getDimension());
        if (destination != null) {
            destination.setName(name);

            if (id == -1) {
                id = destinations.getNewId(gc);
                markDirty();
            } else {
                destinations.assignId(gc, id);
            }

            destinations.save();
        }
        markDirtyClient();
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
        markDirtyClient();
    }

    public boolean checkAccess(String player) {
        if (!privateAccess) {
            return true;
        }
        return allowedPlayers.contains(player);
    }

    public List<String> getAllowedPlayers() {
        return new ArrayList<>(allowedPlayers);
    }

    public void addPlayer(String player) {
        if (!allowedPlayers.contains(player)) {
            allowedPlayers.add(player);
            markDirtyClient();
        }
    }

    public void delPlayer(String player) {
        if (allowedPlayers.contains(player)) {
            allowedPlayers.remove(player);
            markDirtyClient();
        }
    }

    public int checkStatus() {
        BlockState state = getWorld().getBlockState(getPos().up());
        Block block = state.getBlock();
        if (!block.isAir(state, getWorld(), getPos().up())) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }
        block = getWorld().getBlockState(getPos().up(2)).getBlock();
        if (!block.isAir(state, getWorld(), getPos().up(2))) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }

        if (getStoredPower() < TeleportConfiguration.rfPerTeleportReceiver.get()) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_POWER_LOW_MASK;
        }

        return DialingDeviceTileEntity.DIAL_OK;
    }

    private int getStoredPower() {
        return energyHandler.map(h -> h.getEnergyStored()).orElse(0);
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        cachedPos = new BlockPos(tagCompound.getInt("cachedX"), tagCompound.getInt("cachedY"), tagCompound.getInt("cachedZ"));
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot table
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        name = tagCompound.getString("tpName");

        privateAccess = tagCompound.getBoolean("private");

        allowedPlayers.clear();
        ListNBT playerList = tagCompound.getList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0 ; i < playerList.size() ; i++) {
                String player = playerList.getString(i);
                allowedPlayers.add(player);
            }
        }
        if (tagCompound.contains("destinationId")) {
            id = tagCompound.getInt("destinationId");
        } else {
            id = -1;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        if (cachedPos != null) {
            tagCompound.putInt("cachedX", cachedPos.getX());
            tagCompound.putInt("cachedY", cachedPos.getY());
            tagCompound.putInt("cachedZ", cachedPos.getZ());
        }
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        if (name != null && !name.isEmpty()) {
            tagCompound.putString("tpName", name);
        }

        tagCompound.putBoolean("private", privateAccess);

        ListNBT playerTagList = new ListNBT();
        for (String player : allowedPlayers) {
            playerTagList.add(new StringNBT(player));
        }
        tagCompound.put("players", playerTagList);
        tagCompound.putInt("destinationId", id);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(params.get(PARAM_PLAYER));
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(params.get(PARAM_PLAYER));
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> rc = super.executeWithResultList(command, args, type);
        if (!rc.isEmpty()) {
            return rc;
        }
        if (CMD_GETPLAYERS.equals(command)) {
            return type.convert(getAllowedPlayers());
        }
        return Collections.emptyList();
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETPLAYERS.equals(command)) {
            GuiMatterReceiver.storeAllowedPlayersForClient(Type.STRING.convert(list));
            return true;
        }
        return false;
    }
}
