package mcjty.rftools.blocks.logic.sequencer;

import mcjty.lib.gui.widgets.ChoiceLabel;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.TickOrderHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_SEQUENCER;

public class SequencerTileEntity extends LogicTileEntity implements ITickableTileEntity, TickOrderHandler.ICheckStateServer {

    public static final String CMD_MODE = "sequencer.mode";
    public static final String CMD_FLIPBITS = "sequencer.flipBits";
    public static final String CMD_CLEARBITS = "sequencer.clearBits";
    public static final String CMD_SETDELAY = "sequencer.setDelay";
    public static final String CMD_SETCOUNT = "sequencer.setCount";
    public static final String CMD_SETENDSTATE = "sequencer.setEndState";

    public static final String CMD_SETBIT = "sequencer.setBit";
    public static final Key<Integer> PARAM_BIT = new Key<>("bit", Type.INTEGER);
    public static final Key<Boolean> PARAM_CHOICE = new Key<>("choice", Type.BOOLEAN);

    private SequencerMode mode = SequencerMode.MODE_ONCE1;
    private long cycleBits = 0;
    private int currentStep = -1;
    private int stepCount = 64;
    private boolean endState = false;

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 1;
    private int timer = 0;

    public SequencerTileEntity() {
        super(TYPE_SEQUENCER);
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

    public boolean getEndState() {
        return endState;
    }

    public void setEndState(boolean endState) {
        this.endState = endState;
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
    public void tick() {
        if (!world.isRemote) {
            TickOrderHandler.queueSequencer(this);
        }
    }

    @Override
    public void checkStateServer() {
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

        setRedstoneState(checkOutput() ? 15 : 0);

        handleCycle(powerLevel > 0);
    }

    @Override
    public int getDimension() {
        return world.getDimension().getType().getId();
    }

    public boolean checkOutput() {
        return currentStep == -1 ? endState : getCycleBit(currentStep);
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        currentStep = tagCompound.getInt("step");
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInt("timer");
        readRestorableFromNBT(tagCompound);
    }

    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        // @todo 1.14 loot table
        cycleBits = tagCompound.getLong("bits");
        int m = tagCompound.getInt("mode");
        mode = SequencerMode.values()[m];
        delay = tagCompound.getInt("delay");
        if (delay == 0) {
            delay = 1;
        }
        stepCount = tagCompound.getInt("stepCount");
        if (stepCount == 0) {
            stepCount = 64;
        }
        endState = tagCompound.getBoolean("endState");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        tagCompound.putInt("step", currentStep);
        tagCompound.putBoolean("prevIn", prevIn);
        tagCompound.putInt("timer", timer);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        // @todo 1.14 loot table
        tagCompound.putLong("bits", cycleBits);
        tagCompound.putInt("mode", mode.ordinal());
        tagCompound.putInt("delay", delay);
        tagCompound.putInt("stepCount", stepCount);
        tagCompound.putBoolean("endState", endState);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETENDSTATE.equals(command)) {
            boolean newChoice = "1".equals(params.get(ImageChoiceLabel.PARAM_CHOICE));
            setEndState(newChoice);
            return true;
        } else if (CMD_FLIPBITS.equals(command)) {
            flipCycleBits();
            return true;
        } else if (CMD_CLEARBITS.equals(command)) {
            clearCycleBits();
            return true;
        } else if (CMD_SETCOUNT.equals(command)) {
            int count;
            try {
                count = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                count = 64;
            }
            setStepCount(count);
            return true;
        } else if (CMD_SETDELAY.equals(command)) {
            int delay;
            try {
                delay = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                delay = 1;
            }
            setDelay(delay);
            return true;
        } else if (CMD_MODE.equals(command)) {
            SequencerMode newMode = SequencerMode.getMode(params.get(ChoiceLabel.PARAM_CHOICE));
            setMode(newMode);
            return true;
        } else if (CMD_SETBIT.equals(command)) {
            int bit = params.get(PARAM_BIT);
            boolean choice = params.get(PARAM_CHOICE);
            setCycleBit(bit, choice);
            return true;
        }
        return false;
    }

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        IProbeInfo horizontal = probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER));
//        horizontal.text(TextFormatting.GREEN + "Mode: " + getMode().getDescription());
//        TheOneProbeSupport.addSequenceElement(horizontal, getCycleBits(),
//                getCurrentStep(), mode == ProbeMode.EXTENDED);
//        int currentStep = getCurrentStep();
//        boolean rc = checkOutput();
//        probeInfo.text(TextFormatting.GREEN + "Step: " + TextFormatting.WHITE + currentStep +
//                TextFormatting.GREEN + " -> " + TextFormatting.WHITE + (rc ? "on" : "off"));
//    }

}
