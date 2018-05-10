package mcjty.rftools.blocks.teleporter;

import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.bindings.IValue;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.GlobalCoordinate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import java.util.*;

public class MatterReceiverTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

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
    public IValue[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_NAME, MatterReceiverTileEntity::getName, MatterReceiverTileEntity::setName),
                new DefaultValue<>(VALUE_PRIVATE, MatterReceiverTileEntity::isPrivateAccess, MatterReceiverTileEntity::setPrivateAccess),
        };
    }

    private BlockPos cachedPos;

    public MatterReceiverTileEntity() {
        super(TeleportConfiguration.RECEIVER_MAXENERGY, TeleportConfiguration.RECEIVER_RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public int getOrCalculateID() {
        if (id == -1) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());
            GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().provider.getDimension());
            id = destinations.getNewId(gc);

            destinations.save(getWorld());
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
        TeleportDestination destination = destinations.getDestination(getPos(), getWorld().provider.getDimension());
        if (destination != null) {
            destination.setName(name);
            destinations.save(getWorld());
        }

        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        if (!getPos().equals(cachedPos)) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());

            destinations.removeDestination(cachedPos, getWorld().provider.getDimension());

            cachedPos = getPos();

            GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().provider.getDimension());

            if (id == -1) {
                id = destinations.getNewId(gc);
            } else {
                destinations.assignId(gc, id);
            }
            destinations.addDestination(gc);
            destinations.save(getWorld());

            markDirty();
        }
    }

    /**
     * This method is called after putting down a receiver that was earlier wrenched. We need to fix the data in
     * the destination.
     */
    public void updateDestination() {
        TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());

        GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().provider.getDimension());
        TeleportDestination destination = destinations.getDestination(gc.getCoordinate(), gc.getDimension());
        if (destination != null) {
            destination.setName(name);

            if (id == -1) {
                id = destinations.getNewId(gc);
                markDirty();
            } else {
                destinations.assignId(gc, id);
            }

            destinations.save(getWorld());
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
        IBlockState state = getWorld().getBlockState(getPos().up());
        Block block = state.getBlock();
        if (!block.isAir(state, getWorld(), getPos().up())) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }
        block = getWorld().getBlockState(getPos().up(2)).getBlock();
        if (!block.isAir(state, getWorld(), getPos().up(2))) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_BLOCKED_MASK;
        }

        if (getEnergyStored() < TeleportConfiguration.rfPerTeleportReceiver) {
            return DialingDeviceTileEntity.DIAL_RECEIVER_POWER_LOW_MASK;
        }

        return DialingDeviceTileEntity.DIAL_OK;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        cachedPos = new BlockPos(tagCompound.getInteger("cachedX"), tagCompound.getInteger("cachedY"), tagCompound.getInteger("cachedZ"));
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("tpName");

        privateAccess = tagCompound.getBoolean("private");

        allowedPlayers.clear();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0 ; i < playerList.tagCount() ; i++) {
                String player = playerList.getStringTagAt(i);
                allowedPlayers.add(player);
            }
        }
        if (tagCompound.hasKey("destinationId")) {
            id = tagCompound.getInteger("destinationId");
        } else {
            id = -1;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (cachedPos != null) {
            tagCompound.setInteger("cachedX", cachedPos.getX());
            tagCompound.setInteger("cachedY", cachedPos.getY());
            tagCompound.setInteger("cachedZ", cachedPos.getZ());
        }
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }

        tagCompound.setBoolean("private", privateAccess);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : allowedPlayers) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
        tagCompound.setInteger("destinationId", id);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
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
    public <T> boolean execute(String command, List<T> list, Type<T> type) {
        boolean rc = super.execute(command, list, type);
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
