package mcjty.rftools.blocks.shaper;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;

import java.util.Map;

public class LocatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    public static final String CMD_MODE = "setMode";

    public LocatorTileEntity() {
        super(ScannerConfiguration.LOCATOR_MAXENERGY, ScannerConfiguration.LOCATOR_RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
        }
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
    }


    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_MODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        }
        return false;
    }

}
