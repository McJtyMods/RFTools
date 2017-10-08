package mcjty.rftools.blocks.shaper;

import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.shapes.BeaconType;
import mcjty.rftools.shapes.ScanDataManager;
import mcjty.rftools.shapes.ScanExtraData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;

public class LocatorTileEntity extends GenericEnergyReceiverTileEntity implements ITickable {

    public static final String CMD_MODE = "setMode";

    private static int counter = 0;

    public LocatorTileEntity() {
        super(ScannerConfiguration.LOCATOR_MAXENERGY, ScannerConfiguration.LOCATOR_RECEIVEPERTICK);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
            counter--;
            markDirtyQuick();
            if (counter <= 0) {
                counter = ScannerConfiguration.ticksPerLocatorScan;
                int energy = getEnergyPerScan();
                if (getEnergyStored() < energy) {
                    // Do nothing
                    return;
                }
                ScannerTileEntity scanner = getScanner();
                if (scanner == null || scanner.getDataDim() == null) {
                    // No scanner, do nothing
                    return;
                }
                // @todo energy consumption per area?
                consumeEnergy(energy);
                BlockPos dim = scanner.getDataDim();
                BlockPos start = scanner.getFirstCorner();
                AxisAlignedBB bb = new AxisAlignedBB(start, start.add(dim));

                List<Entity> entities = getWorld().getEntitiesWithinAABB(EntityLivingBase.class, bb);
                int scanId = scanner.getScanId();
                ScanExtraData extraData = ScanDataManager.getScans().getExtraData(scanId);
                extraData.touch();
                extraData.clear();

                BlockPos center = scanner.getScanCenter();
                for (Entity entity : entities) {
                    if (entity instanceof EntityAnimal) {
                        extraData.addBeacon(entity.getPosition().subtract(center), BeaconType.BEACON_PASSIVE);
                    } else if (entity instanceof EntityPlayer) {
                        extraData.addBeacon(entity.getPosition().subtract(center), BeaconType.BEACON_PLAYER);
                    } else {
                        extraData.addBeacon(entity.getPosition().subtract(center), BeaconType.BEACON_HOSTILE);
                    }
                }
            }
        }
    }

    public int getEnergyPerScan() {
        // @todo
        return ScannerConfiguration.LOCATOR_PERSCAN + ScannerConfiguration.LOCATOR_PERSCAN_HOSTILE + ScannerConfiguration.LOCATOR_PERSCAN_PASSIVE + ScannerConfiguration.LOCATOR_PERSCAN_PLAYER;
    }

    private ScannerTileEntity getScanner() {
        TileEntity te = getWorld().getTileEntity(getPos().down());
        if (te instanceof ScannerTileEntity) {
            return (ScannerTileEntity) te;
        }
        return null;
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }


    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
    }


    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
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
