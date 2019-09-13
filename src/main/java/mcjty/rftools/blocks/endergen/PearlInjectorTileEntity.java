package mcjty.rftools.blocks.endergen;

import mcjty.lib.api.container.CapabilityContainerProvider;
import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.NoDirectionItemHander;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.varia.OrientationTools;
import mcjty.rftools.TickOrderHandler;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static mcjty.rftools.blocks.endergen.EndergenicSetup.TYPE_PEARL_INJECTOR;

public class PearlInjectorTileEntity extends GenericTileEntity implements ITickableTileEntity, TickOrderHandler.ICheckStateServer {

    public static final int BUFFER_SIZE = (9*2);
    public static final int SLOT_BUFFER = 0;
    public static final int SLOT_PLAYERINV = SLOT_BUFFER + BUFFER_SIZE;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(Items.ENDER_PEARL)), ContainerFactory.CONTAINER_CONTAINER, SLOT_BUFFER, 10, 25, 9, 18, 2, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };
    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);

    // For pulse detection.
    private boolean prevIn = false;

    public PearlInjectorTileEntity() {
        super(TYPE_PEARL_INJECTOR);
    }

    public EndergenicTileEntity findEndergenicTileEntity() {
        BlockState state = world.getBlockState(getPos());
        Direction k = OrientationTools.getOrientation(state);
        EndergenicTileEntity te = getEndergenicGeneratorAt(k.getOpposite());
        if (te != null) {
            return te;
        }
        return getEndergenicGeneratorAt(Direction.UP);
    }

    private EndergenicTileEntity getEndergenicGeneratorAt(Direction k) {
        BlockPos o = getPos().offset(k);
        TileEntity te = world.getTileEntity(o);
        if (te instanceof EndergenicTileEntity) {
            return (EndergenicTileEntity) te;
        }
        return null;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            TickOrderHandler.queuePearlInjector(this);
        }
    }

    @Override
    public void checkStateServer() {
        boolean pulse = (powerLevel > 0) && !prevIn;
        if (prevIn == powerLevel > 0) {
            return;
        }
        prevIn = powerLevel > 0;

        if (pulse) {
            injectPearl();
        }
        markDirty();
    }

    @Override
    public int getDimension() {
        return world.getDimension().getType().getId();
    }

    private boolean takePearl() {
        return itemHandler.map(h -> {
            for (int i = 0 ; i < h.getSlots() ; i++) {
                ItemStack stack = h.getStackInSlot(i);
                if (!stack.isEmpty() && Items.ENDER_PEARL.equals(stack.getItem()) && stack.getCount() > 0) {
                    h.extractItem(i, 1, false);
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    public void injectPearl() {
        EndergenicTileEntity endergen = findEndergenicTileEntity();
        if (endergen != null) {
            if (!takePearl()) {
                // No pearls in the inventory.
                return;
            }
            int mode = endergen.getChargingMode();
            // If the endergenic is already holding a pearl then this one is lost.
            if (mode != EndergenicTileEntity.CHARGE_HOLDING) {
                // It can accept a pearl.
                endergen.firePearlFromInjector();
            }
        }
    }

    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        tagCompound.putBoolean("prevIn", prevIn);
        return tagCompound;
    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(PearlInjectorTileEntity.this, CONTAINER_FACTORY, BUFFER_SIZE) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() == Items.ENDER_PEARL;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        return super.getCapability(cap, facing);
    }
}
