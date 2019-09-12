package mcjty.rftools.blocks.booster;

import mcjty.lib.api.infusable.CapabilityInfusable;
import mcjty.lib.api.infusable.DefaultInfusable;
import mcjty.lib.api.infusable.IInfusable;
import mcjty.lib.container.*;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.tileentity.GenericEnergyStorage;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.ModuleSupport;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static mcjty.rftools.blocks.booster.BoosterSetup.TYPE_BOOSTER;

public class BoosterTileEntity extends GenericTileEntity implements ITickableTileEntity {

    public static final String CMD_RSMODE = "booster.setRsMode";

    public static final int SLOT_MODULE = 0;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), ContainerFactory.CONTAINER_CONTAINER, 0, 7, 8, 1, 18, 1, 18);
            layoutPlayerInventorySlots(27, 102);
        }
    };

    private LazyOptional<NoDirectionItemHander> itemHandler = LazyOptional.of(this::createItemHandler);
    private LazyOptional<GenericEnergyStorage> energyHandler = LazyOptional.of(() -> new GenericEnergyStorage(this, true,
            BoosterConfiguration.BOOSTER_MAXENERGY.get(), BoosterConfiguration.BOOSTER_RECEIVEPERTICK.get()));
    private LazyOptional<IInfusable> infusableHandler = LazyOptional.of(() -> new DefaultInfusable(BoosterTileEntity.this));

    static final ModuleSupport MODULE_SUPPORT = new ModuleSupport(SLOT_MODULE) {
        @Override
        public boolean isModule(ItemStack itemStack) {
            return itemStack.getItem() instanceof EnvModuleProvider;
        }
    };

    private InventoryHelper inventoryHelper = new InventoryHelper(this, CONTAINER_FACTORY, 1);

    private AxisAlignedBB beamBox = null;
    private int timeout = 0;

    private EnvironmentModule cachedModule;

    public BoosterTileEntity() {
        super(TYPE_BOOSTER);
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    // @todo 1.14 loot tables
    @Override
    public void read(CompoundNBT tagCompound) {
        super.read(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    // @todo 1.14 loot tables
    @Override
    public CompoundNBT write(CompoundNBT tagCompound) {
        super.write(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        return tagCompound;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            if (timeout > 0) {
                timeout--;
                markDirty();
                return;
            }
            if (cachedModule == null) {
                ItemStack stack = inventoryHelper.getStackInSlot(SLOT_MODULE);
                if (!stack.isEmpty()) {
                    if (stack.getItem() instanceof EnvModuleProvider) {
                        EnvModuleProvider provider = (EnvModuleProvider) stack.getItem();
                        Class<? extends EnvironmentModule> clazz = provider.getServerEnvironmentModule();
                        try {
                            cachedModule = clazz.newInstance();
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            if (cachedModule != null) {
                energyHandler.ifPresent(h -> {
                    long rf = h.getEnergyStored();
                    float factor = infusableHandler.map(inf -> inf.getInfusedFactor()).orElse(1.0f);
                    int rfNeeded = (int) (cachedModule.getRfPerTick() * BoosterConfiguration.energyMultiplier.get());
                    rfNeeded = (int) (rfNeeded * (3.0f - factor) / 3.0f);
                    for (LivingEntity entity : searchEntities()) {
                        if (rfNeeded <= rf) {
                            if (cachedModule.apply(getWorld(), getPos(), entity, 40)) {
                                // Consume energy
                                h.consumeEnergy(rfNeeded);
                                rf -= rfNeeded;
                            }
                        }
                    }
                    timeout = 10;
                    markDirty();
                });
            }
        }
    }

    // @todo 1.14 (cached module!!!)
//    @Override
//    public void setInventorySlotContents(int index, ItemStack stack) {
//        cachedModule = null;
//        getInventoryHelper().setInventorySlotContents(this.getInventoryStackLimit(), index, stack);
//    }

    private NoDirectionItemHander createItemHandler() {
        return new NoDirectionItemHander(BoosterTileEntity.this, CONTAINER_FACTORY, 1) {
            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof EnvModuleProvider;
            }

            @Override
            public boolean isItemInsertable(int slot, @Nonnull ItemStack stack) {
                return stack.getItem() instanceof EnvModuleProvider;
            }
        };
    }

    private List<LivingEntity> searchEntities() {
        if (beamBox == null) {
            int xCoord = getPos().getX();
            int yCoord = getPos().getY();
            int zCoord = getPos().getZ();
            beamBox = new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
        }

        return getWorld().getEntitiesWithinAABB(LivingEntity.class, beamBox);
    }

    @Override
    public boolean execute(PlayerEntity playerMP, String command, TypedMap params) {
        boolean rc = super.execute(playerMP, command, params);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            setRSMode(RedstoneMode.values()[params.get(ImageChoiceLabel.PARAM_CHOICE_IDX)]);
            return true;
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction facing) {
        if (cap == CapabilityEnergy.ENERGY) {
            return energyHandler.cast();
        }
//        if (cap == CapabilityContainerProvider.CONTAINER_PROVIDER_CAPABILITY) {
//            return screenHandler.cast();
//        }
        if (cap == CapabilityInfusable.INFUSABLE_CAPABILITY) {
            return infusableHandler.cast();
        }
        return super.getCapability(cap, facing);
    }
}
