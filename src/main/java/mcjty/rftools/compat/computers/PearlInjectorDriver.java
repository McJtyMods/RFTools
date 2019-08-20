package mcjty.rftools.compat.computers;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.integration.computers.AbstractOCDriver;
import mcjty.rftools.blocks.endergen.PearlInjectorTileEntity;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
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
                    if (!stack.isEmpty() && Items.ENDER_PEARL.equals(stack.getItem()) && stack.getCount() > 0) {
                        ret += stack.getCount();
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
        public AbstractManagedEnvironment createEnvironment(World world, BlockPos pos, Direction side, TileEntity tile) {
            return new InternalManagedEnvironment((PearlInjectorTileEntity) tile);
        }
    }
}
