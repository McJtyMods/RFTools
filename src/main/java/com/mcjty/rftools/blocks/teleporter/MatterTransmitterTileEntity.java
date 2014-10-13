package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.common.DimensionManager;

import java.util.Map;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 1000000;
    public static final int RECEIVEPERTICK = 1000;

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_DIAL = "dial";
    public static final String CLIENTCMD_DIAL = "dialResult";

    // Client side
    private int dialResult;     // This result comes from the server and is read on the client in the GUI.

    // Server side: current dialing destination
    private TeleportDestination teleportDestination = null;

    private String name = null;

    // Server side: the player we're currently teleporting.
    private EntityPlayer teleportingPlayer = null;
    private int teleportTimer = 0;

    public MatterTransmitterTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        name = tagCompound.getString("tpName");
        Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
        if (c == null) {
            teleportDestination = null;
        } else {
            int dim = tagCompound.getInteger("dim");
            teleportDestination = new TeleportDestination(c, dim);
        }
        teleportTimer = tagCompound.getInteger("tpTimer");
        String playerName = tagCompound.getString("tpPlayer");
        if (playerName != null && !playerName.isEmpty()) {
            teleportingPlayer = worldObj.getPlayerEntityByName(playerName);
        } else {
            teleportingPlayer = null;
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }
        if (teleportDestination != null) {
            Coordinate c = teleportDestination.getCoordinate();
            if (c != null) {
                Coordinate.writeToNBT(tagCompound, "dest", c);
                tagCompound.setInteger("dim", teleportDestination.getDimension());
            }
        }
        tagCompound.setInteger("tpTimer", teleportTimer);
        if (teleportingPlayer != null) {
            tagCompound.setString("tpPlayer", teleportingPlayer.getDisplayName());
        }
    }

    private int dial(Coordinate coordinate, int dimension) {
        if (coordinate == null) {
            clearBeam(1, 4);
            teleportDestination = null;
            return DialingDeviceTileEntity.DIAL_OK;
        }

        TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
        teleportDestination = destinations.getDestination(coordinate, dimension);
        if (teleportDestination == null) {
            return DialingDeviceTileEntity.DIAL_INVALID_DESTINATION_MASK;
        }
        if (!makeBeam(1, 4, 2)) {
            teleportDestination = null;
            return DialingDeviceTileEntity.DIAL_TRANSMITTER_BLOCKED_MASK;
        }
        return DialingDeviceTileEntity.DIAL_OK;
    }

    private void clearBeam(int dy1, int dy2) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = worldObj.getBlock(xCoord, yCoord+dy, zCoord);
            if (ModBlocks.teleportBeamBlock.equals(b)) {
                worldObj.setBlockToAir(xCoord, yCoord+dy, zCoord);
            } else {
                return;
            }
        }
    }


    private boolean makeBeam(int dy1, int dy2, int errory) {
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = worldObj.getBlock(xCoord, yCoord+dy, zCoord);
            if ((!b.isAir(worldObj, xCoord, yCoord+dy, zCoord)) && !ModBlocks.teleportBeamBlock.equals(b)) {
                if (dy <= errory) {
                    // Everything below errory must be free.
                    return false;
                } else {
                    // Everything higher then errory doesn't have to be free.
                    break;
                }
            }
        }
        for (int dy = dy1 ; dy <= dy2 ; dy++) {
            Block b = worldObj.getBlock(xCoord, yCoord+dy, zCoord);
            if (b.isAir(worldObj, xCoord, yCoord+dy, zCoord) || ModBlocks.teleportBeamBlock.equals(b)) {
                worldObj.setBlock(xCoord, yCoord+dy, zCoord, ModBlocks.teleportBeamBlock, 0, 2);
            } else {
                break;
            }
        }
        return true;
    }

    public int getDialResult() {
        return dialResult;
    }

    public void setDialResult(int dialResult) {
        this.dialResult = dialResult;
    }

    public TeleportDestination getTeleportDestination() {
        return teleportDestination;
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();
        if (teleportingPlayer != null) {
            if (teleportDestination == null) {
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("The destination vanished! Aborting."));
                teleportingPlayer = null;
                return;
            }

            AxisAlignedBB playerBB = teleportingPlayer.boundingBox;
            AxisAlignedBB beamBB = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord+1, yCoord+2, zCoord+1);
            if (!playerBB.intersectsWith(beamBB)) {
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("Teleportation was interrupted!"));
                teleportingPlayer = null;
                return;
            }

            teleportTimer--;
            if (teleportTimer <= 0) {
                int currentId = teleportingPlayer.worldObj.provider.dimensionId;
                if (currentId != teleportDestination.getDimension()) {
                    MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) teleportingPlayer, teleportDestination.getDimension());
                }
                teleportingPlayer.addChatComponentMessage(new ChatComponentText("Whoosh!"));
                Coordinate c = teleportDestination.getCoordinate();
                teleportingPlayer.setPositionAndUpdate(c.getX(), c.getY()-2, c.getZ());
                teleportingPlayer = null;
            }
        }
    }

    public void startTeleportation(Entity entity) {
        if (teleportingPlayer != null) {
            // Already teleporting
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;

        if (teleportDestination != null) {
            player.addChatComponentMessage(new ChatComponentText("Start teleportation..."));
            teleportingPlayer = player;
            teleportTimer = 40;
        } else {
            player.addChatComponentMessage(new ChatComponentText("Something is wrong with the destination!"));
        }
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        }
        return false;
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_DIAL.equals(command)) {
            Coordinate c = args.get("c").getCoordinate();
            int dim = args.get("dim").getInteger();
            return dial(c, dim);
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_DIAL.equals(command)) {
            setDialResult(result);
            return true;
        }
        return false;
    }
}
