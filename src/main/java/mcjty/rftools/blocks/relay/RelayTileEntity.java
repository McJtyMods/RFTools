package mcjty.rftools.blocks.relay;

import mcjty.lib.api.MachineInformation;
import mcjty.lib.compat.RedstoneFluxCompatibility;
import mcjty.lib.entity.GenericEnergyHandlerTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockTools;
import mcjty.lib.varia.EnergyTools;
import mcjty.rftools.RFTools;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import java.util.Map;

public class RelayTileEntity extends GenericEnergyHandlerTileEntity implements ITickable, MachineInformation {

    public static final int MAXENERGY = 50000;
    public static final int RECEIVEPERTICK = 50000;

    public static final String CMD_SETTINGS = "settings";

    private static final String[] TAGS = new String[]{"rfpertick_out", "rfpertick_in"};
    private static final String[] TAG_DESCRIPTIONS = new String[] {
            "The current RF/t output given by this block (last 2 seconds)",
            "The current RF/t input received by this block (last 2 seconds)"};

    private boolean[] inputModeOn = new boolean[] { false, false, false, false, false, false };
    private boolean[] inputModeOff = new boolean[] { false, false, false, false, false, false };
    private int rfOn[] = new int[] { 1000, 1000, 1000, 1000, 1000, 1000 };
    private int rfOff[] = new int[] { 0, 0, 0, 0, 0, 0 };
    public static final String DUNSWE = "DUBFLR";

    private int lastRfPerTickIn = 0;
    private int lastRfPerTickOut = 0;
    private int powerIn = 0;
    private int powerOut = 0;
    private long lastTime = 0;

    public RelayTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            checkStateServer();
        }
    }

    public int getLastRfPerTickIn() {
        return lastRfPerTickIn;
    }

    public int getLastRfPerTickOut() {
        return lastRfPerTickOut;
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
            case 0: return lastRfPerTickOut + "RF/t";
            case 1: return lastRfPerTickIn + "RF/t";
        }
        return null;
    }

    public boolean isPowered() {
        return powerLevel > 0;
    }

    private void checkStateServer() {
        long time = System.currentTimeMillis();
        if (lastTime == 0) {
            lastTime = time;
        } else if (time > lastTime + 2000) {
            lastRfPerTickIn = (int) (50 * powerIn / (time - lastTime));
            lastRfPerTickOut = (int) (50 * powerOut / (time - lastTime));
            lastTime = time;
            powerIn = 0;
            powerOut = 0;
        }

        boolean redstoneSignal = powerLevel > 0;
        int[] rf = redstoneSignal ? rfOn : rfOff;
        boolean[] inputMode = redstoneSignal ? inputModeOn : inputModeOff;

        int energyStored = getEnergyStored();
        if (energyStored <= 0) {
            return;
        }

        IBlockState state = getWorld().getBlockState(getPos());
        for (EnumFacing facing : EnumFacing.VALUES) {
            int side = BlockTools.reorient(facing, state).ordinal();
//            int side = facing.ordinal();
            if (rf[side] > 0 && !inputMode[side]) {
                TileEntity te = getWorld().getTileEntity(getPos().offset(facing));
                EnumFacing opposite = facing.getOpposite();
                if (EnergyTools.isEnergyTE(te) || (te != null && te.hasCapability(CapabilityEnergy.ENERGY, opposite))) {
                    int rfToGive;
                    if (rf[side] <= energyStored) {
                        rfToGive = rf[side];
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

                    powerOut += received;
                    energyStored -= storage.extractEnergy(received, false);
                    if (energyStored <= 0) {
                        return;
                    }
                }
            }
        }
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        boolean redstoneSignal = powerLevel > 0;

        boolean[] inputMode = redstoneSignal ? inputModeOn : inputModeOff;
        IBlockState state = getWorld().getBlockState(getPos());
        int meta = state.getBlock().getMetaFromState(state);
        int side = BlockTools.reorient(from, meta).ordinal();
        if (inputMode[side]) {
            int[] rf = redstoneSignal ? rfOn : rfOff;
            int actual = super.receiveEnergy(Math.min(maxReceive, rf[side]), simulate);
            if (!simulate) {
                powerIn += actual;
            }
            return actual;
        }
        return 0;
    }

    public boolean isInputModeOn(int side) {
        return inputModeOn[side];
    }

    public boolean isInputModeOff(int side) {
        return inputModeOff[side];
    }

    public int getRfOn(int side) {
        return rfOn[side];
    }

    public int getRfOff(int side) {
        return rfOff[side];
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        if (tagCompound.hasKey("rfOn")) {
            // Old block
            int on = tagCompound.getInteger("rfOn");
            int off = tagCompound.getInteger("rfOff");
            for (int i = 0 ; i < 6 ; i++) {
                rfOn[i] = on;
                rfOff[i] = off;
                inputModeOn[i] = false;
                inputModeOff[i] = false;
            }
        } else {
            int[] on = tagCompound.getIntArray("on");
            int[] off = tagCompound.getIntArray("off");
            System.arraycopy(on, 0, rfOn, 0, Math.min(6, on.length));
            System.arraycopy(off, 0, rfOff, 0, Math.min(6, off.length));

            byte[] inOn = tagCompound.getByteArray("inputOn");
            byte[] inOff = tagCompound.getByteArray("inputOff");
            for (int i = 0 ; i < Math.min(6, inOn.length) ; i++) {
                inputModeOn[i] = inOn[i] > 0;
            }
            for (int i = 0 ; i < Math.min(6, inOff.length) ; i++) {
                inputModeOff[i] = inOff[i] > 0;
            }
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setIntArray("on", rfOn);
        tagCompound.setIntArray("off", rfOff);
        byte[] inOn = new byte[6];
        byte[] inOff = new byte[6];
        for (int i = 0 ; i < 6 ; i++) {
            inOn[i] = (byte) (inputModeOn[i] ? 1 : 0);
            inOff[i] = (byte) (inputModeOff[i] ? 1 : 0);
        }
        tagCompound.setByteArray("inputOn", inOn);
        tagCompound.setByteArray("inputOff", inOff);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETTINGS.equals(command)) {
            for (int i = 0 ; i < 6 ; i++) {
                char prefix = DUNSWE.charAt(i);
                inputModeOn[i] = args.get(prefix + "InOn").getBoolean();
                inputModeOff[i] = args.get(prefix + "InOff").getBoolean();
                rfOn[i] = args.get(prefix + "On").getInteger();
                rfOff[i] = args.get(prefix + "Off").getInteger();
            }
            markDirtyClient();
            return true;
        }
        return false;
    }

    private RelayEnergyStorage facingStorage[] = new RelayEnergyStorage[6];
    private RelayEnergyStorage nullStorage;

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            if (facing == null) {
                if (nullStorage == null) {
                    nullStorage = new RelayEnergyStorage(this, null);
                }
                return (T) nullStorage;
            } else {
                if (facingStorage[facing.ordinal()] == null) {
                    facingStorage[facing.ordinal()] = new RelayEnergyStorage(this, facing);
                }
                return (T) facingStorage[facing.ordinal()];
            }
        }
        return super.getCapability(capability, facing);
    }

}
