package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.dimension.DimensionDescriptor;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.network.Argument;
import com.mcjty.varia.Coordinate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Vec3;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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

    // Server side: current dialing destination
    private TeleportDestination teleportDestination = null;

    private String name = null;
    private boolean privateAccess = false;
    private Set<String> allowedPlayers = new HashSet<String>();

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

    /**
     * Calculate the cost of doing a dial between a transmitter and a destination.
     * @param world
     * @param c1 the start coordinate
     * @param teleportDestination
     * @return
     */
    public static int calculateRFCost(World world, Coordinate c1, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return TeleportConfiguration.rfStartTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int rf = TeleportConfiguration.rfStartTeleportBaseLocal + (int)(TeleportConfiguration.rfStartTeleportDist * dist);
            if (rf > TeleportConfiguration.rfStartTeleportBaseDim) {
                rf = TeleportConfiguration.rfStartTeleportBaseDim;
            }
            return rf;
        }
    }

    /**
     * Calculate the time in ticks of doing a dial between a transmitter and a destination.
     * @param world
     * @param c1 the start coordinate
     * @param teleportDestination
     * @return
     */
    public static int calculateTime(World world, Coordinate c1, TeleportDestination teleportDestination) {
        if (world.provider.dimensionId != teleportDestination.getDimension()) {
            return TeleportConfiguration.timeTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int time = TeleportConfiguration.timeTeleportBaseLocal + (int)(TeleportConfiguration.timeTeleportDist * dist / 1000);
            if (time > TeleportConfiguration.timeTeleportBaseDim) {
                time = TeleportConfiguration.timeTeleportBaseDim;
            }
            return time;
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

        tagCompound.setBoolean("private", privateAccess);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : allowedPlayers) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    public TeleportDestination getTeleportDestination() {
        return teleportDestination;
    }

    public void setTeleportDestination(TeleportDestination teleportDestination) {
        this.teleportDestination = teleportDestination;
        markDirty();
    }

    /**
     * Return a number between 0 and 10 indicating the severity of the teleportation.
     * @return
     */
    private int calculateSeverity() {
        int severity = badTicks * 10 / totalTicks;
        if (mustInterrupt()) {
            // If an interrupt was done then severity is worse.
            severity += 2;
        }
        if (severity > 10) {
            severity = 10;
        }
        return severity;
    }

    private boolean applyBadEffectIfNeeded(int severity) {
        severity += calculateSeverity();
        if (severity > 10) {
            severity = 10;
        }
        if (severity <= 0) {
            return false;
        }

        if (TeleportConfiguration.teleportErrorVolume >= 0.01) {
            worldObj.playSoundAtEntity(teleportingPlayer, RFTools.MODID + ":teleport_error", TeleportConfiguration.teleportErrorVolume, 1.0f);
        }

        switch (severity) {
            case 2:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                break;
            case 3:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 100));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 4:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 0.5f);
                break;
            case 5:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 6:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 1.0f);
                break;
            case 7:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 300));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 200));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 8:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 300));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 2.0f);
                break;
            case 9:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 400));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 400));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 3.0f);
                break;
            case 10:
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.harm.getId(), 500));
                teleportingPlayer.addPotionEffect(new PotionEffect(Potion.wither.getId(), 500));
                teleportingPlayer.attackEntityFrom(DamageSource.generic, 3.0f);
                break;
        }
        return true;
    }


    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        // Every few times we check if the receiver is ok (if we're dialed).
        if (teleportDestination != null) {
            checkReceiverStatusCounter--;
            if (checkReceiverStatusCounter <= 0) {
                checkReceiverStatusCounter = 20;
                if (DialingDeviceTileEntity.isDestinationAnalyzerAvailable(worldObj, xCoord, yCoord, zCoord)) {
                    int meta = checkReceiverStatus();
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord+1, zCoord, meta, 2);
                } else {
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord+1, zCoord, TeleportBeamBlock.META_OK, 2);
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
        } else if (teleportDestination == null) {
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
        World w = DimensionManager.getWorld(teleportDestination.getDimension());
        // By default we will not check if the dimension is not loaded. Can be changed in config.
        if (w == null) {
            if (TeleportConfiguration.matterTransmitterLoadWorld == -1) {
                return TeleportBeamBlock.META_UNKNOWN;
            } else {
                w = MinecraftServer.getServer().worldServerForDimension(teleportDestination.getDimension());
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadWorld;
            }
        }
        Coordinate c = teleportDestination.getCoordinate();

        boolean exists = w.getChunkProvider().chunkExists(c.getX() >> 4, c.getZ() >> 4);
        if (!exists) {
            if (TeleportConfiguration.matterTransmitterLoadChunk == -1) {
                return TeleportBeamBlock.META_UNKNOWN;
            } else {
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadChunk;
            }
        }

        TileEntity tileEntity = w.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return TeleportBeamBlock.META_WARN;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

        int status = matterReceiverTileEntity.checkStatus();
        return (status == DialingDeviceTileEntity.DIAL_OK) ? TeleportBeamBlock.META_OK : TeleportBeamBlock.META_WARN;
    }

    private void clearTeleport(int cooldown) {
        markDirty();
        applyBadEffectIfNeeded(0);
        cooldownTimer = cooldown;
        teleportingPlayer = null;
    }

    private boolean isDestinationValid() {
        return teleportDestination != null && teleportDestination.isValid();
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
            applyBadEffectIfNeeded(10);
            RFTools.warn(teleportingPlayer, "Missing destination!");
            clearTeleport(200);
            return;
        }

        Coordinate c = teleportDestination.getCoordinate();

        int currentId = teleportingPlayer.worldObj.provider.dimensionId;
        if (currentId != teleportDestination.getDimension()) {
            WorldServer worldServerForDimension = MinecraftServer.getServer().worldServerForDimension(teleportDestination.getDimension());
            MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) teleportingPlayer, teleportDestination.getDimension(),
                    new RfToolsTeleporter(worldServerForDimension, c.getX()+0.5, c.getY()+1.5, c.getZ()+0.5));
        } else {
            teleportingPlayer.setPositionAndUpdate(c.getX()+0.5, c.getY()+1, c.getZ()+0.5);
        }

        RFTools.message(teleportingPlayer, "Whoosh!");

        int severity = consumeReceiverEnergy(c, teleportDestination.getDimension());
        if (!applyBadEffectIfNeeded(severity)) {
            if (TeleportConfiguration.teleportVolume >= 0.01) {
                worldObj.playSoundAtEntity(teleportingPlayer, RFTools.MODID + ":teleport_whoosh", TeleportConfiguration.teleportVolume, 1.0f);
            }
        }
        teleportingPlayer = null;
    }

    private boolean isDestinationStillValid() {
        return TeleportDestinations.getDestinations(worldObj).isDestinationValid(teleportDestination);
    }

    private void handleEnergyShortage() {
        markDirty();
        // Not enough energy. This is a bad tick.
        badTicks++;
        if (mustInterrupt()) {
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

    /**
     * Consume energy on the receiving side and return a number indicating how good this went.
     *
     * @param c
     * @param dimension
     * @return 0 in case of success. 10 in case of severe failure
     */
    private int consumeReceiverEnergy(Coordinate c, int dimension) {
        World world = DimensionManager.getWorld(dimension);
        TileEntity te = world.getTileEntity(c.getX(), c.getY(), c.getZ());
        if (!(te instanceof MatterReceiverTileEntity)) {
            RFTools.warn(teleportingPlayer, "Something went wrong with the destination!");
            return 0;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) te;
        int rf = TeleportConfiguration.rfPerTeleportReceiver;
        rf = (int) (rf * (2.0f - matterReceiverTileEntity.getInfusedFactor()) / 2.0f);

        int extracted = matterReceiverTileEntity.extractEnergy(ForgeDirection.DOWN, rf, false);
        return 10 - (extracted * 10 / rf);
    }

    private boolean mustInterrupt() {
        return badTicks > (totalTicks / 2);
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

        if (teleportDestination != null) {
            Coordinate cthis = new Coordinate(xCoord, yCoord, zCoord);
            int cost = calculateRFCost(worldObj, cthis, teleportDestination);
            cost = (int) (cost * (4.0f - getInfusedFactor()) / 4.0f);

            if (getEnergyStored(ForgeDirection.DOWN) < cost) {
                RFTools.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }
            extractEnergy(ForgeDirection.DOWN, cost, false);

            RFTools.message(player, "Start teleportation...");
            teleportingPlayer = player;
            teleportTimer = calculateTime(worldObj, cthis, teleportDestination);
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
}
