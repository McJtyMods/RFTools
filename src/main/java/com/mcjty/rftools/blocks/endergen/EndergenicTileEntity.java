package com.mcjty.rftools.blocks.endergen;

import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.varia.Coordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class EndergenicTileEntity extends GenericEnergyHandlerTileEntity {

    private static Random random = new Random();

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

    // The location of the destination endergenic generator.
    private Coordinate destination = null;

    // Statistics for this generator.
    private int rfRemembered = 0;
    private int pearlsLaunched = 0;
    private int pearlsLost = 0;
    private int pearlsOpportunities = 0;
    private int ticks = 100;
    private int rfAverage = 0;

    // This table indicates how much RF is produced when an endergenic pearl hits this block
    // at that specific chargingMode.
    private static int rfPerHit[] = new int[]{ 0, 100, 200, 400, 800, 1600, 3200, 6400, 12800, 6400, 3200, 1600, 800, 400, 200, 100 };

    // This value indicates the chance (with 0 being no chance and 100 being 100% chance) that an
    // endergenic pearl is lost while holding it.
    public static int chanceLost = 2;

    // This value indicates how much RF is being consumed every tick to try to keep the endergenic pearl.
    public static int rfToHoldPearl = 1000;

    public EndergenicTileEntity() {
        super(1000000, 20000, 20000);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        ticks--;
        if (ticks < 0) {
            ticks = 100;
            int rf = getEnergyStored(ForgeDirection.DOWN);
            rfAverage = (rf-rfRemembered) / 100;
            rfRemembered = rf;
            pearlsLaunched = 0;
            pearlsLost = 0;
            pearlsOpportunities = 0;
        }

        if (chargingMode == CHARGE_IDLE) {
            // Do nothing
            return;
        }

        if (chargingMode == CHARGE_HOLDING) {
            if (random.nextInt(100) <= chanceLost) {
                // Pearl is lost.
                discardPearl();
            } else {
                // Consume energy to keep the endergenic pearl.
                int rfExtracted = extractEnergy(ForgeDirection.DOWN, rfToHoldPearl, false);
                if (rfExtracted < rfToHoldPearl) {
                    // Not enough energy. Pearl is lost.
                    discardPearl();

                }
            }
            return;
        }

        // Else we're charging up.
        chargingMode++;
        if (chargingMode >= 16) {
            chargingMode = CHARGE_IDLE;
        }
    }

    private void discardPearl() {
        pearlsLost++;
        chargingMode = CHARGE_IDLE;
    }

    public void firePearl() {
        if (destination == null) {
            // There is no destination so the pearl is simply lost.
            discardPearl();
        } else {
            chargingMode = CHARGE_IDLE;
            pearlsLaunched++;
            // @todo Fire pearl
        }
    }

    public void receivePearl() {
        if (chargingMode == CHARGE_HOLDING) {
            // If this block is already holding a pearl and it still has one then both pearls are
            // automatically lost.
            chargingMode = CHARGE_IDLE;
        } else if (chargingMode == CHARGE_IDLE) {
            // If this block is idle and it is hit by a pearl then the pearl is lost and nothing
            // happens.
            chargingMode = CHARGE_IDLE;
        } else {
            // Otherwise we get RF and this block goes into holding mode.
            receiveEnergy(ForgeDirection.DOWN, rfPerHit[chargingMode], false);
            chargingMode = CHARGE_HOLDING;
        }
    }

    public void startCharging() {
        chargingMode = 1;
        pearlsOpportunities++;
    }

    public int getChargingMode() {
        return chargingMode;
    }

    public int getAverageRF() {
        return rfAverage;
    }

    public Coordinate getDestination() {
        return destination;
    }

    public void setDestination(Coordinate destination) {
        this.destination = destination;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);

        chargingMode = tagCompound.getInteger("charging");
        destination = Coordinate.readFromNBT(tagCompound, "dest");
        rfAverage = tagCompound.getInteger("rfAverage");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);

        tagCompound.setInteger("charging", chargingMode);
        Coordinate.writeToNBT(tagCompound, "dest", destination);
        tagCompound.setInteger("rfAverage", rfAverage);
    }
}
