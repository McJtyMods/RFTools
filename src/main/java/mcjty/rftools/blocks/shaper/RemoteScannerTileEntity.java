package mcjty.rftools.blocks.shaper;

import mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;
import mcjty.rftools.blocks.teleporter.TeleportDestination;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class RemoteScannerTileEntity extends ScannerTileEntity {


    @Override
    protected int getEnergyPerTick() {
        return ScannerConfiguration.REMOTE_SCANNER_PERTICK;
    }

    @Override
    protected BlockPos getScanPos() {
        TileEntity te = getWorld().getTileEntity(getPos().up());
        if (te instanceof MatterTransmitterTileEntity) {
            return getMachinePos((MatterTransmitterTileEntity)te);
        }
        te = getWorld().getTileEntity(getPos().up(2));
        if (te instanceof MatterTransmitterTileEntity) {
            return getMachinePos((MatterTransmitterTileEntity)te);
        }
        return null;
    }

    private BlockPos getMachinePos(MatterTransmitterTileEntity te) {
        TeleportDestination dest = te.getTeleportDestination();
        if (dest == null) {
            return null;
        }
        return dest.getCoordinate();
    }

    @Override
    public int getScanDimension() {
        TileEntity te = getWorld().getTileEntity(getPos().up());
        if (te instanceof MatterTransmitterTileEntity) {
            return getScanDimension((MatterTransmitterTileEntity)te);
        }
        te = getWorld().getTileEntity(getPos().up(2));
        if (te instanceof MatterTransmitterTileEntity) {
            return getScanDimension((MatterTransmitterTileEntity)te);
        }
        return 0;
    }

    private int getScanDimension(MatterTransmitterTileEntity te) {
        TeleportDestination dest = te.getTeleportDestination();
        if (dest == null) {
            return 0;
        }
        return dest.getDimension();
    }
}
