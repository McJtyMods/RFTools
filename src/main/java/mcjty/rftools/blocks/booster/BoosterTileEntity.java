package mcjty.rftools.blocks.booster;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.entity.GenericEnergyReceiverTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.tools.ItemStackTools;
import mcjty.lib.varia.RedstoneMode;
import mcjty.rftools.blocks.environmental.EnvModuleProvider;
import mcjty.rftools.blocks.environmental.modules.EnvironmentModule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.Map;

public class BoosterTileEntity extends GenericEnergyReceiverTileEntity implements DefaultSidedInventory, ITickable {

    public static final String CMD_RSMODE = "rsMode";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, BoosterContainer.factory, 1);

    private AxisAlignedBB beamBox = null;
    private int timeout = 0;

    private EnvironmentModule cachedModule;

    public BoosterTileEntity() {
        super(BoosterConfiguration.BOOSTER_MAXENERGY, BoosterConfiguration.BOOSTER_RECEIVEPERTICK);
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
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
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
                ItemStack stack = inventoryHelper.getStackInSlot(BoosterContainer.SLOT_MODULE);
                if (ItemStackTools.isValid(stack)) {
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
                int rf = getEnergyStored(EnumFacing.DOWN);
                int rfNeeded = (int) (cachedModule.getRfPerTick() * BoosterConfiguration.energyMultiplier);
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
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_RSMODE.equals(command)) {
            String m = args.get("rs").getString();
            setRSMode(RedstoneMode.getMode(m));
            return true;
        }
        return false;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return BoosterContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
        return BoosterContainer.factory.isOutputSlot(index);
    }

    @Override
    public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
        return BoosterContainer.factory.isInputSlot(index);
    }
}
