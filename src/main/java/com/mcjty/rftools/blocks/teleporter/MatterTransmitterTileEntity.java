package com.mcjty.rftools.blocks.teleporter;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
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
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class MatterTransmitterTileEntity extends GenericEnergyHandlerTileEntity {

    public static int MAXENERGY = 100000;
    public static int RECEIVEPERTICK = 1000;

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";
    public static final String CMD_SETPRIVATE = "setAccess";
    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    public static int horizontalDialerRange = 10;           // Horizontal range the dialing device can check for transmitters
    public static int verticalDialerRange = 5;              // Vertical range the dialing device can check for transmitters

    public static int rfPerDial = 1000;                     // RF Consumed by dialing device when making a new dial
    public static int rfPerCheck = 5000;                    // RF Used to do a check on a receiver.
    public static int rfDialedConnectionPerTick = 10;       // RF Consumed by transmitter when a dial is active and not doing anything else

    // The following flags are used to calculate power usage for even starting a teleport. The rfStartTeleportBaseDim (cost of
    // teleporting to another dimension) is also the cap of the local teleport which is calculated by doing
    // rfStartTelelportBaseLocal + dist * rfStartTeleportDist
    public static int rfStartTeleportBaseLocal = 5000;      // Base RF consumed by transmitter when starting a teleport in same dimension
    public static int rfStartTeleportBaseDim = 100000;      // Base RF consumed by transmitter when starting a teleport to another dimension
    public static int rfStartTeleportDist = 10;             // RF per distance unit when starting a teleport
    public static int rfTeleportPerTick = 500;              // During the time the teleport is busy this RF is used per tick on the transmitter

    public static int rfPerTeleportReceiver = 5000;         // On the receiver side we need this amount of power

    // The following flags are used to calculate the time used for doing the actual teleportation. Same principle as with
    // the power usage above with regards to local/dimensional teleport.
    public static int timeTeleportBaseLocal = 5;
    public static int timeTeleportBaseDim = 100;
    public static int timeTeleportDist = 10;                // Value in militicks (1000 == 1 tick)

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

    private AxisAlignedBB beamBox = null;

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

    public List<String> getClientAllowedPlayers() {
        return new ArrayList<String> (allowedPlayers);
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
            return rfStartTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int rf = rfStartTeleportBaseLocal + (int)(rfStartTeleportDist * dist);
            if (rf > rfStartTeleportBaseDim) {
                rf = rfStartTeleportBaseDim;
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
            return timeTeleportBaseDim;
        } else {
            Coordinate c2 = teleportDestination.getCoordinate();
            double dist = Vec3.createVectorHelper(c1.getX(), c1.getY(), c1.getZ()).distanceTo(Vec3.createVectorHelper(c2.getX(), c2.getY(), c2.getZ()));
            int time = timeTeleportBaseLocal + (int)(timeTeleportDist * dist / 1000);
            if (time > timeTeleportBaseDim) {
                time = timeTeleportBaseDim;
            }
            return time;
        }
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
        tagCompound.setInteger("cooldownTimer", cooldownTimer);
        tagCompound.setInteger("totalTicks", totalTicks);
        tagCompound.setInteger("goodTicks", goodTicks);
        tagCompound.setInteger("badTicks", badTicks);
        if (teleportingPlayer != null) {
            tagCompound.setString("tpPlayer", teleportingPlayer.getDisplayName());
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

    private void applyBadEffectIfNeeded(int severity) {
        severity += calculateSeverity();
        if (severity > 10) {
            severity = 10;
        }
        switch (severity) {
            case 0:
                break;
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
    }


    @Override
    protected void checkStateServer() {
        super.checkStateServer();

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
        } else if (getEnergyStored(ForgeDirection.DOWN) < rfTeleportPerTick) {
            // We don't have enough energy to handle this tick.
            handleEnergyShortage();
        } else {
            // We have enough energy so this is a good tick.
            extractEnergy(ForgeDirection.DOWN, rfTeleportPerTick, false);
            goodTicks++;

            teleportTimer--;
            if (teleportTimer <= 0) {
                performTeleport();
            }
        }
    }

    private void clearTeleport(int cooldown) {
        applyBadEffectIfNeeded(0);
        cooldownTimer = cooldown;
        teleportingPlayer = null;
    }

    private boolean isDestinationValid() {
        return teleportDestination != null && teleportDestination.isValid();
    }

    private boolean isCoolingDown() {
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
        int currentId = teleportingPlayer.worldObj.provider.dimensionId;
        if (currentId != teleportDestination.getDimension()) {
            MinecraftServer.getServer().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) teleportingPlayer, teleportDestination.getDimension());
        }

        Coordinate c = teleportDestination.getCoordinate();
        RFTools.message(teleportingPlayer, "Whoosh!");
        teleportingPlayer.setPositionAndUpdate(c.getX(), c.getY()+1, c.getZ());
        int severity = consumeReceiverEnergy(c, teleportDestination.getDimension());
        applyBadEffectIfNeeded(severity);
        teleportingPlayer = null;
    }

    private void handleEnergyShortage() {
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
        int extracted = matterReceiverTileEntity.extractEnergy(ForgeDirection.DOWN, rfPerTeleportReceiver, false);
        return 10 - (extracted * 10 / rfPerTeleportReceiver);
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
            if (getEnergyStored(ForgeDirection.DOWN) < cost) {
                RFTools.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }
            extractEnergy(ForgeDirection.DOWN, cost, false);

            RFTools.message(player, "Start teleportation...");
            teleportingPlayer = player;
            teleportTimer = calculateTime(worldObj, cthis, teleportDestination);
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

    public void storeAllowedPlayersForClient(List<PlayerName> players) {
        Set<String> p = new HashSet<String>();
        for (PlayerName n : players) {
            p.add(n.getName());
        }
        this.allowedPlayers = p;
    }

    @Override
    public boolean execute(String command, List list) {
        boolean rc = super.execute(command, list);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETPLAYERS.equals(command)) {
            storeAllowedPlayersForClient(list);
            return true;
        }
        return false;
    }
}
