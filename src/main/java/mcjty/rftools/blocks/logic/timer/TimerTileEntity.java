package mcjty.rftools.blocks.logic.timer;

import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.ToggleButton;
import mcjty.rftools.TickOrderHandler;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.lib.typed.TypedMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;

public class TimerTileEntity extends LogicTileEntity implements ITickable, TickOrderHandler.ICheckStateServer {

    public static final String CMD_SETDELAY = "timer.setDelay";
    public static final String CMD_SETPAUSES = "timer.setPauses";

    // For pulse detection.
    private boolean prevIn = false;

    private int delay = 20;
    private int timer = 0;
    private boolean redstonePauses = false;

    public TimerTileEntity() {
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
    public void update() {
        if (!getWorld().isRemote) {
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        prevIn = tagCompound.getBoolean("prevIn");
        timer = tagCompound.getInteger("timer");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        delay = tagCompound.getInteger("delay");
        redstonePauses = tagCompound.getBoolean("redstonePauses");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        tagCompound.setBoolean("prevIn", prevIn);
        tagCompound.setInteger("timer", timer);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("delay", delay);
        tagCompound.setBoolean("redstonePauses", redstonePauses);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
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


    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextFormatting.GREEN + "Time: " + TextFormatting.WHITE + getTimer());
    }
}
