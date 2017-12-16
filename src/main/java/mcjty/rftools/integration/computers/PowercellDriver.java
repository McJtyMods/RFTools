package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.powercell.PowerCellTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class PowercellDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_powercell", PowerCellTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<PowerCellTileEntity> {
            public InternalManagedEnvironment(PowerCellTileEntity tile) {
                super(tile, "rftools_powercell");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getEnergyStored(null)};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getMaxEnergyStored(null)};
            }

            @Callback(doc="function():number; Get how much energy was extracted in total")
            public Object[] getTotalExtracted(Context c, Arguments a) {
                return new Object[]{tile.getTotalExtracted()};
            }

            @Callback(doc="function():number; Get how much energy was inserted in total")
            public Object[] getTotalInserted(Context c, Arguments a) {
                return new Object[]{tile.getTotalInserted()};
            }

            @Callback(doc="function(); Reset the total extracted energy stat")
            public Object[] resetTotalExtracted(Context c, Arguments a) {
                tile.resetTotalExtracted();
                return new Object[]{};
            }

            @Callback(doc="function(); Reset the total extracted energy stat")
            public Object[] resetTotalInserted(Context c, Arguments a) {
                tile.resetTotalInserted();
                return new Object[]{};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((PowerCellTileEntity) tile);
        }
    }
}
