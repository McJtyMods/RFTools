package mcjty.rftools.blocks.logic.counter;

import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.tileentity.LogicTileEntity;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static mcjty.rftools.blocks.logic.LogicBlockSetup.TYPE_COUNTER;

public class CounterTileEntity extends LogicTileEntity {

    public static final String CMD_SETCOUNTER = "counter.setCounter";
    public static final String CMD_SETCURRENT = "counter.setCurrent";

    // For pulse detection.
    private boolean prevIn = false;

    private int counter = 1;
    private int current = 0;

    public CounterTileEntity() {
        super(TYPE_COUNTER);
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
        if (world.isRemote) {
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
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        powerOutput = tagCompound.getBoolean("rs") ? 15 : 0;
        prevIn = tagCompound.getBoolean("prevIn");
        readRestorableFromNBT(tagCompound);
    }

    // @todo 1.14
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        counter = tagCompound.getInt("counter");
        if (counter == 0) {
            counter = 1;
        }
        current = tagCompound.getInt("current");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("rs", powerOutput > 0);
        tagCompound.putBoolean("prevIn", prevIn);
        writeRestorableToNBT(tagCompound);
        return tagCompound;
    }

    // @todo 1.14 loot tables
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        tagCompound.putInt("counter", counter);
        tagCompound.putInt("current", current);
    }

    @Override
    public void checkRedstone(World world, BlockPos pos) {
        super.checkRedstone(world, pos);
        update();
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
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

//    @Override
//    @Optional.Method(modid = "theoneprobe")
//    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, BlockState blockState, IProbeHitData data) {
//        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
//        probeInfo.text(TextFormatting.GREEN + "Current: " + getCurrent());
//    }
//
//    private static long lastTime = 0;
//
    // Client side
    // @todo 1.14 rewrite using new way to sync integers through container!
    public static int cntReceived = 0;


//    @SideOnly(Side.CLIENT)
//    @Override
//    @Optional.Method(modid = "waila")
//    public void addWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config) {
//        super.addWailaBody(itemStack, currenttip, accessor, config);
//
//        if (System.currentTimeMillis() - lastTime > 500) {
//            lastTime = System.currentTimeMillis();
//            RFToolsMessages.sendToServer(CommandHandler.CMD_GET_COUNTER_INFO,
//                    TypedMap.builder().put(CommandHandler.PARAM_DIMENSION, world.getDimension().getType().getId()).put(CommandHandler.PARAM_POS, getPos()));
//        }
//
//        currenttip.add(TextFormatting.GREEN + "Current: " + cntReceived);
//    }


}
