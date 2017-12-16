package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.teleporter.MatterReceiverTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class MatterReceiverDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_matter_receiver", MatterReceiverTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<MatterReceiverTileEntity> {
            public InternalManagedEnvironment(MatterReceiverTileEntity tile) {
                super(tile, "rftools_matter_receiver");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getEnergyStored()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getMaxEnergyStored()};
            }

            @Callback(doc = "function():string; Get the current name")
            public Object[] getName(Context c, Arguments a) {
                return new Object[]{tile.getName()};
            }

            @Callback(doc = "function(name:string); Set the current name")
            public Object[] setName(Context c, Arguments a) {
                String name = a.checkString(0);
                tile.setName(name);
                return new Object[]{};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((MatterReceiverTileEntity) tile);
        }
    }
}
