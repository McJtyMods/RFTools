package mcjty.rftools.blocks.endergen;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.api.information.IMachineInformation;
import mcjty.lib.bindings.DefaultValue;
import mcjty.lib.bindings.IValue;
import mcjty.lib.compat.RedstoneFluxCompatibility;
import mcjty.lib.network.PacketSendClientCommand;
import mcjty.lib.tileentity.GenericEnergyProviderTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.GlobalCoordinate;
import mcjty.lib.varia.Logging;
import mcjty.rftools.ClientCommandHandler;
import mcjty.rftools.RFTools;
import mcjty.rftools.hud.IHudSupport;
import mcjty.rftools.network.PacketGetHudLog;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class EndergenicTileEntity extends GenericEnergyProviderTileEntity implements ITickable, MachineInformation,
        IHudSupport, IMachineInformation {

    private static Random random = new Random();

    public static final String CMD_GETSTATS = "getStats";
    public static final String CLIENTCMD_GETSTATS = "getStats";

    public static Key<Integer> PARAM_STATRF = new Key<>("statrf", Type.INTEGER);
    public static Key<Integer> PARAM_STATLOST = new Key<>("statlost", Type.INTEGER);
    public static Key<Integer> PARAM_STATLAUNCHED = new Key<>("statlaunched", Type.INTEGER);
    public static Key<Integer> PARAM_STATOPPORTUNITIES = new Key<>("statopportunities", Type.INTEGER);

    private static final String[] TAGS = new String[]{"rftick", "lost", "launched", "opportunities"};
    private static final String[] TAG_DESCRIPTIONS = new String[]{"Average RF/tick for the last 5 seconds", "Amount of pearls that were lost during the last 5 seconds",
            "Amount of pearls that were launched during the last 5 seconds", "Number of opportunities for the last 5 seconds"};

    public static final int CHARGE_IDLE = 0;
    public static final int CHARGE_HOLDING = -1;

    public static final Key<BlockPos> VALUE_DESTINATION = new Key<>("destination", Type.BLOCKPOS);

    @Override
    public IValue[] getValues() {
        return new IValue[] {
                new DefaultValue<>(VALUE_DESTINATION, EndergenicTileEntity::getDestination, EndergenicTileEntity::setDestination)
        };
    }

    // The current chargingMode status.
    // CHARGE_IDLE means this entity is doing nothing.
    // A positive number means it is chargingMode up from 0 to 15. When it reaches 15 it will go back to idle unless
    // it was hit by an endergenic pearl in the mean time. In that case it goes to 'holding' state.
    // CHARGE_HOLDING means this entity is holding an endergenic pearl. Whie it does that it consumes
    // energy. If internal energy is depleted then the endergenic pearl is lost and the mode goes
    // back to idle.
    private int chargingMode = CHARGE_IDLE;

    // The current age of the pearl we're holding. Will be used to calculate bonuses
    // for powergeneration on pearls that are in the network for a longer time.
    private int currentAge = 0;

    // The location of the destination endergenic generator.
    private BlockPos destination = null;
    private int distance = 0;           // Distance between this block and destination in ticks

    // For pulse detection.
    private boolean prevIn = false;

    // Statistics for this generator.
    // These values count what is happening.
    private int rfGained = 0;
    private int rfLost = 0;
    private int pearlsLaunched = 0;
    private int pearlsLost = 0;
    private int chargeCounter = 0;
    private int pearlArrivedAt = -2;
    private int ticks = 100;

    // These values actually contain valid statistics.
    private int lastRfPerTick = 0;
    private int lastRfGained = 0;
    private int lastRfLost = 0;
    private int lastPearlsLost = 0;
    private int lastPearlsLaunched = 0;
    private int lastChargeCounter = 0;
    private int lastPearlArrivedAt = 0;
    private String lastPearlsLostReason = "";

    // Current traveling pearls.
    private List<EndergenicPearl> pearls = new ArrayList<>();

    private long lastHudTime = 0;
    private List<String> clientHudLog = new ArrayList<>();

    // Used for rendering a 'bad' and 'good' effect client-side
    private int badCounter = 0;
    private int goodCounter = 0;

    // This table indicates how much RF is produced when an endergenic pearl hits this block
    // at that specific chargingMode.
    private static int rfPerHit[] = new int[]{0, 100, 150, 200, 400, 800, 1600, 3200, 6400, 8000, 12800, 8000, 6400, 2500, 1000, 100};

    private int tickCounter = 0;            // Only used for logging, counts server ticks.

    // We enqueue endergenics for processing later
    public static List<EndergenicTileEntity> todoEndergenics = new ArrayList<>();
    public static Set<GlobalCoordinate> endergenicsAdded = new HashSet<>();

    public EndergenicTileEntity() {
        super(5000000, 20000);
    }

    @Override
    public int getEnergyDiffPerTick() {
        return rfGained - rfLost;
    }

    @Nullable
    @Override
    public String getEnergyUnitName() {
        return "RF";
    }

    @Override
    public boolean isMachineActive() {
        return true;
    }

    @Override
    public boolean isMachineRunning() {
        return true;
    }

    @Nullable
    @Override
    public String getMachineStatus() {
        return null;
    }

    @Override
    public void update() {
        // bad and good counter are handled both client and server side
        if (badCounter > 0) {
            badCounter--;
            markDirtyQuick();
        }
        if (goodCounter > 0) {
            goodCounter--;
            markDirtyQuick();
        }

        if (!getWorld().isRemote) {
            queueWork();
        }
    }

    // Postpone the actual tick to after all other TE's have ticked (in a WorldTickEvent)
    private void queueWork() {
        GlobalCoordinate gc = new GlobalCoordinate(getPos(), getWorld().provider.getDimension());
        if (endergenicsAdded.contains(gc)) {
            // We're already there. Nothing to do
            return;
        }

        // Find an endergenic with an injector.
        EndergenicTileEntity withInjector = findEndergenicWithPredicate(new HashSet<>(), EndergenicTileEntity::hasInjector);
        if (withInjector != null) {
            // From this injector locate if possible an injector that has a pearl and use
            // that one instead as the head of the endergenic list for post-tick processing.
            EndergenicTileEntity loop = withInjector.findEndergenicWithPredicate(new HashSet<>(), p -> !p.pearls.isEmpty());
            if (loop == null) loop = withInjector;
            Set<BlockPos> done = new HashSet<>();

            while (loop != null) {
                done.add(loop.getPos());
                addToQueue(loop, new GlobalCoordinate(loop.getPos(), getWorld().provider.getDimension()));
                loop = loop.getDestinationTE();
                if (loop == null || done.contains(loop.getPos())) {
                    loop = null;
                }
            }
        }
        // In all cases we add this endergenic. This will not do anything
        // if it was already added before
        addToQueue(this, gc);
    }

    private void addToQueue(EndergenicTileEntity endergenicWithInjector, GlobalCoordinate gc2) {
        if (!endergenicsAdded.contains(gc2)) {
            todoEndergenics.add(endergenicWithInjector);
            endergenicsAdded.add(gc2);
        }
    }

    private EndergenicTileEntity findEndergenicWithPredicate(Set<BlockPos> done, Predicate<EndergenicTileEntity> pred) {
        if (pred.test(this)) {
            return this;
        }
        if (destination == null) {
            return null;
        }
        // Avoid eternal loops
        done.add(getPos());
        if (done.contains(destination)) {
            return null;
        }
        TileEntity te = getWorld().getTileEntity(destination);
        if (te instanceof EndergenicTileEntity) {
            return ((EndergenicTileEntity) te).findEndergenicWithPredicate(done, pred);
        }
        return null;
    }

    private boolean hasInjector() {
        for (EnumFacing dir : EnumFacing.VALUES) {
            IBlockState state = getWorld().getBlockState(getPos().offset(dir));
            if (state.getBlock() == EndergenicSetup.pearlInjectorBlock) {
                TileEntity te = getWorld().getTileEntity(getPos().offset(dir));
                if (te instanceof PearlInjectorTileEntity) {
                    PearlInjectorTileEntity injector = (PearlInjectorTileEntity) te;
                    EndergenicTileEntity endergenic = injector.findEndergenicTileEntity();
                    if (endergenic == this) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public EnumFacing getBlockOrientation() {
        return null;
    }

    @Override
    public boolean isBlockAboveAir() {
        return getWorld().isAirBlock(pos.up());
    }

    public List<String> getHudLog() {
        List<String> list = new ArrayList<>();
        list.add(TextFormatting.BLUE + "Last 5 seconds:");
        list.add("    Charged: " + getLastChargeCounter());
        list.add("    Fired: " + getLastPearlsLaunched());
        list.add("    Lost: " + getLastPearlsLost());
        if (getLastPearlsLost() > 0) {
            list.add(TextFormatting.RED + "    " + getLastPearlsLostReason());
        }
        if (getLastPearlArrivedAt() > -2) {
            list.add("    Last pearl at " + getLastPearlArrivedAt());
        }
        list.add(TextFormatting.BLUE + "Power:");
        list.add(TextFormatting.GREEN + "    RF Gain " + getLastRfGained());
        list.add(TextFormatting.RED + "    RF Lost " + getLastRfLost());
        list.add(TextFormatting.GREEN + "    RF/t " + getLastRfPerTick());
        return list;
    }

    @Override
    public BlockPos getBlockPos() {
        return getPos();
    }

    @Override
    public List<String> getClientLog() {
        return clientHudLog;
    }

    @Override
    public long getLastUpdateTime() {
        return lastHudTime;
    }

    @Override
    public void setLastUpdateTime(long t) {
        lastHudTime = t;
    }

    public int getBadCounter() {
        return badCounter;
    }

    public int getLastRfPerTick() {
        return lastRfPerTick;
    }

    public int getLastRfGained() {
        return lastRfGained;
    }

    public int getLastRfLost() {
        return lastRfLost;
    }

    public int getLastPearlsLost() {
        return lastPearlsLost;
    }

    public int getLastPearlsLaunched() {
        return lastPearlsLaunched;
    }

    public int getLastChargeCounter() {
        return lastChargeCounter;
    }

    public int getLastPearlArrivedAt() {
        return lastPearlArrivedAt;
    }

    public String getLastPearlsLostReason() {
        return lastPearlsLostReason;
    }

    public int getGoodCounter() {
        return goodCounter;
    }

    public void checkStateServer() {
        tickCounter++;

        ticks--;
        if (ticks < 0) {
            lastRfGained = rfGained;
            lastRfLost = rfLost;
            lastRfPerTick = (rfGained - rfLost) / 100;
            lastPearlsLost = pearlsLost;
            lastPearlsLaunched = pearlsLaunched;
            lastChargeCounter = chargeCounter;
            lastPearlArrivedAt = pearlArrivedAt;
//
//            System.out.println(BlockPosTools.toString(getPos()) + " RF: +" + lastRfGained + " -" + lastRfLost + " (" + lastRfPerTick + ")  "
//                + "Pearls: F" + lastPearlsLaunched + " L" + lastPearlsLost + "  Charges: " + lastChargeCounter);

            ticks = 100;
            rfGained = 0;
            rfLost = 0;
            pearlsLaunched = 0;
            pearlsLost = 0;
            chargeCounter = 0;
            pearlArrivedAt = -2;
        }

        handlePearls();
        handleSendingEnergy();

        // First check if we're holding a pearl to see if the pearl will be lost.
        if (chargingMode == CHARGE_HOLDING) {
            if (random.nextInt(1000) <= EndergenicConfiguration.chanceLost) {
                // Pearl is lost.
                log("Server Tick: discard pearl randomly");
                discardPearl("Random pearl discard");
            }
        }

        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;
        if (pulse) {
            if (chargingMode == CHARGE_IDLE) {
                log("Server Tick: pulse -> start charging");
                startCharging();
                return;
            } else if (chargingMode == CHARGE_HOLDING) {
                log("Server Tick: pulse -> fire pearl");
                firePearl();
                return;
            }
        }

        if (chargingMode == CHARGE_IDLE) {
            // Do nothing
            return;
        }

        if (chargingMode == CHARGE_HOLDING) {
            // Consume energy to keep the endergenic pearl.
            int rf = EndergenicConfiguration.rfToHoldPearl;
            rf = (int) (rf * (3.0f - getInfusedFactor()) / 3.0f);

            int rfStored = getEnergyStored();
            if (rfStored < rf) {
                // Not enough energy. Pearl is lost.
                log("Server Tick: insufficient energy to hold pearl (" + rfStored + " vs " + rf + ")");
                discardPearl("Not enough energy to hold pearl");
            } else {
                int rfExtracted = storage.extractEnergy(rf, false);
                log("Server Tick: holding pearl, consume " + rfExtracted + " RF");
                rfLost += rfExtracted;
            }
            return;
        }

        // Else we're charging up.
        markDirtyQuick();
        chargingMode++;
        if (chargingMode >= 16) {
            log("Server Tick: charging mode ends -> idle");
            chargingMode = CHARGE_IDLE;
        }
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
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
        switch (index) {
            case 0:
                return Integer.toString(lastRfPerTick);
            case 1:
                return Integer.toString(lastPearlsLost);
            case 2:
                return Integer.toString(lastPearlsLaunched);
            case 3:
                return Integer.toString(lastChargeCounter);
        }
        return null;
    }

    private void log(String message) {
        /* RFTools.log(getWorld(), this, message);*/
    }

    public static final EnumFacing[] HORIZ_DIRECTIONS = {EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST};

    /**
     * Something happens, we need to notify all ender monitors.
     *
     * @param mode is the new mode
     */
    private void fireMonitors(EnderMonitorMode mode) {
        BlockPos pos = getPos();
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos c = pos.offset(dir);
            TileEntity te = getWorld().getTileEntity(c);
            if (te instanceof EnderMonitorTileEntity) {
                EnderMonitorTileEntity enderMonitorTileEntity = (EnderMonitorTileEntity) te;
                EnumFacing inputSide = enderMonitorTileEntity.getFacing(getWorld().getBlockState(c)).getInputSide();
                if (inputSide == dir.getOpposite()) {
                    enderMonitorTileEntity.fireFromEndergenic(mode);
                }
            }
        }
    }

    private void handleSendingEnergy() {
        int energyStored = getEnergyStored();
        if (energyStored <= EndergenicConfiguration.keepRfInBuffer) {
            return;
        }
        energyStored -= EndergenicConfiguration.keepRfInBuffer;

        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos o = getPos().offset(dir);
            TileEntity te = getWorld().getTileEntity(o);
            EnumFacing opposite = dir.getOpposite();
            if (EnergyTools.isEnergyTE(te) || (te != null && te.hasCapability(CapabilityEnergy.ENERGY, opposite))) {
                int rfToGive;
                if (EndergenicConfiguration.rfOutput <= energyStored) {
                    rfToGive = EndergenicConfiguration.rfOutput;
                } else {
                    rfToGive = energyStored;
                }
                int received;
                if (RFTools.redstoneflux && RedstoneFluxCompatibility.isEnergyConnection(te)) {
                    if (RedstoneFluxCompatibility.canConnectEnergy(te, opposite)) {
                        received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                    } else {
                        received = 0;
                    }
                } else {
                    // Forge unit
                    received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                }
                energyStored -= storage.extractEnergy(received, false);
                if (energyStored <= 0) {
                    break;
                }
            }
        }
    }

    // Handle all pearls that are currently in transit.
    private void handlePearls() {
        if (pearls.isEmpty()) {
            return;
        }
        List<EndergenicPearl> newlist = new ArrayList<>();
        for (EndergenicPearl pearl : pearls) {
            log("Pearls: age=" + pearl.getAge() + ", ticks left=" + pearl.getTicksLeft());
            if (!pearl.handleTick(getWorld())) {
                // Keep the pearl. It has not arrived yet.
                newlist.add(pearl);
            }
        }

        // Replace the old list with the new one.
        pearls = newlist;
    }

    private void markDirtyClientNoRender() {
        markDirty();
        if (getWorld() != null) {
            getWorld().getPlayers(EntityPlayer.class, p -> getPos().distanceSq(p.posX, p.posY, p.posZ) < 32 * 32)
                    .stream()
                    .forEach(p -> RFToolsMessages.INSTANCE.sendTo(
                            new PacketSendClientCommand(RFTools.MODID, ClientCommandHandler.CMD_FLASH_ENDERGENIC,
                                    TypedMap.builder()
                                            .put(ClientCommandHandler.PARAM_POS, getPos())
                                            .put(ClientCommandHandler.PARAM_GOODCOUNTER, goodCounter)
                                            .put(ClientCommandHandler.PARAM_BADCOUNTER, badCounter)
                                            .build()),
                            (EntityPlayerMP) p));
        }
    }

    public void syncCountersFromServer(int goodCounter, int badCounter) {
        this.goodCounter = goodCounter;
        this.badCounter = badCounter;
    }

    private void discardPearl(String reason) {
        badCounter = 20;
        markDirtyClientNoRender();
        pearlsLost++;
        lastPearlsLostReason = reason;
        chargingMode = CHARGE_IDLE;
        fireMonitors(EnderMonitorMode.MODE_LOSTPEARL);
    }

    /**
     * Get the current destination. This function checks first if that destination is
     * still valid and if not it is reset to null (i.e. the destination was removed).
     *
     * @return the destination TE or null if there is no valid one
     */
    private EndergenicTileEntity getDestinationTE() {
        if (destination == null) {
            return null;
        }
        TileEntity te = getWorld().getTileEntity(destination);
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        } else {
            destination = null;
            markDirtyClient();
            return null;
        }
    }

    public void firePearl() {
        markDirtyQuick();
        // This method assumes we're in holding mode.
        getDestinationTE();
        if (destination == null) {
            // There is no destination so the pearl is simply lost.
            log("Fire Pearl: pearl lost due to lack of destination");
            discardPearl("Missing destination");
        } else {
            log("Fire Pearl: pearl is launched to " + destination.getX() + "," + destination.getY() + "," + destination.getZ());
            chargingMode = CHARGE_IDLE;
            pearlsLaunched++;
            pearls.add(new EndergenicPearl(distance, destination, currentAge + 1));
            fireMonitors(EnderMonitorMode.MODE_PEARLFIRED);
        }
    }

    public void firePearlFromInjector() {
        markDirtyQuick();
        // This method assumes we're not in holding mode.
        getDestinationTE();
        chargingMode = CHARGE_IDLE;
        if (destination == null) {
            // There is no destination so the injected pearl is simply lost.
            log("Fire Pearl from injector: pearl lost due to lack of destination");
            discardPearl("Missing destination");
        } else {
            log("Fire Pearl from injector: pearl is launched to " + destination.getX() + "," + destination.getY() + "," + destination.getZ());
            pearlsLaunched++;
            pearls.add(new EndergenicPearl(distance, destination, 0));
            fireMonitors(EnderMonitorMode.MODE_PEARLFIRED);
        }
    }

    // This generator receives a pearl. The age of the pearl is how many times the pearl has
    // already generated power.
    public void receivePearl(int age) {
        fireMonitors(EnderMonitorMode.MODE_PEARLARRIVED);
        markDirtyQuick();
        if (chargingMode == CHARGE_HOLDING) {
            log("Receive Pearl: pearl arrives but already holding -> both are lost");
            // If this block is already holding a pearl and it still has one then both pearls are
            // automatically lost.
            discardPearl("Pearl arrived while holding");
        } else if (chargingMode == CHARGE_IDLE) {
            log("Receive Pearl: pearl arrives but generator is idle -> pearl is lost");
            // If this block is idle and it is hit by a pearl then the pearl is lost and nothing
            // happens.
            discardPearl("Pearl arrived while idle");
        } else {
            pearlArrivedAt = chargingMode;
            // Otherwise we get RF and this block goes into holding mode.
            int rf = (int) (rfPerHit[chargingMode] * EndergenicConfiguration.powergenFactor);
            rf = (int) (rf * (getInfusedFactor() + 2.0f) / 2.0f);

            // Give a bonus for pearls that have been around a bit longer.
            int a = age * 5;
            if (a > 100) {
                a = 100;
            }
            rf += rf * a / 100;     // Maximum 200% bonus. Minimum no bonus.
            rfGained += rf;
            log("Receive Pearl: pearl arrives at tick " + chargingMode + ", age=" + age + ", RF=" + rf);
            modifyEnergyStored(rf);

            goodCounter = 10;
            markDirtyClientNoRender();

            chargingMode = CHARGE_HOLDING;
            currentAge = age;
        }
    }

    public void startCharging() {
        markDirtyQuick();
        chargingMode = 1;
        chargeCounter++;
    }

    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        BlockPos thisCoord = getPos();
        BlockPos coord = RFTools.instance.clientInfo.getSelectedTE();
        TileEntity tileEntity = null;
        if (coord != null) {
            tileEntity = getWorld().getTileEntity(coord);
        }

        if (!(tileEntity instanceof EndergenicTileEntity)) {
            // None selected. Just select this one.
            RFTools.instance.clientInfo.setSelectedTE(thisCoord);
            EndergenicTileEntity destinationTE = getDestinationTE();
            if (destinationTE == null) {
                RFTools.instance.clientInfo.setDestinationTE(null);
                Logging.message(player, "Select another endergenic generator as destination");
            } else {
                RFTools.instance.clientInfo.setDestinationTE(destinationTE.getPos());
                int distance = getDistanceInTicks();
                Logging.message(player, "Select another endergenic generator as destination (current distance " + distance + ")");
            }
        } else if (coord.equals(thisCoord)) {
            // Unselect this one.
            RFTools.instance.clientInfo.setSelectedTE(null);
            RFTools.instance.clientInfo.setDestinationTE(null);
        } else {
            // Make a link.
            EndergenicTileEntity otherTE = (EndergenicTileEntity) tileEntity;
            int distance = otherTE.calculateDistance(thisCoord);
            if (distance >= 5) {
                Logging.warn(player, "Distance is too far (maximum 4)");
                return;
            }
            otherTE.setDestination(thisCoord);
            RFTools.instance.clientInfo.setSelectedTE(null);
            RFTools.instance.clientInfo.setDestinationTE(null);
            Logging.message(player, "Destination is set (distance " + otherTE.getDistanceInTicks() + " ticks)");
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;
    }

    public int getChargingMode() {
        return chargingMode;
    }

    /**
     * Calculate the distance in ticks between this endergenic generator and the given coordinate.
     *
     * @param destination is the coordinate of the new destination
     * @return is the distance in ticks
     */
    public int calculateDistance(BlockPos destination) {
        double d = new Vec3d(destination).distanceTo(new Vec3d(getPos()));
        return (int) (d / 3.0f) + 1;
    }


    public BlockPos getDestination() {
        return destination;
    }

    public void setDestination(BlockPos destination) {
        markDirtyQuick();
        this.destination = destination;
        distance = calculateDistance(destination);

        if (getWorld().isRemote) {
            // We're on the client. Send change to server.
            valueToServer(RFToolsMessages.INSTANCE, VALUE_DESTINATION, destination);
        }
    }

    public int getDistanceInTicks() {
        return distance;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        chargingMode = tagCompound.getInteger("charging");
        currentAge = tagCompound.getInteger("age");
        destination = BlockPosTools.readFromNBT(tagCompound, "dest");
        distance = tagCompound.getInteger("distance");
        prevIn = tagCompound.getBoolean("prevIn");
        badCounter = tagCompound.getByte("bad");
        goodCounter = tagCompound.getByte("good");
        pearls.clear();
        NBTTagList list = tagCompound.getTagList("pearls", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            EndergenicPearl pearl = new EndergenicPearl(tc);
            pearls.add(pearl);
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("charging", chargingMode);
        tagCompound.setInteger("age", currentAge);
        BlockPosTools.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setInteger("distance", distance);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setByte("bad", (byte) badCounter);
        tagCompound.setByte("good", (byte) goodCounter);

        NBTTagList pearlList = new NBTTagList();
        for (EndergenicPearl pearl : pearls) {
            pearlList.appendTag(pearl.getTagCompound());
        }
        tagCompound.setTag("pearls", pearlList);
        return tagCompound;
    }

    @Override
    public TypedMap executeWithResult(String command, TypedMap args) {
        TypedMap rc = super.executeWithResult(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETSTATS.equals(command)) {
            return TypedMap.builder()
                    .put(PARAM_STATRF, lastRfPerTick)
                    .put(PARAM_STATLOST, lastPearlsLost)
                    .put(PARAM_STATLAUNCHED, lastPearlsLaunched)
                    .put(PARAM_STATRF, lastChargeCounter)
                    .build();
        }
        return null;
    }

    @Override
    public boolean receiveDataFromServer(String command, @Nonnull TypedMap value) {
        boolean rc = super.receiveDataFromServer(command, value);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETSTATS.equals(command)) {
            GuiEndergenic.fromServer_lastRfPerTick = value.get(PARAM_STATRF);
            GuiEndergenic.fromServer_lastPearlsLost = value.get(PARAM_STATLOST);
            GuiEndergenic.fromServer_lastPearlsLaunched = value.get(PARAM_STATLAUNCHED);
            GuiEndergenic.fromServer_lastPearlOpportunities = value.get(PARAM_STATOPPORTUNITIES);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> List<T> executeWithResultList(String command, TypedMap args, Type<T> type) {
        List<T> list = super.executeWithResultList(command, args, type);
        if (!list.isEmpty()) {
            return list;
        }
        if (PacketGetHudLog.CMD_GETHUDLOG.equals(command)) {
            return type.convert(getHudLog());
        }
        return list;
    }

    @Override
    public <T> boolean receiveListFromServer(String command, List<T> list, Type<T> type) {
        boolean rc = super.receiveListFromServer(command, list, type);
        if (rc) {
            return true;
        }
        if (PacketGetHudLog.CLIENTCMD_GETHUDLOG.equals(command)) {
            clientHudLog = Type.STRING.convert(list);
            return true;
        }
        return false;
    }

    @Override
    public boolean wrenchUse(World world, BlockPos pos, EnumFacing side, EntityPlayer player) {
        if (world.isRemote) {
            SoundEvent pling = SoundEvent.REGISTRY.getObject(new ResourceLocation("block.note.pling"));
            world.playSound(player, pos, pling, SoundCategory.BLOCKS, 1.0f, 1.0f);
            useWrench(player);
        }
        return true;
    }


    @Override
    @net.minecraftforge.fml.common.Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if (mode == ProbeMode.EXTENDED) {
            IItemStyle style = probeInfo.defaultItemStyle().width(16).height(13);
            ILayoutStyle layoutStyle = probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER);
            probeInfo.text(TextFormatting.BLUE + "Stats over the last 5 seconds:");
            probeInfo.horizontal(layoutStyle)
                    .item(new ItemStack(Items.REDSTONE), style)
                    .text("Charged " + getLastChargeCounter() + " time(s)");
            probeInfo.horizontal(layoutStyle)
                    .item(new ItemStack(Items.ENDER_PEARL), style)
                    .text("Fired " + getLastPearlsLaunched())
                    .text(" / Lost " + getLastPearlsLost());
            if (getLastPearlsLost() > 0) {
                probeInfo.text(TextFormatting.RED + getLastPearlsLostReason());
            }
            if (getLastPearlArrivedAt() > -2) {
                probeInfo.text("Last pearl arrived at " + getLastPearlArrivedAt());
            }
            probeInfo.horizontal()
                    .text(TextFormatting.GREEN + "RF Gain " + getLastRfGained())
                    .text(" / ")
                    .text(TextFormatting.RED + "Lost " + getLastRfLost())
                    .text(" (RF/t " + getLastRfPerTick() + ")");
        } else {
            probeInfo.text("(sneak to get statistics)");
        }
    }

}
