package com.mcjty.rftools.blocks.endergen;

import cofh.api.energy.IEnergyHandler;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.RFTools;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketServerCommand;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class EndergenicTileEntity extends GenericEnergyHandlerTileEntity {

    private static Random random = new Random();

    public static String CMD_SETDESTINATION = "setDest";
    public static String CMD_GETSTAT_RF = "getStatRF";
    public static String CLIENTCMD_GETSTAT_RF = "getStatRF";
    public static String CMD_GETSTAT_LOST = "getStatLost";
    public static String CLIENTCMD_GETSTAT_LOST = "getStatLost";
    public static String CMD_GETSTAT_LAUNCHED = "getStatLaunched";
    public static String CLIENTCMD_GETSTAT_LAUNCHED = "getStatLaunched";
    public static String CMD_GETSTAT_OPPORTUNITIES = "getStatOpp";
    public static String CLIENTCMD_GETSTAT_OPPORTUNITIES = "getStatOpp";

    public static final int CHARGE_IDLE = 0;
    public static final int CHARGE_HOLDING = -1;

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
    private Coordinate destination = null;
    private int distance = 0;           // Distance between this block and destination in ticks

    // For pulse detection.
    private boolean prevIn = false;

    // Statistics for this generator.
    // These values count what is happening.
    private int rfGained = 0;
    private int rfLost = 0;
    private int pearlsLaunched = 0;
    private int pearlsLost = 0;
    private int pearlsOpportunities = 0;
    private int ticks = 100;

    // These values actually contain valid statistics.
    private int lastRfPerTick = 0;
    private int lastPearlsLost = 0;
    private int lastPearlsLaunched = 0;
    private int lastPearlOpportunities = 0;

    // Current traveling pearls.
    private List<EndergenicPearl> pearls = new ArrayList<EndergenicPearl>();

    // List of monitors for this endergenic.
    private List<Coordinate> monitors = new ArrayList<Coordinate>();

    // This table indicates how much RF is produced when an endergenic pearl hits this block
    // at that specific chargingMode.
    private static int rfPerHit[] = new int[]{ 0, 100, 150, 200, 400, 800, 1600, 3200, 6400, 8000, 12800, 8000, 6400, 2500, 1000, 100 };

    private int tickCounter = 0;            // Only used for logging, counts server ticks.

    public EndergenicTileEntity() {
        super(5000000, 0, 20000);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        tickCounter++;

        ticks--;
        if (ticks < 0) {
            lastRfPerTick = (rfGained - rfLost) / 100;
            lastPearlsLost = pearlsLost;
            lastPearlsLaunched = pearlsLaunched;
            lastPearlOpportunities = pearlsOpportunities;

            ticks = 100;
            rfGained = 0;
            rfLost = 0;
            pearlsLaunched = 0;
            pearlsLost = 0;
            pearlsOpportunities = 0;
        }

        handlePearls();
        handleSendingEnergy();

        // First check if we're holding a pearl to see if the pearl will be lost.
        if (chargingMode == CHARGE_HOLDING) {
            if (random.nextInt(100) <= EndergenicConfiguration.chanceLost) {
                // Pearl is lost.
                log("Server Tick: discard pearl randomly");
                discardPearl();
            }
        }

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue = BlockTools.getRedstoneSignalIn(meta);
        boolean pulse = newvalue && !prevIn;
        prevIn = newvalue;
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
            int rfExtracted = extractEnergy(ForgeDirection.DOWN, EndergenicConfiguration.rfToHoldPearl, false);
            log("Server Tick: holding pearl, consume "+rfExtracted+" RF (to hold " + EndergenicConfiguration.rfToHoldPearl+")");
            rfLost += rfExtracted;
            if (rfExtracted < EndergenicConfiguration.rfToHoldPearl) {
                // Not enough energy. Pearl is lost.
                log("Server Tick: insufficient energy to hold pearl");
                discardPearl();

            }
            return;
        }

        // Else we're charging up.
        markDirty();
        chargingMode++;
        if (chargingMode >= 16) {
            log("Server Tick: charging mode ends -> idle");
            chargingMode = CHARGE_IDLE;
        }
    }

    private void log(String message) {
        if (EndergenicConfiguration.logEndergenic) {
            String id = tickCounter + ": " + xCoord + "," + yCoord + "," + zCoord + ": ";
            RFTools.instance.logger.log(Level.INFO, id + message);
        }
    }

    public void addMonitor(Coordinate c) {
        monitors.add(c);
    }

    public void removeMonitor(Coordinate c) {
        monitors.remove(c);
    }

    /**
     * Something happens, we need to notify all ender monitors.
     * @param mode is the new mode
     */
    private void fireMonitors(EnderMonitorMode mode) {
        boolean cleanup = false;
        for (Coordinate c : monitors) {
            TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
            if (te instanceof EnderMonitorTileEntity) {
                EnderMonitorTileEntity enderMonitorTileEntity = (EnderMonitorTileEntity) te;
                enderMonitorTileEntity.fireFromEndergenic(mode);
            } else {
                cleanup = true;
            }
        }
        // This should normally not be needed but to be safe we make sure that we clean
        // up defunct monitor references.
        if (cleanup) {
            List<Coordinate> newMonitors = new ArrayList<Coordinate>();
            for (Coordinate c : monitors) {
                TileEntity te = worldObj.getTileEntity(c.getX(), c.getY(), c.getZ());
                if (te instanceof EnderMonitorTileEntity) {
                    newMonitors.add(c);
                }
            }
            monitors = newMonitors;
        }
    }

    @Override
    public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z) {
        return super.shouldRefresh(oldBlock, newBlock, oldMeta, newMeta, world, x, y, z);
    }

    private void handleSendingEnergy() {
        int energyStored = getEnergyStored(ForgeDirection.DOWN);
        if (energyStored <= EndergenicConfiguration.keepRfInBuffer) {
            return;
        }
        energyStored -= EndergenicConfiguration.keepRfInBuffer;

        for (int i = 0 ; i < 6 ; i++) {
            ForgeDirection dir = ForgeDirection.getOrientation(i);
            int x = xCoord + dir.offsetX;
            int y = yCoord + dir.offsetY;
            int z = zCoord + dir.offsetZ;
            TileEntity te = worldObj.getTileEntity(x, y, z);
            if (te instanceof IEnergyHandler) {
                IEnergyHandler handler = (IEnergyHandler) te;
                ForgeDirection opposite = dir.getOpposite();
                if (handler.canConnectEnergy(opposite)) {
                    int rfToGive;
                    if (EndergenicConfiguration.rfOutput <= energyStored) {
                        rfToGive = EndergenicConfiguration.rfOutput;
                    } else {
                        rfToGive = energyStored;
                    }

                    int received = handler.receiveEnergy(opposite, rfToGive, false);
                    energyStored -= extractEnergy(ForgeDirection.DOWN, received, false);
                    if (energyStored <= 0) {
                        break;
                    }
                }
            }
        }
    }

    // Handle all pearls that are currently in transit.
    private void handlePearls() {
        if (pearls.isEmpty()) {
            return;
        }
        List<EndergenicPearl> newlist = new ArrayList<EndergenicPearl>();
        for (EndergenicPearl pearl : pearls) {
            log("Pearls: age=" + pearl.getAge() + ", ticks left=" + pearl.getTicksLeft());
            if (!pearl.handleTick(worldObj)) {
                // Keep the pearl. It has not arrived yet.
                newlist.add(pearl);
            }
        }

        // Replace the old list with the new one.
        pearls = newlist;
    }

    private void discardPearl() {
        spawnParticles("smoke", EndergenicConfiguration.badParticleCount);
        markDirty();
        pearlsLost++;
        chargingMode = CHARGE_IDLE;
        fireMonitors(EnderMonitorMode.MODE_LOSTPEARL);
    }

    /**
     * Get the current destination. This function checks first if that destination is
     * still valid and if not it is reset to null (i.e. the destination was removed).
     * @return the destination TE or null if there is no valid one
     */
    private EndergenicTileEntity getDestinationTE() {
        if (destination == null) {
            return null;
        }
        TileEntity te = worldObj.getTileEntity(destination.getX(), destination.getY(), destination.getZ());
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        } else {
            destination = null;
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return null;
        }
    }

    public void firePearl() {
        markDirty();
        // This method assumes we're in holding mode.
        getDestinationTE();
        if (destination == null) {
            // There is no destination so the pearl is simply lost.
            log("Fire Pearl: pearl lost due to lack of destination");
            discardPearl();
        } else {
            log("Fire Pearl: pearl is launched to "+destination.getX()+","+destination.getY()+","+destination.getZ());
            chargingMode = CHARGE_IDLE;
            pearlsLaunched++;
            pearls.add(new EndergenicPearl(distance, destination, currentAge+1));
            fireMonitors(EnderMonitorMode.MODE_PEARLFIRED);
        }
    }

    public void firePearlFromInjector() {
        markDirty();
        // This method assumes we're not in holding mode.
        getDestinationTE();
        chargingMode = CHARGE_IDLE;
        if (destination == null) {
            // There is no destination so the injected pearl is simply lost.
            log("Fire Pearl from injector: pearl lost due to lack of destination");
            discardPearl();
        } else {
            log("Fire Pearl from injector: pearl is launched to "+destination.getX()+","+destination.getY()+","+destination.getZ());
            pearlsLaunched++;
            pearls.add(new EndergenicPearl(distance, destination, 0));
            fireMonitors(EnderMonitorMode.MODE_PEARLFIRED);
        }
    }

    // This generator receives a pearl. The age of the pearl is how many times the pearl has
    // already generated power.
    public void receivePearl(int age) {
        fireMonitors(EnderMonitorMode.MODE_PEARLARRIVED);
        markDirty();
        if (chargingMode == CHARGE_HOLDING) {
            log("Receive Pearl: pearl arrives but already holding -> both are lost");
            // If this block is already holding a pearl and it still has one then both pearls are
            // automatically lost.
            discardPearl();
        } else if (chargingMode == CHARGE_IDLE) {
            log("Receive Pearl: pearl arrives but generator is idle -> pearl is lost");
            // If this block is idle and it is hit by a pearl then the pearl is lost and nothing
            // happens.
            discardPearl();
        } else {
            // Otherwise we get RF and this block goes into holding mode.
            int rf = rfPerHit[chargingMode];
            // Give a bonus for pearls that have been around a bit longer.
            int a = age*5;
            if (a > 100) {
                a = 100;
            }
            rf += rf * a / 100;     // Maximum 200% bonus. Minimum no bonus.
            rfGained += rf;
            log("Receive Pearl: pearl arrives at tick " + chargingMode + ", age=" + age + ", RF=" + rf);
            modifyEnergyStored(rf);

            spawnParticles("portal", EndergenicConfiguration.goodParticleCount);

            chargingMode = CHARGE_HOLDING;
            currentAge = age;
        }
    }

    private void spawnParticles(String name, int amount) {
        if (amount <= 0) {
            return;
        }
        float vecX = (random.nextFloat() - 0.5F) * 0.2F;
        float vecY = (random.nextFloat()) * 0.1F;
        float vecZ = (random.nextFloat() - 0.5F) * 0.2F;
        ((WorldServer)worldObj).func_147487_a(name, xCoord + 0.5f, yCoord + 1.1f, zCoord + 0.5f, amount, vecX, vecY, vecZ, 0.3f);
    }

    public void startCharging() {
        markDirty();
        chargingMode = 1;
        pearlsOpportunities++;
    }

    // Called from client side when a wrench is used.
    public void useWrench(EntityPlayer player) {
        EndergenicTileEntity otherTE = RFTools.instance.clientInfo.getSelectedEndergenicTileEntity();
        if (otherTE == null) {
            // None selected. Just select this one.
            RFTools.instance.clientInfo.setSelectedEndergenicTileEntity(this);
            EndergenicTileEntity destinationTE = getDestinationTE();
            RFTools.instance.clientInfo.setDestinationEndergenicTileEntity(destinationTE);
            if (destinationTE == null) {
                RFTools.message(player, "Select another endergenic generator as destination");
            } else {
                int distance = getDistanceInTicks();
                RFTools.message(player, "Select another endergenic generator as destination (current distance "+distance+")");
            }
        } else if (otherTE.equals(this)) {
            // Unselect this one.
            RFTools.instance.clientInfo.setSelectedEndergenicTileEntity(null);
            RFTools.instance.clientInfo.setDestinationEndergenicTileEntity(null);
        } else {
            // Make a link.
            Coordinate c = new Coordinate(xCoord, yCoord, zCoord);
            int distance = otherTE.calculateDistance(c);
            if (distance >= 5) {
                RFTools.warn(player, "Distance is too far (maximum 4)");
                return;
            }
            otherTE.setDestination(c);
            RFTools.instance.clientInfo.setSelectedEndergenicTileEntity(null);
            RFTools.instance.clientInfo.setDestinationEndergenicTileEntity(null);
            RFTools.message(player, "Destination is set (distance "+otherTE.getDistanceInTicks()+" ticks)");
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
     * @param destination is the coordinate of the new destination
     * @return is the distance in ticks
     */
    public int calculateDistance(Coordinate destination) {
        double d = Vec3.createVectorHelper(destination.getX(), destination.getY(), destination.getZ()).distanceTo(
                Vec3.createVectorHelper(xCoord, yCoord, zCoord));
        return (int) (d / 3.0f) + 1;
    }

    public void setDestination(Coordinate destination) {
        markDirty();
        this.destination = destination;
        distance = calculateDistance(destination);

        if (worldObj.isRemote) {
            // We're on the client. Send change to server.
            PacketHandler.INSTANCE.sendToServer(new PacketServerCommand(xCoord, yCoord, zCoord,
                    EndergenicTileEntity.CMD_SETDESTINATION,
                    new Argument("dest", destination)));
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
        destination = Coordinate.readFromNBT(tagCompound, "dest");
        distance = tagCompound.getInteger("distance");
        prevIn = tagCompound.getBoolean("prevIn");
        pearls.clear();
        NBTTagList list = tagCompound.getTagList("pearls", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < list.tagCount() ; i++) {
            NBTTagCompound tc = list.getCompoundTagAt(i);
            EndergenicPearl pearl = new EndergenicPearl(tc);
            pearls.add(pearl);
        }

        monitors.clear();
        NBTTagList monitorList = tagCompound.getTagList("monitors", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < monitorList.tagCount() ; i++) {
            NBTTagCompound tc = monitorList.getCompoundTagAt(i);
            Coordinate c = Coordinate.readFromNBT(tc, "c");
            if (c != null) {
                monitors.add(c);
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("charging", chargingMode);
        tagCompound.setInteger("age", currentAge);
        Coordinate.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setInteger("distance", distance);
        tagCompound.setBoolean("prevIn", prevIn);

        NBTTagList pearlList = new NBTTagList();
        for (EndergenicPearl pearl : pearls) {
            pearlList.appendTag(pearl.getTagCompound());
        }
        tagCompound.setTag("pearls", pearlList);

        NBTTagList monitorList = new NBTTagList();
        for (Coordinate c : monitors) {
            monitorList.appendTag(Coordinate.writeToNBT(c));
        }
        tagCompound.setTag("monitors", monitorList);
    }

    @Override
    public boolean execute(String command, Map<String, Argument> args) {
        boolean rc = super.execute(command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETDESTINATION.equals(command)) {
            setDestination(args.get("dest").getCoordinate());
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
        if (CMD_GETSTAT_RF.equals(command)) {
            return lastRfPerTick;
        } else if (CMD_GETSTAT_LOST.equals(command)) {
            return lastPearlsLost;
        } else if (CMD_GETSTAT_LAUNCHED.equals(command)) {
            return lastPearlsLaunched;
        } else if (CMD_GETSTAT_OPPORTUNITIES.equals(command)) {
            return lastPearlOpportunities;
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer value) {
        boolean rc = super.execute(command, value);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETSTAT_RF.equals(command)) {
            GuiEndergenic.fromServer_lastRfPerTick = value;
            return true;
        } else if (CLIENTCMD_GETSTAT_LOST.equals(command)) {
            GuiEndergenic.fromServer_lastPearlsLost = value;
            return true;
        } else if (CLIENTCMD_GETSTAT_LAUNCHED.equals(command)) {
            GuiEndergenic.fromServer_lastPearlsLaunched = value;
            return true;
        } else if (CLIENTCMD_GETSTAT_OPPORTUNITIES.equals(command)) {
            GuiEndergenic.fromServer_lastPearlOpportunities = value;
            return true;
        }
        return false;
    }
}
