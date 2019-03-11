package mcjty.rftools.compat.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.teleporter.MatterTransmitterTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class MatterTransmitterDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_matter_transmitter", MatterTransmitterTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<MatterTransmitterTileEntity> {
            public InternalManagedEnvironment(MatterTransmitterTileEntity tile) {
                super(tile, "rftools_matter_transmitter");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getStoredPower()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getCapacity()};
            }

            @Callback(doc="function():string; Get the current name")
            public Object[] getName(Context c, Arguments a) {
                return new Object[]{tile.getName()};
            }

            @Callback(doc="function(name:string); Set the current name")
            public Object[] setName(Context c, Arguments a) {
                String name = a.checkString(0);
                tile.setName(name);
                return new Object[]{};
            }

            @Callback(doc="function(hide:bool); Set whether the beam should be hidden or not")
            public Object[] hideBeam(Context c, Arguments a) {
                tile.setBeamHidden(a.checkBoolean(0));
                return new Object[]{};
            }

            @Callback(doc="function():bool; Returns whether the beam is hidden or not")
            public Object[] isBeamHidden(Context c, Arguments a) {
                return new Object[]{tile.isBeamHidden()};
            }

            @Callback(doc="function():bool; Returns whether a destination is dialed or not")
            public Object[] isDialed(Context c, Arguments a) {
                return new Object[]{tile.isDialed()};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((MatterTransmitterTileEntity) tile);
        }
    }
}
