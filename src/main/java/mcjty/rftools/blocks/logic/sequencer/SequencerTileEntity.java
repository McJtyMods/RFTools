package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.network.Argument;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class SequencerTileEntity extends LogicTileEntity implements ITickable {

    public static final String CMD_MODE = "mode";
    public static final String CMD_SETBIT = "setBit";
    public static final String CMD_FLIPBITS = "flipBits";
    public static final String CMD_CLEARBITS = "clearBits";
    public static final String CMD_SETDELAY = "setDelay";
    public static final String CMD_SETCOUNT = "setCount";

    private SequencerMode mode = SequencerMode.MODE_ONCE1;
    private long cycleBits = 0;
    private int currentStep = -1;
    private int stepCount = 64;

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 1;
    private int timer = 0;

    public SequencerTileEntity() {
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        timer = delay;
        markDirtyClient();
    }

    public int getStepCount() {
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        this.stepCount = stepCount >= 1 && stepCount <= 64 ? stepCount : 64;
        if(this.currentStep >= stepCount) {
            this.currentStep = stepCount - 1;
        }
        markDirtyClient();
    }

    public SequencerMode getMode() {
        return mode;
    }

    public void setMode(SequencerMode mode) {
        this.mode = mode;
        switch (mode) {
            case MODE_ONCE1:
            case MODE_ONCE2:
            case MODE_LOOP3:
            case MODE_LOOP4:
                currentStep = -1;
                break;
            case MODE_LOOP1:
            case MODE_LOOP2:
            case MODE_STEP:
                currentStep = 0;
                break;
        }
        markDirtyClient();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public boolean getCycleBit(int bit) {
        return ((cycleBits >> bit) & 1) == 1;
    }

    public long getCycleBits() {
        return cycleBits;
    }

    public void setCycleBit(int bit, boolean flag) {
        if (flag) {
            cycleBits |= 1L << bit;
        } else {
            cycleBits &= ~(1L << bit);
        }
        markDirtyClient();
    }

    public void flipCycleBits() {
        cycleBits ^= ~0L;
        markDirtyClient();
    }

    public void clearCycleBits() {
        cycleBits = 0L;
        markDirtyClient();
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    private void checkStateServer() {

        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;

        if (pulse) {
            handlePulse();
        }

        markDirty();
        timer--;
        if (timer <= 0) {
            timer = delay;
        } else {
            return;
        }

        setRedstoneState(checkOutput());

        handleCycle(powerLevel > 0);
    }

    public boolean checkOutput() {
        return currentStep != -1 && getCycleBit(currentStep);
    }

    /**
     * Handle a cycle step.
     * @param redstone true if there is a redstone signal
     */
    private void handleCycle(boolean redstone) {
        switch (mode) {
            case MODE_ONCE1:
            case MODE_ONCE2:
                if (currentStep != -1) {
                    nextStepAndStop();
                }
                break;
            case MODE_LOOP1:
                nextStep();
                break;
            case MODE_LOOP2:
                nextStep();
                break;
            case MODE_LOOP3:
                if (redstone) {
                    nextStep();
                }
                break;
            case MODE_LOOP4:
                if (redstone) {
                    nextStep();
                } else {
                    currentStep = -1;
                }
                break;
            case MODE_STEP:
                break;
        }
    }

    /**
     * Handle the arrival of a new redstone pulse.
     */
    private void handlePulse() {
        switch (mode) {
            case MODE_ONCE1:
                // If we're not doing a cycle then we start one now. Otherwise we do nothing.
                if (currentStep == -1) {
                    currentStep = 0;
                }
                break;
            case MODE_ONCE2:
                // If we're not doing a cycle then we start one now. Otherwise we restart the cycle..
                currentStep = 0;
                break;
            case MODE_LOOP1:
                // Ignore signals
                break;
            case MODE_LOOP2:
                // Set cycle to the start.
                currentStep = 0;
                break;
            case MODE_LOOP3:
            case MODE_LOOP4:
                // Ignore pulses. We just work on redstone signal.
                break;
            case MODE_STEP:
                // Go to next step.
                nextStep();
                break;
        }
    }

    private void nextStep() {
        currentStep++;
        if (currentStep >= stepCount) {
            currentStep = 0;
        }
    }

    private void nextStepAndStop() {
        currentStep++;
        if (currentStep >= stepCount) {
            currentStep = -1;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powered = tagCompound.getBoolean("rs");
        currentStep = tagCompound.getInteger("step");
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInteger("timer");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        cycleBits = tagCompound.getLong("bits");
        int m = tagCompound.getInteger("mode");
        mode = SequencerMode.values()[m];
        delay = tagCompound.getInteger("delay");
        if (delay == 0) {
            delay = 1;
        }
        stepCount = tagCompound.getInteger("stepCount");
        if (stepCount == 0) {
            stepCount = 64;
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powered);
        tagCompound.setInteger("step", currentStep);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setInteger("timer", timer);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setLong("bits", cycleBits);
        tagCompound.setInteger("mode", mode.ordinal());
        tagCompound.setInteger("delay", delay);
        tagCompound.setInteger("stepCount", stepCount);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("mode").getString();
            setMode(SequencerMode.getMode(m));
            return true;
        } else if (CMD_SETBIT.equals(command)) {
            setCycleBit(args.get("bit").getInteger(), args.get("choice").getBoolean());
            return true;
        } else if (CMD_FLIPBITS.equals(command)) {
            flipCycleBits();
            return true;
        } else if (CMD_CLEARBITS.equals(command)) {
            clearCycleBits();
            return true;
        } else if (CMD_SETDELAY.equals(command)) {
            setDelay(args.get("delay").getInteger());
            return true;
        } else if (CMD_SETCOUNT.equals(command)) {
            setStepCount(args.get("count").getInteger());
            return true;
        }
        return false;
    }
}
