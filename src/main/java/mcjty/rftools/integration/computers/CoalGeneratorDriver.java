package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.generator.CoalGeneratorTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class CoalGeneratorDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_coal_generator", CoalGeneratorTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<CoalGeneratorTileEntity> {
            public InternalManagedEnvironment(CoalGeneratorTileEntity tile) {
                super(tile, "rftools_coal_generator");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getEnergyStored()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getMaxEnergyStored()};
            }
            
            @Callback(doc="function():string; Get the current redstone mode. One of \"Ignored\", \"Off\" and \"On\"")
            public Object[] getRedstoneMode(Context c, Arguments a) {
                return new Object[]{tile.getRSMode().getDescription()};
            }

            @Callback(doc="function(string); Set the redstone mode. One of \"Ignored\", \"Off\" and \"On\"")
            public Object[] setRedstoneMode(Context c, Arguments a) {
                String newVal = a.checkString(0);
                RedstoneMode rsMode = RedstoneMode.getMode(newVal);
                if (rsMode != null) {
                    tile.setRSMode(rsMode);
                    tile.markDirtyClient();
                    return new Object[]{true};
                } else {
                    return new Object[]{false, "Not a valid redstone mode. Needs to be one of \"Ignored\", \"Off\" and \"On\""};
                }
            }

            @Callback(doc="function():int; Gets the current RF/t")
            public Object[] getRFPerTick(Context c, Arguments a) {
                if (tile.isWorking()) {
                    return new Object[]{tile.getRfPerTick()};
                } else {
                    return new Object[]{0};
                }
            }

            @Callback(doc="function():bool; Returns whether the generator is currently running")
            public Object[] isRunning(Context c, Arguments a) {
                return new Object[]{tile.isWorking()};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((CoalGeneratorTileEntity) tile);
        }
    }
}
