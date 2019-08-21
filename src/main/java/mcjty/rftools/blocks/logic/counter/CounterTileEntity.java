package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.setup.CommandHandler;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class CounterTileEntity extends LogicTileEntity {

    public static final String CMD_SETCOUNTER = "counter.setCounter";
    public static final String CMD_SETCURRENT = "counter.setCurrent";

    // For pulse detection.
    private boolean prevIn = false;

    private int counter = 1;
    private int current = 0;

    public CounterTileEntity() {
    }

    public int getCounter() {
        return counter;
    }

    public int getCurrent() {
        return current;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        current = 0;
        markDirtyClient();
    }

    public void setCurrent(int current) {
        this.current = current;
        markDirtyClient();
    }

    protected void update() {
        if (getWorld().isRemote) {
            return;
        }
        boolean pulse = (powerLevel > 0) && !prevIn;
        prevIn = powerLevel > 0;

        int newout = 0;

        if (pulse) {
            current++;
            if (current >= counter) {
                current = 0;
                newout = 15;
            }

            markDirty();
            setRedstoneState(newout);
        }
    }

    @Override
    public void readFromNBT(CompoundNBT tagCompound) {
        super.readFromNBT(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
        if (counter == 0) {
            counter = 1;
        }
        current = tagCompound.getInteger("current");
    }

    @Override
    public CompoundNBT writeToNBT(CompoundNBT tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", powerOutput > 0);
        tagCompound.setBoolean("prevIn", prevIn);
        return tagCompound;
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
        tagCompound.setInteger("current", current);
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        super.checkRedstone(world, pos);
        update();
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETCOUNTER.equals(command)) {
            int counter;
            try {
                counter = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                counter = 1;
            }
            setCounter(counter);
            return true;
        } else if (CMD_SETCURRENT.equals(command)) {
            int current;
            try {
                current = Integer.parseInt(params.get(TextField.PARAM_TEXT));
            } catch (NumberFormatException e) {
                current = 1;
            }
            setCurrent(current);
            return true;
        }
        return false;
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        probeInfo.text(TextFormatting.GREEN + "Current: " + getCurrent());
    }

    private static long lastTime = 0;

    // Client side
    public static int cntReceived = 0;


    @SideOnly(Side.CLIENT)
    @Override
    @Optional.Method(modid = "waila")
    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
        super.addWailaBody(itemStack, currenttip, accessor, config);

        if (System.currentTimeMillis() - lastTime > 500) {
            lastTime = System.currentTimeMillis();
            RFToolsMessages.sendToServer(CommandHandler.CMD_GET_COUNTER_INFO,
                    TypedMap.builder().put(CommandHandler.PARAM_DIMENSION, getWorld().provider.getDimension()).put(CommandHandler.PARAM_POS, getPos()));
        }

        currenttip.add(TextFormatting.GREEN + "Current: " + cntReceived);
    }


}
