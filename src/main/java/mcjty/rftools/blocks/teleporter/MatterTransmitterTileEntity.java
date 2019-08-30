package mcjty.rftools.blocks.teleporter;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.lib.varia.WorldTools;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.*;

import static mcjty.rftools.blocks.teleporter.TeleporterSetup.TYPE_MATTER_TRANSMITTER;

public class MatterTransmitterTileEntity extends GenericTileEntity implements MachineInformation, ITickableTileEntity {

    public static final String CMD_ADDPLAYER = "transmitter.addPlayer";
    public static final String CMD_DELPLAYER = "transmitter.delPlayer";
    public static final Key<String> PARAM_PLAYER = new Key<>("player", Type.STRING);

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
    private UUID teleportingPlayer = null;
    private int teleportTimer = 0;
    private int cooldownTimer = 0;
    private int totalTicks;
    private int goodTicks;
    private int badTicks;
    private int rfPerTick = 0;

    private int checkReceiverStatusCounter = 20;

    private AxisAlignedBB beamBox = null;

    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true, TeleportConfiguration.TRANSMITTER_MAXENERGY.get(), TeleportConfiguration.TRANSMITTER_RECEIVEPERTICK.get()));

    public static final Key<String> VALUE_NAME = new Key<>("name", Type.STRING);
    public static final Key<Boolean> VALUE_PRIVATE = new Key<>("private", Type.BOOLEAN);
    public static final Key<Boolean> VALUE_BEAM = new Key<>("beam", Type.BOOLEAN);

    @Override
    public IValue<?>[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_NAME, this::getName, this::setName),
                new DefaultValue<>(VALUE_PRIVATE, this::isPrivateAccess, this::setPrivateAccess),
                new DefaultValue<>(VALUE_BEAM, this::isBeamHidden, this::setBeamHidden),
        };
    }

    public MatterTransmitterTileEntity() {
        super(TYPE_MATTER_TRANSMITTER);
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        teleportTimer = tagCompound.getInt("tpTimer");
        cooldownTimer = tagCompound.getInt("cooldownTimer");
        totalTicks = tagCompound.getInt("totalTicks");
        goodTicks = tagCompound.getInt("goodTicks");
        badTicks = tagCompound.getInt("badTicks");
        if (tagCompound.contains("tpPlayer")) {
            teleportingPlayer = tagCompound.getUniqueId("tpPlayer");
        } else {
            teleportingPlayer = null;
        }
        status = tagCompound.getInt("status");
        rfPerTick = tagCompound.getInt("rfPerTick");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot tables
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        name = tagCompound.getString("tpName");
        BlockPos c = BlockPosTools.read(tagCompound, "dest");
        if (c == null) {
            teleportDestination = null;
        } else {
            int dim = tagCompound.getInt("dim");
            teleportDestination = new TeleportDestination(c, dim);
        }
        if (tagCompound.contains("destId")) {
            teleportId = tagCompound.getInt("destId");
        } else {
            teleportId = null;
        }
        privateAccess = tagCompound.getBoolean("private");
        beamHidden = tagCompound.getBoolean("hideBeam");
        once = tagCompound.getBoolean("once");

        allowedPlayers.clear();
        ListNBT playerList = tagCompound.getList("players", Constants.NBT.TAG_STRING);
        for (int i = 0 ; i < playerList.size() ; i++) {
            String player = playerList.getString(i);
            allowedPlayers.add(player);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putInt("tpTimer", teleportTimer);
        tagCompound.putInt("cooldownTimer", cooldownTimer);
        tagCompound.putInt("totalTicks", totalTicks);
        tagCompound.putInt("goodTicks", goodTicks);
        tagCompound.putInt("badTicks", badTicks);
        if (teleportingPlayer != null) {
            tagCompound.putUniqueId("tpPlayer", teleportingPlayer);
        }
        tagCompound.putInt("status", status);
        tagCompound.putInt("rfPerTick", rfPerTick);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        if (name != null && !name.isEmpty()) {
            tagCompound.putString("tpName", name);
        }
        if (teleportDestination != null) {
            BlockPos c = teleportDestination.getCoordinate();
            if (c != null) {
                BlockPosTools.write(tagCompound, "dest", c);
                tagCompound.putInt("dim", teleportDestination.getDimension());
            }
        }
        if (teleportId != null) {
            tagCompound.putInt("destId", teleportId);
        }

        tagCompound.putBoolean("private", privateAccess);
        tagCompound.putBoolean("hideBeam", beamHidden);
        tagCompound.putBoolean("once", once);

        ListNBT playerTagList = new ListNBT();
        for (String player : allowedPlayers) {
            playerTagList.add(new StringNBT(player));
        }
        tagCompound.put("players", playerTagList);
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
            TeleportDestinations teleportDestinations = TeleportDestinations.getDestinations(world);
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
            TeleportDestinations destinations = TeleportDestinations.getDestinations(world);
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
        if (TeleportConfiguration.rfMatterIdleTick.get() > 0 && teleportingPlayer == null) {
            energyHandler.ifPresent(h -> {
                if (h.getEnergyStored() >= TeleportConfiguration.rfMatterIdleTick.get()) {
                    h.consumeEnergy(TeleportConfiguration.rfMatterIdleTick.get());
                } else {
                    setTeleportDestination(null, false);
                }
            });
        }
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
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
                if (DialingDeviceTileEntity.isDestinationAnalyzerAvailable(world, getPos())) {
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
            PlayerEntity player = world.getPlayerByUuid(teleportingPlayer);
            if (player != null) {
                Logging.warn(player, "The destination vanished! Aborting.");
            }
            clearTeleport(80);
        } else if (isPlayerOutsideBeam()) {
            // The player moved outside the beam. Interrupt the teleport.
            clearTeleport(80);
        } else {
            energyHandler.ifPresent(h -> {
                int rf = rfPerTick;
                if (h.getEnergyStored() < rf) {
                    // We don't have enough energy to handle this tick.
                    handleEnergyShortage();
                } else {
                    // We have enough energy so this is a good tick.
                    markDirty();
                    h.consumeEnergy(rf);
                    goodTicks++;

                    teleportTimer--;
                    if (teleportTimer <= 0) {
                        performTeleport();
                    }
                }
            });
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
//        RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(world);
//        if (dimensionManager.getDimensionInformation(dimension) != null) {
//            // This is an RFTools dimension. Check power.
//            DimensionStorage dimensionStorage = DimensionStorage.getDimensionStorage(world);
//            int energyLevel = dimensionStorage.getEnergyLevel(dimension);
//            if (energyLevel < DimletConfiguration.DIMPOWER_WARN_TP) {
//                return TeleportationTools.STATUS_WARN;
//            }
//        }


        World w = WorldTools.getWorld(dimension);
        // By default we will not check if the dimension is not loaded. Can be changed in config.
        if (w == null) {
            if (TeleportConfiguration.matterTransmitterLoadWorld.get() == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                w = WorldTools.loadWorld(dimension);
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadWorld.get();
            }
        }
        BlockPos c = destination.getCoordinate();

        boolean exists = WorldTools.chunkLoaded(w, c);
        if (!exists) {
            if (TeleportConfiguration.matterTransmitterLoadChunk.get() == -1) {
                return TeleportationTools.STATUS_UNKNOWN;
            } else {
                checkReceiverStatusCounter = TeleportConfiguration.matterTransmitterLoadChunk.get();
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
        TeleportationTools.applyBadEffectIfNeeded(world.getPlayerByUuid(teleportingPlayer), 0, badTicks, totalTicks, false);
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

    private void prepareBeamBox() {
        if (beamBox == null) {
            int xCoord = getPos().getX();
            int yCoord = getPos().getY();
            int zCoord = getPos().getZ();
            beamBox = new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
        }
    }

    private void searchForNearestPlayer() {
        prepareBeamBox();

        List<Entity> l = world.getEntitiesWithinAABB(PlayerEntity.class, beamBox);
        Entity nearestPlayer = findNearestPlayer(l);

        if (nearestPlayer == null) {
            cooldownTimer = 5;
            return;
        }
        AxisAlignedBB playerBB = nearestPlayer.getBoundingBox();
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
            if (entity instanceof PlayerEntity) {
                PlayerEntity player = (PlayerEntity) entity;
                if (player.isPassenger() || player.isBeingRidden()) {
                    // Ignore players that are riding a horse
                    continue;
                }

                if (player.getName() != null) {
                    if ((!isPrivateAccess()) || allowedPlayers.contains(player.getName())) {
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
            PlayerEntity player = world.getPlayerByUuid(teleportingPlayer);
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

        boolean boosted = DialingDeviceTileEntity.isMatterBoosterAvailable(world, getPos());
        if (boosted && energyHandler.map(h -> h.getEnergyStored()).orElse(0) < TeleportConfiguration.rfBoostedTeleport.get()) {
            // Not enough energy. We cannot do a boosted teleport.
            boosted = false;
        }
        PlayerEntity player = world.getPlayerByUuid(teleportingPlayer);
        if (player != null) {
            boolean boostNeeded = TeleportationTools.performTeleport(player, dest, badTicks, totalTicks, boosted);
            if (boostNeeded) {
                energyHandler.ifPresent(h -> {
                    h.consumeEnergy(TeleportConfiguration.rfBoostedTeleport.get());
                });
            }
        }

        teleportingPlayer = null;
    }

    private boolean isDestinationStillValid() {
        TeleportDestination dest = getTeleportDestination();
        return TeleportDestinations.getDestinations(world).isDestinationValid(dest);
    }

    private void handleEnergyShortage() {
        markDirty();
        // Not enough energy. This is a bad tick.
        badTicks++;
        if (TeleportationTools.mustInterrupt(badTicks, totalTicks)) {
            // Too many bad ticks. Total failure!
            PlayerEntity player = world.getPlayerByUuid(teleportingPlayer);
            if (player != null) {
                Logging.warn(player, "Power failure during transit!");
            }
            clearTeleport(200);
        }
        return;
    }

    private boolean isPlayerOutsideBeam() {
        PlayerEntity player = world.getPlayerByUuid(teleportingPlayer);
        if (player == null) {
            return true;
        }
        AxisAlignedBB playerBB = player.getBoundingBox();
        // Shouldn't be possible but there are mods...
        if (playerBB == null) {
            return true;
        }
        prepareBeamBox();
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
        if (!(entity instanceof PlayerEntity)) {
            return;
        }
        PlayerEntity player = (PlayerEntity) entity;
        if (player.isPassenger() || player.isBeingRidden()) {
            cooldownTimer = 80;
            return;
        }

        TeleportDestination dest = teleportDestination;
        if (teleportId != null) {
            dest = getTeleportDestination();
        }

        if (dest != null && dest.isValid()) {
            int cost = TeleportationTools.calculateRFCost(world, getPos(), dest);
            cost = (int) (cost * (4.0f - getInfusedFactor()) / 4.0f);

            if (energyHandler.map(h -> h.getEnergyStored()).orElse(0) < cost) {
                Logging.warn(player, "Not enough power to start the teleport!");
                cooldownTimer = 80;
                return;
            }

            int srcId = world.getDimension().getType().getId();
            int dstId = dest.getDimension();
            if (!TeleportationTools.checkValidTeleport(player, srcId, dstId)) {
                cooldownTimer = 80;
                return;
            }

            Logging.message(player, "Start teleportation...");
            teleportingPlayer = player.getUniqueID();
            teleportTimer = TeleportationTools.calculateTime(world, getPos(), dest);
            teleportTimer = (int) (teleportTimer * (1.2f - getInfusedFactor()) / 1.2f);

            int rf = TeleportConfiguration.rfTeleportPerTick.get();
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
            GuiMatterTransmitter.storeAllowedPlayersForClient(Type.STRING.convert(list));
            return true;
        }
        return false;
    }

    // @todo 1.14
//    @Override
//    public boolean shouldRenderInPass(int pass) {
//        return pass == 1;
//    }
//
//    @SideOnly(Side.CLIENT)
//    @Override
//    public AxisAlignedBB getRenderBoundingBox() {
//        return new AxisAlignedBB(getPos(), getPos().add(1, 4, 1));
//    }
}
