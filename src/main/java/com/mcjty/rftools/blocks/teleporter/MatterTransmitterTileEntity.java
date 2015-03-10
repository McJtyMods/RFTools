package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.dimlets.DimletConfiguration;
import com.mcjty.rftools.dimension.DimensionStorage;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";
    public static final String CMD_SETPRIVATE = "setAccess";
    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    // Server side: current dialing destination. Old system.
    private TeleportDestination teleportDestination = null;
    // Server side: current dialing destination. New system.
    private Integer teleportId = null;

    private String name = null;
    private boolean privateAccess = false;
    private Set<String> allowedPlayers = new HashSet<String>();
    private int status = TeleportationTools.STATUS_OK;

    // Server side: the player we're currently teleporting.
    private EntityPlayer teleportingPlayer = null;
    private int teleportTimer = 0;
    private int cooldownTimer = 0;
    private int totalTicks;
    private int goodTicks;
    private int badTicks;

    private int checkReceiverStatusCounter = 20;

    private AxisAlignedBB beamBox = null;

    public MatterTransmitterTileEntity() {
        super(TeleportConfiguration.TRANSMITTER_MAXENERGY, TeleportConfiguration.TRANSMITTER_RECEIVEPERTICK);
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public boolean checkAccess(String player) {
        if (!privateAccess) {
            return true;
        }
        return allowedPlayers.contains(player);
    }

    public int getStatus() {
        return status;
    }

    public List<PlayerName> getAllowedPlayers() {
        List<PlayerName> p = new ArrayList<PlayerName>();
        for (String player : allowedPlayers) {
            p.add(new PlayerName(player));
        }
        return p;
    }

    public void addPlayer(String player) {
        if (!allowedPlayers.contains(player)) {
            allowedPlayers.add(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    public void delPlayer(String player) {
        if (allowedPlayers.contains(player)) {
            allowedPlayers.remove(player);
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        teleportTimer = tagCompound.getInteger("tpTimer");
        cooldownTimer = tagCompound.getInteger("cooldownTimer");
        totalTicks = tagCompound.getInteger("totalTicks");
        goodTicks = tagCompound.getInteger("goodTicks");
        badTicks = tagCompound.getInteger("badTicks");
        String playerName = tagCompound.getString("tpPlayer");
        if (playerName != null && !playerName.isEmpty()) {
            teleportingPlayer = worldObj.getPlayerEntityByName(playerName);
        } else {
            teleportingPlayer = null;
        }
        status = tagCompound.getInteger("status");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("tpName");
        Coordinate c = Coordinate.readFromNBT(tagCompound, "dest");
        if (c == null) {
            teleportDestination = null;
        } else {
            int dim = tagCompound.getInteger("dim");
            teleportDestination = new TeleportDestination(c, dim);
        }
        if (tagCompound.hasKey("destId")) {
            teleportId = tagCompound.getInteger("destId");
        } else {
            teleportId = null;
        }
        privateAccess = tagCompound.getBoolean("private");

        allowedPlayers.clear();
        NBTTagList playerList = tagCompound.getTagList("players", Constants.NBT.TAG_STRING);
        if (playerList != null) {
            for (int i = 0 ; i < playerList.tagCount() ; i++) {
                String player = playerList.getStringTagAt(i);
                allowedPlayers.add(player);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("tpTimer", teleportTimer);
        tagCompound.setInteger("cooldownTimer", cooldownTimer);
        tagCompound.setInteger("totalTicks", totalTicks);
        tagCompound.setInteger("goodTicks", goodTicks);
        tagCompound.setInteger("badTicks", badTicks);
        if (teleportingPlayer != null) {
            tagCompound.setString("tpPlayer", teleportingPlayer.getDisplayName());
        }
        tagCompound.setInteger("status", status);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
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
        if (teleportId != null) {
            tagCompound.setInteger("destId", teleportId);
        }

        tagCompound.setBoolean("private", privateAccess);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : allowedPlayers) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    public boolean isDialed() {
        return teleportId != null || teleportDestination != null;
    }

    public TeleportDestination getTeleportDestination() {
        if (teleportId != null) {
            TeleportDestinations teleportDestinations = TeleportDestinations.getDestinations(worldObj);
            GlobalCoordinate gc = teleportDestinations.getCoordinateForId(teleportId);
            if (gc == null) {
                return null;
            } else {
                return teleportDestinations.getDestination(gc.getCoordinate(), gc.getDimension());
            }
        }
        return teleportDestination;
    }

    public void setTeleportDestination(TeleportDestination teleportDestination) {
        this.teleportDestination = null;
        this.teleportId = null;
        if (teleportDestination != null) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(worldObj);
            Integer id = destinations.getIdForCoordinate(new GlobalCoordinate(teleportDestination.getCoordinate(), teleportDestination.getDimension()));
            if (id == null) {
                this.teleportDestination = teleportDestination;
            } else {
                this.teleportId = id;
            }
        }
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        markDirty();
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        // Every few times we check if the receiver is ok (if we're dialed).
        if (teleportDestination != null || teleportId != null) {
            checkReceiverStatusCounter--;
            if (checkReceiverStatusCounter <= 0) {
                checkReceiverStatusCounter = 20;
                int newstatus;
                if (DialingDeviceTileEntity.isDestinationAnalyzerAvailable(worldObj, xCoord, yCoord, zCoord)) {
                    newstatus = checkReceiverStatus();
                } else {
                    newstatus = TeleportationTools.STATUS_OK;
                }
                if (newstatus != status) {
                    status = newstatus;
                    markDirty();
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            }
        }

        if (isCoolingDown()) {
            // We're still in cooldown. Do nothing.
            return;
        } else if (teleportingPlayer == null) {
            // If we have a valid destination we check here if there is a player on this transmitter.
            if (isDestinationValid()) {
                searchForNearestPlayer();
            }
        } else if (teleportDestination == null && teleportId == null) {
            // We were teleporting a player but for some reason the destination went away. Interrupt.
            RFTools.warn(teleportingPlayer, "The destination vanished! Aborting.");
            clearTeleport(80);
        } else if (isPlayerOutsideBeam()) {
            // The player moved outside the beam. Interrupt the teleport.
            clearTeleport(80);
        } else {
            int rf = TeleportConfiguration.rfTeleportPerTick;
            rf = (int) (rf * (4.0f - getInfusedFactor()) / 4.0f);

            if (getEnergyStored(ForgeDirection.DOWN) < rf) {
                // We don't have enough energy to handle this tick.
                handleEnergyShortage();
            } else {
                // We have enough energy so this is a good tick.
                markDirty();
                extractEnergy(ForgeDirection.DOWN, rf, false);
                goodTicks++;

                teleportTimer--;
                if (teleportTimer <= 0) {
                    performTeleport();
                }
            }
        }
    }

    // Server side only
    private int checkReceiverStatus() {
        TeleportDestination destination = getTeleportDestination();
        if (destination == null) {
            return TeleportationTools.STATUS_WARN;
        }

        int dimension = destination.getDimension();

        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);
        if (dimensionManager.getDimensionInformation(dimension) != null) {
            // This is an RFTools dimension. Check power.
            DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(worldObj);
            int energyLevel = dimensionStorage.getEnergyLevel(dimension);
            if (energyLevel < DimletConfiguration.DIMPOWER_WARN_TP) {
                return TeleportationTools.STATUS_WARN;
            }
        }


        World w = DimensionManager.getWorld(dimension);
        // By default we will not check if the dimension is not loaded. Can be changed in config.
        if (w == null) {
            if (TeleportConfiguration.matterTransmitterLoadWorld == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                w = MinecraftServer.getServer().worldServerForDimension(dimension);
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadWorld;
            }
        }
        Coordinate c = destination.getCoordinate();

        boolean exists = w.getChunkProvider().chunkExists(c.getX() >> 4, c.getZ() >> 4);
        if (!exists) {
            if (TeleportConfiguration.matterTransmitterLoadChunk == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadChunk;
            }
        }

        TileEntity tileEntity = w.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return TeleportationTools.STATUS_WARN;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

        int status = matterReceiverTileEntity.checkStatus();
        return (status == DialingDeviceTileEntity.DIAL_OK) ? TeleportationTools.STATUS_OK : TeleportationTools.STATUS_WARN;
    }

    private void clearTeleport(int cooldown) {
        markDirty();
        TeleportationTools.applyBadEffectIfNeeded(teleportingPlayer, 0, badTicks, totalTicks, false);
        cooldownTimer = cooldown;
        teleportingPlayer = null;
    }

    private boolean isDestinationValid() {
        return teleportId != null || (teleportDestination != null && teleportDestination.isValid());
    }

    private boolean isCoolingDown() {
        markDirty();
        cooldownTimer--;
        if (cooldownTimer <= 0) {
            cooldownTimer = 0;
        } else {
            return true;
        }
        return false;
    }

    private void searchForNearestPlayer() {
        if (beamBox == null) {
            beamBox = AxisAlignedBB.getBoundingBox(xCoord, yCoord+1, zCoord, xCoord+1, yCoord+3, zCoord+1);
        }

        List<Entity> l = worldObj.getEntitiesWithinAABB(EntityPlayer.class, beamBox);
        Entity nearestPlayer = findNearestPlayer(l);

        if (nearestPlayer == null) {
            cooldownTimer = 5;
            return;
        }
        AxisAlignedBB playerBB = nearestPlayer.boundingBox;
        if (playerBB.intersectsWith(beamBox)) {
            startTeleportation(nearestPlayer);
        } else {
            cooldownTimer = 5;
        }
    }

    private Entity findNearestPlayer(List<Entity> l) {
        Entity nearestPlayer = null;
        double dmax = Double.MAX_VALUE;
        for (Entity entity : l) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;

            if ((!isPrivateAccess()) || allowedPlayers.contains(entityPlayer.getDisplayName())) {
                double d1 = entity.getDistanceSq(xCoord + .5, yCoord + 1.5, zCoord + .5);

                if (d1 <= dmax) {
                    nearestPlayer = entity;
                    dmax = d1;
                }
            }
        }
        return nearestPlayer;
    }

    private void performTeleport() {
        // First check if the destination is still valid.
        if (!isDestinationStillValid()) {
            TeleportationTools.applyBadEffectIfNeeded(teleportingPlayer, 10, badTicks, totalTicks, false);
            RFTools.warn(teleportingPlayer, "Missing destination!");
            clearTeleport(200);
            return;
        }

        TeleportDestination dest = getTeleportDestination();

        boolean boosted = DialingDeviceTileEntity.isMatterBoosterAvailable(worldObj, xCoord, yCoord, zCoord);
        if (boosted && getEnergyStored(ForgeDirection.DOWN) < TeleportConfiguration.rfBoostedTeleport) {
            // Not enough energy. We cannot do a boosted teleport.
            boosted = false;
        }
        boolean boostNeeded = TeleportationTools.performTeleport(teleportingPlayer, dest, badTicks, totalTicks, boosted);
        if (boostNeeded) {
            extractEnergy(ForgeDirection.DOWN, TeleportConfiguration.rfBoostedTeleport, false);
        }

        teleportingPlayer = null;
    }

    private boolean isDestinationStillValid() {
        TeleportDestination dest = getTeleportDestination();
        return TeleportDestinations.getDestinations(worldObj).isDestinationValid(dest);
    }

    private void handleEnergyShortage() {
        markDirty();
        // Not enough energy. This is a bad tick.
        badTicks++;
        if (TeleportationTools.mustInterrupt(badTicks, totalTicks)) {
            // Too many bad ticks. Total failure!
            RFTools.warn(teleportingPlayer, "Power failure during transit!");
            clearTeleport(200);
        }
        return;
    }

    private boolean isPlayerOutsideBeam() {
        AxisAlignedBB playerBB = teleportingPlayer.boundingBox;
        if (!playerBB.intersectsWith(beamBox)) {
            RFTools.message(teleportingPlayer, "Teleportation was interrupted!");
            return true;
        }
        return false;
    }

    public void startTeleportation(Entity entity) {
        if (cooldownTimer > 0) {
            // In cooldown. We can't do teleport right now.
            return;
        }
        if (teleportingPlayer != null) {
            // Already teleporting
            return;
        }
        if (!(entity instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) entity;

        TeleportDestination dest = teleportDestination;
        if (teleportId != null) {
            dest = getTeleportDestination();
        }

        if (dest != null) {
            Coordinate cthis = new Coordinate(xCoord, yCoord, zCoord);
            int cost = TeleportationTools.calculateRFCost(worldObj, cthis, dest);
            cost = (int) (cost * (4.0f - getInfusedFactor()) / 4.0f);

            if (getEnergyStored(ForgeDirection.DOWN) < cost) {
                RFTools.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }
            extractEnergy(ForgeDirection.DOWN, cost, false);

            RFTools.message(player, "Start teleportation...");
            teleportingPlayer = player;
            teleportTimer = TeleportationTools.calculateTime(worldObj, cthis, dest);
            teleportTimer = (int) (teleportTimer * (2.0f - getInfusedFactor()) / 2.0f);

            totalTicks = teleportTimer;
            goodTicks = 0;
            badTicks = 0;
        } else {
            RFTools.warn(player, "Something is wrong with the destination!");
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
        } else if (CMD_SETPRIVATE.equals(command)) {
            setPrivateAccess(args.get("private").getBoolean());
            return true;
        } else if (CMD_ADDPLAYER.equals(command)) {
            addPlayer(args.get("player").getString());
            return true;
        } else if (CMD_DELPLAYER.equals(command)) {
            delPlayer(args.get("player").getString());
            return true;
        }
        return false;
    }

    @Override
    public List executeWithResultList(String command, Map<String, Argument> args) {
        List rc = super.executeWithResultList(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETPLAYERS.equals(command)) {
            return getAllowedPlayers();
        }
        return null;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETPLAYERS.equals(command)) {
            GuiMatterTransmitter.storeAllowedPlayersForClient(list);
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 4, zCoord + 1);
    }
}
