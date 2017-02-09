package mcjty.rftools.integration.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.ManagedEnvironment;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.endergen.PearlInjectorTileEntity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class PearlInjectorDriver {
    public static class OCDriver extends AbstractOCDriver {
        public OCDriver() {
            super("rftools_pearl_injector", PearlInjectorTileEntity.class);
        }

        public static class InternalManagedEnvironment extends AbstractOCDriver.InternalManagedEnvironment<PearlInjectorTileEntity> {
            public InternalManagedEnvironment(PearlInjectorTileEntity tile) {
                super(tile, "rftools_pearl_injector");
            }

            @Callback(doc="function(); Inject a pearl")
            public Object[] injectPearl(Context c, Arguments a) {
                tile.injectPearl();
                return new Object[]{};
            }

            @Callback(doc="function():number; Get the amount of pearls left")
            public Object[] getPearls(Context c, Arguments a) {
                int ret = 0;
                InventoryHelper inventoryHelper = tile.getInventoryHelper();
                for (int i = 0; i < inventoryHelper.getCount(); ++i) {
                    ItemStack stack = inventoryHelper.getStackInSlot(i);
                    if (ItemStackTools.isValid(stack) && Items.ENDER_PEARL.equals(stack.getItem()) && ItemStackTools.getStackSize(stack) > 0) {
                        ret += ItemStackTools.getStackSize(stack);
                    }
                }
                return new Object[]{ret};
            }

            @Override
            public int priority() {
                return 4;
            }
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side, TileEntity tile) {
            return new InternalManagedEnvironment((PearlInjectorTileEntity) tile);
        }
    }
}
