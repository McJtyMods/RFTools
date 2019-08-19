package mcjty.rftools.blocks.booster;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tileentity.GenericEnergyReceiverTileEntity;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.varia.ModuleSupport;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import mcjty.lib.typed.TypedMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public class BoosterTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_RSMODE = "booster.setRsMode";

    public static final String CONTAINER_INVENTORY = "container";
    public static final int SLOT_MODULE = 0;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory(new ResourceLocation(RFTools.MODID, "gui/booster.gui"));

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
        super(BoosterConfiguration.BOOSTER_MAXENERGY.get(), BoosterConfiguration.BOOSTER_RECEIVEPERTICK.get());
    }

    @Override
    protected boolean needsRedstoneMode() {
        return true;
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public void readRestorableFromNBT(CompoundNBT tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(CompoundNBT tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void update() {
        if (!getWorld().isRemote) {
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
                long rf = getStoredPower();
                int rfNeeded = (int) (cachedModule.getRfPerTick() * BoosterConfiguration.energyMultiplier.get());
                rfNeeded = (int) (rfNeeded * (3.0f - getInfusedFactor()) / 3.0f);
                for (EntityLivingBase entity : searchEntities()) {
                    if (rfNeeded <= rf) {
                        if (cachedModule.apply(getWorld(), getPos(), entity, 40)) {
                            // Consume energy
                            consumeEnergy(rfNeeded);
                            rf -= rfNeeded;
                        }
                    }
                }
                timeout = 10;
                markDirty();
            }
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        cachedModule = null;
        getInventoryHelper().setInventorySlotContents(this.getInventoryStackLimit(), index, stack);
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        cachedModule = null;
        return getInventoryHelper().decrStackSize(index, count);
    }

    private List<EntityLivingBase> searchEntities() {
        if (beamBox == null) {
            int xCoord = getPos().getX();
            int yCoord = getPos().getY();
            int zCoord = getPos().getZ();
            beamBox = new AxisAlignedBB(xCoord, yCoord + 1, zCoord, xCoord + 1, yCoord + 3, zCoord + 1);
        }

        return getWorld().getEntitiesWithinAABB(EntityLivingBase.class, beamBox);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, TypedMap params) {
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

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        return canPlayerAccess(player);
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return CONTAINER_FACTORY.getAccessibleSlots();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
        return CONTAINER_FACTORY.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, Direction direction) {
        return CONTAINER_FACTORY.isInputSlot(index);
    }
}
