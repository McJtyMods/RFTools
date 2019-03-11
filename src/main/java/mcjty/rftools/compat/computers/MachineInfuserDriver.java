package mcjty.rftools.compat.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.lib.base.GeneralConfig;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.infuser.MachineInfuserTileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MachineInfuserDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_infuser", MachineInfuserTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<MachineInfuserTileEntity> {
            public InternalManagedEnvironment(MachineInfuserTileEntity tile) {
                super(tile, "rftools_infuser");
            }

            @Callback(doc = "function():number; Get the currently stored energy")
            public Object[] getEnergy(Context c, Arguments a) {
                return new Object[]{tile.getStoredPower()};
            }

            @Callback(doc = "function():number; Get the maximum stored energy")
            public Object[] getMaxEnergy(Context c, Arguments a) {
                return new Object[]{tile.getCapacity()};
            }

            @Callback(doc = "function():number; Get the maximum number of dimensional shards a machine can be infused with")
            public Object[] getMaxShards(Context c, Arguments a) {
                return new Object[]{GeneralConfig.maxInfuse};
            }

            @Callback(doc = "function():number; Get the current number of dimensional shards the machine is infused with")
            public Object[] getShards(Context c, Arguments a) {
                ItemStack item = tile.getInventoryHelper().getStackInSlot(1);
                NBTTagCompound tag = MachineInfuserTileEntity.getTagCompound(item);
                if (tag == null || !tag.hasKey("infused")) {
                    return new Object[]{0};
                }
                return new Object[]{tag.getInteger("infused")};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((MachineInfuserTileEntity) tile);
        }
    }
}
