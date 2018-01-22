package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.endergen.EndergenicTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class EndergenicDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_endergenic_generator", EndergenicTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<EndergenicTileEntity> {
            public InternalManagedEnvironment(EndergenicTileEntity tile) {
                super(tile, "rftools_endergenic_generator");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getEnergyStored()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getMaxEnergyStored()};
            }

            @Callback(doc="function():number; Get the amount of RF last gained")
            public Object[] getLastRFGained(Context c, Arguments a) {
                return new Object[]{tile.getLastRfGained()};
            }

            @Callback(doc="function():number; Get the amount of RF last lost")
            public Object[] getLastRFLost(Context c, Arguments a) {
                return new Object[]{tile.getLastRfLost()};
            }

            @Callback(doc="function():number; Get last RF/t")
            public Object[] getLastRFPerTick(Context c, Arguments a) {
                return new Object[]{tile.getLastRfPerTick()};
            }

            @Callback(doc="function():number; Get the last amount of pearls lost")
            public Object[] getLastPearlsLost(Context c, Arguments a) {
                return new Object[]{tile.getLastPearlsLost()};
            }

            @Callback(doc="function():number; Get the last amount of pearls fired")
            public Object[] getLastPearlsFired(Context c, Arguments a) {
                return new Object[]{tile.getLastPearlsLaunched()};
            }

            @Callback(doc="function():number or nil; Get the position in the charge cycle the last pearl arrived at")
            public Object[] getLastPearlPosition(Context c, Arguments a) {
                if (tile.getLastPearlArrivedAt() == -2) {
                    return new Object[]{null};
                }
                return new Object[]{tile.getLastPearlArrivedAt()};
            }

            @Callback(doc="function():string; Get the reason the last pearl was lost")
            public Object[] getLastPearlLostReason(Context c, Arguments a) {
                return new Object[]{tile.getLastPearlsLostReason()};
            }

            @Callback(doc="function():int; Get how many times the generator was last charged")
            public Object[] getLastCharged(Context c, Arguments a) {
                return new Object[]{tile.getLastChargeCounter()};
            }

            @Callback(doc="function():string; Returns the current mode. One of \"Idle\", \"Holding\", \"Charging\"")
            public Object[] getMode(Context c, Arguments a) {
                String mode = "";
                switch (tile.getChargingMode()) {
                    case -1:
                        mode = "Holding";
                        break;
                    case 0:
                        mode = "Idle";
                        break;
                    default:
                        mode = "Charging";
                }
                return new Object[]{mode};
            }

            @Callback(doc="function():int; Returns how far into the charging cycle the generator currently is")
            public Object[] getChargeCycle(Context c, Arguments a) {
                int ret = tile.getChargingMode();
                if (ret < 0) {
                    ret = 0;
                }
                return new Object[]{ret};
            }

            @Callback(doc="function():bool; Start charging the generator")
            public Object[] startCharging(Context c, Arguments a) {
                if (tile.getChargingMode() == EndergenicTileEntity.CHARGE_IDLE) {
                    tile.startCharging();
                    return new Object[]{true};
                }
                return new Object[]{null};
            }

            @Callback(doc="function():bool; Fire the pearl the generator is currently holding")
            public Object[] firePearl(Context c, Arguments a) {
                if (tile.getChargingMode() == EndergenicTileEntity.CHARGE_HOLDING) {
                    tile.firePearl();
                    return new Object[]{true};
                }
                return new Object[]{null};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((EndergenicTileEntity) tile);
        }
    }
}
