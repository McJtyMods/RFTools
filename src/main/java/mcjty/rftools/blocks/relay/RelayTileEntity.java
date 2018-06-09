package mcjty.rftools.blocks.relay;

import java.util.Arrays;
import java.util.List;

import cofh.redstoneflux.api.IEnergyReceiver;
import mcjty.lib.api.MachineInformation;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.typed.Key;
import mcjty.lib.typed.Type;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.EnergyTools;
import mcjty.lib.varia.OrientationTools;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.Optional;

@Optional.Interface(iface="cofh.redstoneflux.api.IEnergyReceiver", modid="redstoneflux")
public class RelayTileEntity extends GenericEnergyStorageTileEntity implements ITickable, MachineInformation, IEnergyReceiver {

    public static final int MAXENERGY = 50000;
    public static final int RECEIVEPERTICK = 50000;

    public static final String CMD_SETTINGS = "relay.settings";

    public static final Key<Boolean> PARAM_INPUTON_D = new Key<>("inputOnD", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_U = new Key<>("inputOnU", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_N = new Key<>("inputOnN", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_S = new Key<>("inputOnS", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_W = new Key<>("inputOnW", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_E = new Key<>("inputOnE", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTON_I = new Key<>("inputOnI", Type.BOOLEAN);
    public static final List<Key<Boolean>> PARAM_INPUTON = Arrays.asList(PARAM_INPUTON_D, PARAM_INPUTON_U, PARAM_INPUTON_N, PARAM_INPUTON_S, PARAM_INPUTON_W, PARAM_INPUTON_E, PARAM_INPUTON_I);

    public static final Key<Boolean> PARAM_INPUTOFF_D = new Key<>("inputOffD", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_U = new Key<>("inputOffU", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_N = new Key<>("inputOffN", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_S = new Key<>("inputOffS", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_W = new Key<>("inputOffW", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_E = new Key<>("inputOffE", Type.BOOLEAN);
    public static final Key<Boolean> PARAM_INPUTOFF_I = new Key<>("inputOffI", Type.BOOLEAN);
    public static final List<Key<Boolean>> PARAM_INPUTOFF = Arrays.asList(PARAM_INPUTOFF_D, PARAM_INPUTOFF_U, PARAM_INPUTOFF_N, PARAM_INPUTOFF_S, PARAM_INPUTOFF_W, PARAM_INPUTOFF_E, PARAM_INPUTOFF_I);

    public static final Key<Integer> PARAM_RFON_D = new Key<>("rfOnD", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_U = new Key<>("rfOnU", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_N = new Key<>("rfOnN", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_S = new Key<>("rfOnS", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_W = new Key<>("rfOnW", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_E = new Key<>("rfOnE", Type.INTEGER);
    public static final Key<Integer> PARAM_RFON_I = new Key<>("rfOnI", Type.INTEGER);
    public static final List<Key<Integer>> PARAM_RFON = Arrays.asList(PARAM_RFON_D, PARAM_RFON_U, PARAM_RFON_N, PARAM_RFON_S, PARAM_RFON_W, PARAM_RFON_E, PARAM_RFON_I);

    public static final Key<Integer> PARAM_RFOFF_D = new Key<>("rfOffD", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_U = new Key<>("rfOffU", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_N = new Key<>("rfOffN", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_S = new Key<>("rfOffS", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_W = new Key<>("rfOffW", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_E = new Key<>("rfOffE", Type.INTEGER);
    public static final Key<Integer> PARAM_RFOFF_I = new Key<>("rfOffI", Type.INTEGER);
    public static final List<Key<Integer>> PARAM_RFOFF = Arrays.asList(PARAM_RFOFF_D, PARAM_RFOFF_U, PARAM_RFOFF_N, PARAM_RFOFF_S, PARAM_RFOFF_W, PARAM_RFOFF_E, PARAM_RFOFF_I);

    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    private static final String[] TAGS = new String[]{"rfpertick_out", "rfpertick_in"};
    private static final String[] TAG_DESCRIPTIONS = new String[] {
            "The current RF/t output given by this block (last 2 seconds)",
            "The current RF/t input received by this block (last 2 seconds)"};

    private boolean[] inputModeOn = new boolean[] { false, false, false, false, false, false, false };
    private boolean[] inputModeOff = new boolean[] { false, false, false, false, false, false, false };
    private int rfOn[] = new int[] { 1000, 1000, 1000, 1000, 1000, 1000, 1000 };
    private int rfOff[] = new int[] { 0, 0, 0, 0, 0, 0, 0 };
    public static final String DUNSWEI = "DUBFLRI";

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

        long energyStored = getStoredPower();
        if (energyStored <= 0) {
            return;
        }

        IBlockState state = getWorld().getBlockState(getPos());
        for (EnumFacing facing : EnumFacing.VALUES) { // there's no sensible way to send power out the null side, so just send it out the real sides
            int side = OrientationTools.reorient(facing, state).ordinal();
//            int side = facing.ordinal();
            if (rf[side] > 0 && !inputMode[side]) {
                TileEntity te = getWorld().getTileEntity(getPos().offset(facing));
                EnumFacing opposite = facing.getOpposite();
                if (EnergyTools.isEnergyTE(te, opposite)) {
                    int rfToGive = (int) Math.min(rf[side], energyStored);
                    int received = (int) EnergyTools.receiveEnergy(te, opposite, rfToGive);

                    powerOut += received;
                    energyStored -= storage.extractEnergy(received, false);
                    if (energyStored <= 0) {
                        return;
                    }
                }
            }
        }
    }

    // deliberately not @Optional, as other power APIs delegate to this method
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        boolean redstoneSignal = powerLevel > 0;

        boolean[] inputMode = redstoneSignal ? inputModeOn : inputModeOff;
        IBlockState state = getWorld().getBlockState(getPos());
        int side = from == null ? 6 : OrientationTools.reorient(from, state).ordinal();
        if (inputMode[side]) {
            int[] rf = redstoneSignal ? rfOn : rfOff;
            int actual = (int)storage.receiveEnergy(Math.min(maxReceive, rf[side]), simulate);
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
            for (int i = 0 ; i < 7 ; i++) {
                rfOn[i] = on;
                rfOff[i] = off;
                inputModeOn[i] = false;
                inputModeOff[i] = false;
            }
        } else {
            int[] on = tagCompound.getIntArray("on");
            int[] off = tagCompound.getIntArray("off");
            System.arraycopy(on, 0, rfOn, 0, Math.min(7, on.length));
            System.arraycopy(off, 0, rfOff, 0, Math.min(7, off.length));

            byte[] inOn = tagCompound.getByteArray("inputOn");
            byte[] inOff = tagCompound.getByteArray("inputOff");
            for (int i = 0 ; i < Math.min(7, inOn.length) ; i++) {
                inputModeOn[i] = inOn[i] > 0;
            }
            for (int i = 0 ; i < Math.min(7, inOff.length) ; i++) {
                inputModeOff[i] = inOff[i] > 0;
            }
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setIntArray("on", rfOn);
        tagCompound.setIntArray("off", rfOff);
        byte[] inOn = new byte[7];
        byte[] inOff = new byte[7];
        for (int i = 0 ; i < 7 ; i++) {
            inOn[i] = (byte) (inputModeOn[i] ? 1 : 0);
            inOff[i] = (byte) (inputModeOff[i] ? 1 : 0);
        }
        tagCompound.setByteArray("inputOn", inOn);
        tagCompound.setByteArray("inputOff", inOff);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_SETTINGS.equals(command)) {
            for (int i = 0 ; i < 7 ; i++) {
                char prefix = DUNSWEI.charAt(i);
                inputModeOn[i] = params.get(PARAM_INPUTON.get(i));
                inputModeOff[i] = params.get(PARAM_INPUTOFF.get(i));
                rfOn[i] = params.get(PARAM_RFON.get(i));
                rfOff[i] = params.get(PARAM_RFOFF.get(i));
            }
            markDirtyClient();
            return true;
        }
        return false;
    }

    private RelayEnergyStorage facingStorage[] = new RelayEnergyStorage[7];

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == EnergyTools.TESLA_CONSUMER) { // no need to test for CapabilityEnergy.ENERGY, as super already does this
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY || capability == EnergyTools.TESLA_CONSUMER) {
            int facingOrdinal = facing == null ? 6 : facing.ordinal();
            if (facingStorage[facingOrdinal] == null) {
                facingStorage[facingOrdinal] = new RelayEnergyStorage(this, facing);
            }
            return (T) facingStorage[facingOrdinal];
        }
        return super.getCapability(capability, facing);
    }

    @Override
    @Optional.Method(modid = "theoneprobe")
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, EntityPlayer player, World world, IBlockState blockState, IProbeHitData data) {
        super.addProbeInfo(mode, probeInfo, player, world, blockState, data);
        if (mode == ProbeMode.EXTENDED) {
            int rfPerTickIn = getLastRfPerTickIn();
            int rfPerTickOut = getLastRfPerTickOut();
            probeInfo.text(TextFormatting.GREEN + "In:  " + rfPerTickIn + "RF/t");
            probeInfo.text(TextFormatting.GREEN + "Out: " + rfPerTickOut + "RF/t");
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state) {
        boolean enabled = isPowered();
        return state.withProperty(ENABLED, enabled);
    }
}
