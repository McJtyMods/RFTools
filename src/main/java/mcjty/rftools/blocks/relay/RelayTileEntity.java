package mcjty.rftools.blocks.relay;

import cofh.api.energy.IEnergyConnection;
import mcjty.entity.GenericEnergyHandlerTileEntity;
import mcjty.varia.BlockTools;
import mcjty.network.Argument;
import mcjty.varia.EnergyTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class RelayTileEntity extends GenericEnergyHandlerTileEntity {

    public static final int MAXENERGY = 50000;
    public static final int RECEIVEPERTICK = 50000;

    public static final String CMD_SETTINGS = "settings";

    private boolean[] inputModeOn = new boolean[] { false, false, false, false, false, false };
    private boolean[] inputModeOff = new boolean[] { false, false, false, false, false, false };
    private int rfOn[] = new int[] { 1000, 1000, 1000, 1000, 1000, 1000 };
    private int rfOff[] = new int[] { 0, 0, 0, 0, 0, 0 };
    public static final String DUNSWE = "DUNSWE";

    public RelayTileEntity() {
        super(MAXENERGY, RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean redstoneSignal = BlockTools.getRedstoneSignal(meta);
        int[] rf = redstoneSignal ? rfOn : rfOff;
        boolean[] inputMode = redstoneSignal ? inputModeOn : inputModeOff;

        int energyStored = getEnergyStored(ForgeDirection.DOWN);
        if (energyStored <= 0) {
            return;
        }

        for (int i = 0 ; i < 6 ; i++) {
            int side = BlockTools.reorient(ForgeDirection.values()[i], meta).ordinal();
            if (rf[side] > 0 && !inputMode[side]) {
                ForgeDirection dir = ForgeDirection.getOrientation(i);
                TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                if (EnergyTools.isEnergyTE(te)) {
                    IEnergyConnection connection = (IEnergyConnection) te;
                    ForgeDirection opposite = dir.getOpposite();
                    if (connection.canConnectEnergy(opposite)) {
                        int rfToGive;
                        if (rf[side] <= energyStored) {
                            rfToGive = rf[side];
                        } else {
                            rfToGive = energyStored;
                        }

                        int received = EnergyTools.receiveEnergy(te, opposite, rfToGive);
                        energyStored -= extractEnergy(ForgeDirection.DOWN, received, false);
                        if (energyStored <= 0) {
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean redstoneSignal = BlockTools.getRedstoneSignal(meta);

        boolean[] inputMode = redstoneSignal ? inputModeOn : inputModeOff;
        int side = BlockTools.reorient(from, meta).ordinal();
        if (inputMode[side]) {
            int[] rf = redstoneSignal ? rfOn : rfOff;
            return super.receiveEnergy(from, Math.min(maxReceive, rf[side]), simulate);
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
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
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
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
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
            markDirty();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }
        return false;
    }
}
