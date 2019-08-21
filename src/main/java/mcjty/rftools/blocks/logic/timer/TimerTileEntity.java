package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.TickOrderHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_TIMER;

public class TimerTileEntity extends LogicTileEntity implements ITickableTileEntity, TickOrderHandler.ICheckStateServer {

    public static final String CMD_SETDELAY = "timer.setDelay";
    public static final String CMD_SETPAUSES = "timer.setPauses";

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 20;
    private int timer = 0;
    private boolean redstonePauses = false;

    public TimerTileEntity() {
        super(TYPE_TIMER);
    }

    public int getDelay() {
        return delay;
    }

    public int getTimer() {
        return timer;
    }

    public boolean getRedstonePauses() {
        return redstonePauses;
    }

    public void setDelay(int delay) {
        this.delay = delay;
        timer = delay;
        markDirtyClient();
    }

    public void setRedstonePauses(boolean redstonePauses) {
        this.redstonePauses = redstonePauses;
        if(redstonePauses && powerLevel > 0) {
            timer = delay;
        }
        markDirtyClient();
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            TickOrderHandler.queueTimer(this);
        }
    }

    @Override
    public void checkStateServer() {
        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;

        markDirty();

        if (pulse) {
            timer = delay;
        }

        int newout;

        if(!redstonePauses || !prevIn) {
            timer--;
        }
        if (timer <= 0) {
            timer = delay;
            newout = 15;
        } else {
            newout = 0;
        }

        setRedstoneState(newout);
    }

    @Override
    public int getDimension() {
        return world.getDimension().getType().getId();
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInt("timer");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14 loot table
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        delay = tagCompound.getInt("delay");
        redstonePauses = tagCompound.getBoolean("redstonePauses");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        tagCompound.putBoolean("prevIn", prevIn);
        tagCompound.putInt("timer", timer);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot table
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("delay", delay);
        tagCompound.putBoolean("redstonePauses", redstonePauses);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETDELAY.equals(command)) {
            String text = params.get(TextField.PARAM_TEXT);
            int delay;
            try {
                delay = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                delay = 1;
            }
            setDelay(delay);
            return true;
        } else if (CMD_SETPAUSES.equals(command)) {
            Boolean on = params.get(ToggleButton.PARAM_ON);
            setRedstonePauses(on);
            return true;
        }
        return false;
    }


//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextFormatting.GREEN + "Time: " + TextFormatting.WHITE + getTimer());
//    }
}
