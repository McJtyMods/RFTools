package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.varia.RFToolsTools;
import mcjty.typed.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.*;

public class MatterTransmitterTileEntity extends GenericEnergyReceiverTileEntity implements MachineInformation, ITickable {

    public static final String CMD_SETNAME = "setName";
    public static final String CMD_ADDPLAYER = "addPlayer";
    public static final String CMD_DELPLAYER = "delPlayer";
    public static final String CMD_SETPRIVATE = "setAccess";
    public static final String CMD_SETBEAM = "setBeam";
    public static final String CMD_GETPLAYERS = "getPlayers";
    public static final String CLIENTCMD_GETPLAYERS = "getPlayers";

    private static final String[] TAGS = new String[]{"dim", "coord", "name"};
    private static final String[] TAG_DESCRIPTIONS = new String[]{"The dimension this transmitter is dialed too", "The coordinate this transmitter is dialed too", "The name of the destination"};

    // Server side: current dialing destination. Old system.
    private TeleportDestination teleportDestination = null;
    // Server side: current dialing destination. New system.
    private Integer teleportId = null;
    // If this is true the dial is cleared as soon as a player teleports.
    private boolean once = false;

    private String name = null;
    private boolean privateAccess = false;
    private boolean beamHidden = false;
    private Set<String> allowedPlayers = new HashSet<>();
    private int status = TeleportationTools.STATUS_OK;

    // Server side: the player we're currently teleporting.
    private String teleportingPlayer = null;
    private int teleportTimer = 0;
    private int cooldownTimer = 0;
    private int totalTicks;
    private int goodTicks;
    private int badTicks;
    private int rfPerTick = 0;

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
        markDirtyClient();
    }

    public boolean isPrivateAccess() {
        return privateAccess;
    }

    public void setPrivateAccess(boolean privateAccess) {
        this.privateAccess = privateAccess;
        markDirtyClient();
    }

    public boolean isBeamHidden() {
        return beamHidden;
    }

    public void setBeamHidden(boolean b) {
        this.beamHidden = b;
        markDirtyClient();
    }

    public boolean isOnce() {
        return once;
    }

    @Override
    public int getTagCount() {
        return TAGS.length;
    }

    @Override
    public String getTagName(int index) {
        return TAGS[index];
    }

    @Override
    public String getTagDescription(int index) {
        return TAG_DESCRIPTIONS[index];
    }

    @Override
    public String getData(int index, long millis) {
        TeleportDestination destination = getTeleportDestination();
        if (destination == null) {
            return "<not dialed>";
        }
        switch (index) {
            case 0: return Integer.toString(destination.getDimension());
            case 1: return destination.getCoordinate().toString();
            case 2: return destination.getName();
        }
        return null;
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

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        teleportTimer = tagCompound.getInteger("tpTimer");
        cooldownTimer = tagCompound.getInteger("cooldownTimer");
        totalTicks = tagCompound.getInteger("totalTicks");
        goodTicks = tagCompound.getInteger("goodTicks");
        badTicks = tagCompound.getInteger("badTicks");
        teleportingPlayer = tagCompound.getString("tpPlayer");
        if (teleportingPlayer.isEmpty()) {
            teleportingPlayer = null;
        }
        status = tagCompound.getInteger("status");
        rfPerTick = tagCompound.getInteger("rfPerTick");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        name = tagCompound.getString("tpName");
        BlockPos c = BlockPosTools.readFromNBT(tagCompound, "dest");
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
        beamHidden = tagCompound.getBoolean("hideBeam");
        once = tagCompound.getBoolean("once");

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
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("tpTimer", teleportTimer);
        tagCompound.setInteger("cooldownTimer", cooldownTimer);
        tagCompound.setInteger("totalTicks", totalTicks);
        tagCompound.setInteger("goodTicks", goodTicks);
        tagCompound.setInteger("badTicks", badTicks);
        if (teleportingPlayer != null) {
            tagCompound.setString("tpPlayer", teleportingPlayer);
        }
        tagCompound.setInteger("status", status);
        tagCompound.setInteger("rfPerTick", rfPerTick);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        if (name != null && !name.isEmpty()) {
            tagCompound.setString("tpName", name);
        }
        if (teleportDestination != null) {
            BlockPos c = teleportDestination.getCoordinate();
            if (c != null) {
                BlockPosTools.writeToNBT(tagCompound, "dest", c);
                tagCompound.setInteger("dim", teleportDestination.getDimension());
            }
        }
        if (teleportId != null) {
            tagCompound.setInteger("destId", teleportId);
        }

        tagCompound.setBoolean("private", privateAccess);
        tagCompound.setBoolean("hideBeam", beamHidden);
        tagCompound.setBoolean("once", once);

        NBTTagList playerTagList = new NBTTagList();
        for (String player : allowedPlayers) {
            playerTagList.appendTag(new NBTTagString(player));
        }
        tagCompound.setTag("players", playerTagList);
    }

    public boolean isDialed() {
        return teleportId != null || teleportDestination != null;
    }

    public Integer getTeleportId() {
        if (isDialed() && teleportId == null) {
            getTeleportDestination();
        }
        return teleportId;
    }

    public TeleportDestination getTeleportDestination() {
        if (teleportId != null) {
            TeleportDestinations teleportDestinations = TeleportDestinations.getDestinations(getWorld());
            GlobalCoordinate gc = teleportDestinations.getCoordinateForId(teleportId);
            if (gc == null) {
                return null;
            } else {
                return teleportDestinations.getDestination(gc.getCoordinate(), gc.getDimension());
            }
        }
        return teleportDestination;
    }

    public void setTeleportDestination(TeleportDestination teleportDestination, boolean once) {
        this.teleportDestination = null;
        this.teleportId = null;
        this.once = once;
        if (teleportDestination != null) {
            TeleportDestinations destinations = TeleportDestinations.getDestinations(getWorld());
            Integer id = destinations.getIdForCoordinate(new GlobalCoordinate(teleportDestination.getCoordinate(), teleportDestination.getDimension()));
            if (id == null) {
                this.teleportDestination = teleportDestination;
            } else {
                this.teleportId = id;
            }
        }
        markDirtyClient();
    }

    private void consumeIdlePower() {
        if (TeleportConfiguration.rfMatterIdleTick > 0 && teleportingPlayer == null) {
            if (getEnergyStored() >= TeleportConfiguration.rfMatterIdleTick) {
                consumeEnergy(TeleportConfiguration.rfMatterIdleTick);
            } else {
                setTeleportDestination(null, false);
            }
        }
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {
        // Every few times we check if the receiver is ok (if we're dialed).
        if (isDialed()) {
            consumeIdlePower();

            checkReceiverStatusCounter--;
            if (checkReceiverStatusCounter <= 0) {
                checkReceiverStatusCounter = 20;
                int newstatus;
                if (DialingDeviceTileEntity.isDestinationAnalyzerAvailable(getWorld(), getPos())) {
                    newstatus = checkReceiverStatus();
                } else {
                    newstatus = TeleportationTools.STATUS_OK;
                }
                if (newstatus != status) {
                    status = newstatus;
                    markDirtyClient();
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
            EntityPlayer player = getWorld().getPlayerEntityByName(teleportingPlayer);
            if (player != null) {
                Logging.warn(player, "The destination vanished! Aborting.");
            }
            clearTeleport(80);
        } else if (isPlayerOutsideBeam()) {
            // The player moved outside the beam. Interrupt the teleport.
            clearTeleport(80);
        } else {
            int rf = rfPerTick;

            if (getEnergyStored() < rf) {
                // We don't have enough energy to handle this tick.
                handleEnergyShortage();
            } else {
                // We have enough energy so this is a good tick.
                markDirty();
                consumeEnergy(rf);
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

        // @todo
//        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(getWorld());
//        if (dimensionManager.getDimensionInformation(dimension) != null) {
//            // This is an RFTools dimension. Check power.
//            DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(getWorld());
//            int energyLevel = dimensionStorage.getEnergyLevel(dimension);
//            if (energyLevel < DimletConfiguration.DIMPOWER_WARN_TP) {
//                return TeleportationTools.STATUS_WARN;
//            }
//        }


        World w = DimensionManager.getWorld(dimension);
        // By default we will not check if the dimension is not loaded. Can be changed in config.
        if (w == null) {
            if (TeleportConfiguration.matterTransmitterLoadWorld == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                w = getWorld().getMinecraftServer().getWorld(dimension);
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadWorld;
            }
        }
        BlockPos c = destination.getCoordinate();

        boolean exists = RFToolsTools.chunkLoaded(w, c);
        if (!exists) {
            if (TeleportConfiguration.matterTransmitterLoadChunk == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadChunk;
            }
        }

        TileEntity tileEntity = w.getTileEntity(c);
        if (!(tileEntity instanceof MatterReceiverTileEntity)) {
            return TeleportationTools.STATUS_WARN;
        }

        MatterReceiverTileEntity matterReceiverTileEntity = (MatterReceiverTileEntity) tileEntity;

        int status = matterReceiverTileEntity.checkStatus();
        return (status == DialingDeviceTileEntity.DIAL_OK) ? TeleportationTools.STATUS_OK : TeleportationTools.STATUS_WARN;
    }

    private void clearTeleport(int cooldown) {
        markDirty();
        TeleportationTools.applyBadEffectIfNeeded(getWorld().getPlayerEntityByName(teleportingPlayer), 0, badTicks, totalTicks, false);
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
            int xCoord = getPos().getX();
            int yCoord = getPos().getY();
            int zCoord = getPos().getZ();
            beamBox = new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
        }

        List<Entity> l = getWorld().getEntitiesWithinAABB(EntityPlayer.class, beamBox);
        Entity nearestPlayer = findNearestPlayer(l);

        if (nearestPlayer == null) {
            cooldownTimer = 5;
            return;
        }
        AxisAlignedBB playerBB = nearestPlayer.getEntityBoundingBox();
        // Shouldn't be possible but there are mods...
        if (playerBB == null) {
            cooldownTimer = 5;
            return;
        }
        if (playerBB.intersects(beamBox)) {
            startTeleportation(nearestPlayer);
        } else {
            cooldownTimer = 5;
        }
    }

    private Entity findNearestPlayer(List<Entity> l) {
        Entity nearestPlayer = null;
        double dmax = Double.MAX_VALUE;
        for (Entity entity : l) {
            if (entity instanceof EntityPlayer) {
                EntityPlayer entityPlayer = (EntityPlayer) entity;
                if (entityPlayer.isRiding() || entityPlayer.isBeingRidden()) {
                    // Ignore players that are riding a horse
                    continue;
                }

                if (entityPlayer.getName() != null) {
                    if ((!isPrivateAccess()) || allowedPlayers.contains(entityPlayer.getName())) {
                        double d1 = entity.getDistanceSq(getPos().getX() + .5, getPos().getY() + 1.5, getPos().getZ() + .5);

                        if (d1 <= dmax) {
                            nearestPlayer = entity;
                            dmax = d1;
                        }
                    }
                }
            }
        }
        return nearestPlayer;
    }

    private void performTeleport() {
        // First check if the destination is still valid.
        if (!isDestinationStillValid()) {
            EntityPlayer player = getWorld().getPlayerEntityByName(teleportingPlayer);
            if (player != null) {
                TeleportationTools.applyBadEffectIfNeeded(player, 10, badTicks, totalTicks, false);
                Logging.warn(player, "Missing destination!");
            }
            clearTeleport(200);
            return;
        }

        TeleportDestination dest = getTeleportDestination();

        // The destination is valid. If this is a 'once' dial then we clear the destination here.
        if (once) {
            setTeleportDestination(null, false);
        }

        boolean boosted = DialingDeviceTileEntity.isMatterBoosterAvailable(getWorld(), getPos());
        if (boosted && getEnergyStored() < TeleportConfiguration.rfBoostedTeleport) {
            // Not enough energy. We cannot do a boosted teleport.
            boosted = false;
        }
        EntityPlayer player = getWorld().getPlayerEntityByName(teleportingPlayer);
        if (player != null) {
            boolean boostNeeded = TeleportationTools.performTeleport(player, dest, badTicks, totalTicks, boosted);
            if (boostNeeded) {
                consumeEnergy(TeleportConfiguration.rfBoostedTeleport);
            }
        }

        teleportingPlayer = null;
    }

    private boolean isDestinationStillValid() {
        TeleportDestination dest = getTeleportDestination();
        return TeleportDestinations.getDestinations(getWorld()).isDestinationValid(dest);
    }

    private void handleEnergyShortage() {
        markDirty();
        // Not enough energy. This is a bad tick.
        badTicks++;
        if (TeleportationTools.mustInterrupt(badTicks, totalTicks)) {
            // Too many bad ticks. Total failure!
            EntityPlayer player = getWorld().getPlayerEntityByName(teleportingPlayer);
            if (player != null) {
                Logging.warn(player, "Power failure during transit!");
            }
            clearTeleport(200);
        }
        return;
    }

    private boolean isPlayerOutsideBeam() {
        EntityPlayer player = getWorld().getPlayerEntityByName(teleportingPlayer);
        if (player == null) {
            return true;
        }
        AxisAlignedBB playerBB = player.getEntityBoundingBox();
        // Shouldn't be possible but there are mods...
        if (playerBB == null) {
            return true;
        }
        if (!playerBB.intersects(beamBox)) {
            Logging.message(player, "Teleportation was interrupted!");
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
        if (player.isRiding() || player.isBeingRidden()) {
            cooldownTimer = 80;
            return;
        }

        TeleportDestination dest = teleportDestination;
        if (teleportId != null) {
            dest = getTeleportDestination();
        }

        if (dest != null && dest.isValid()) {
            int cost = TeleportationTools.calculateRFCost(getWorld(), getPos(), dest);
            cost = (int) (cost * (4.0f - getInfusedFactor()) / 4.0f);

            if (getEnergyStored() < cost) {
                Logging.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }

            int srcId = getWorld().provider.getDimension();
            int dstId = dest.getDimension();
            if (!TeleportationTools.checkValidTeleport(player, srcId, dstId)) {
                cooldownTimer = 80;
                return;
            }

            Logging.message(player, "Start teleportation...");
            teleportingPlayer = player.getName();
            teleportTimer = TeleportationTools.calculateTime(getWorld(), getPos(), dest);
            teleportTimer = (int) (teleportTimer * (1.2f - getInfusedFactor()) / 1.2f);

            int rf = TeleportConfiguration.rfTeleportPerTick;
            rf = (int) (rf * (4.0f - getInfusedFactor()) / 4.0f);
            int totalRfUsed = cost + rf * (teleportTimer+1);
            rfPerTick = totalRfUsed / (teleportTimer+1);

            totalTicks = teleportTimer;
            goodTicks = 0;
            badTicks = 0;
        } else {
            Logging.warn(player, "Something is wrong with the destination!");
        }
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETNAME.equals(command)) {
            setName(args.get("name").getString());
            return true;
        } else if (CMD_SETPRIVATE.equals(command)) {
            setPrivateAccess(args.get("private").getBoolean());
            return true;
        } else if (CMD_SETBEAM.equals(command)) {
            setBeamHidden(args.get("hide").getBoolean());
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

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, Map<String, Argument> args, Type<T> type) {
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
            GuiMatterTransmitter.storeAllowedPlayersForClient(Type.STRING.convert(list));
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
        return new AxisAlignedBB(getPos(), getPos().add(1, 4, 1));
    }
}
