package mcjty.rftools.blocks.dimletconstruction;

import mcjty.container.InventoryHelper;
import mcjty.entity.GenericEnergyReceiverTileEntity;
import mcjty.network.Argument;
import mcjty.network.PacketRequestIntegerFromServer;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.items.dimlets.*;
import mcjty.rftools.items.dimlets.types.DimletCraftingTools;
import mcjty.rftools.items.dimlets.types.IDimletType;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.varia.BlockTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class DimletWorkbenchTileEntity extends GenericEnergyReceiverTileEntity implements ISidedInventory {
    public static final String CMD_STARTEXTRACT = "startExtract";
    public static final String CMD_GETEXTRACTING = "getExtracting";
    public static final String CLIENTCMD_GETEXTRACTING = "getExtracting";
    public static final String CMD_SETAUTOEXTRACT = "setAutoExtract";

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimletWorkbenchContainer.factory, DimletWorkbenchContainer.SIZE_BUFFER + 9);

    private int extracting = 0;
    private DimletKey idToExtract = null;
    private int inhibitCrafting = 0;
    private boolean autoExtract = false;

    public int getExtracting() {
        return extracting;
    }

    public boolean isAutoExtract() {return autoExtract;
    }

    public DimletWorkbenchTileEntity() {
        super(DimletConstructionConfiguration.WORKBENCH_MAXENERGY, DimletConstructionConfiguration.WORKBENCH_RECEIVEPERTICK);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimletWorkbenchContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return index == DimletWorkbenchContainer.SLOT_INPUT;
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return index == DimletWorkbenchContainer.SLOT_OUTPUT;
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        ItemStack s = inventoryHelper.decrStackSize(index, amount);
        checkCrafting();
        return s;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
        if (index < DimletWorkbenchContainer.SLOT_BASE || index > DimletWorkbenchContainer.SLOT_ESSENCE) {
            return;
        }

        checkCrafting();
    }

    private void checkCrafting() {
        if (inhibitCrafting == 0) {
            if (!checkDimletCrafting()) {
                if (inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_OUTPUT) != null) {
                    inventoryHelper.setInventorySlotContents(0, DimletWorkbenchContainer.SLOT_OUTPUT, null);
                }
            }
        }
    }

    @Override
    public String getInventoryName() {
        return "Workbench Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return true;
    }

    @Override
    public void openInventory() {

    }

    @Override
    public void closeInventory() {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    protected void checkStateServer() {
        if (extracting > 0) {
            extracting--;
            if (extracting == 0) {
                if (!doExtract()) {
                    // We failed due to not enough power. Try again later.
                    extracting = 10;
                }
            }
            markDirty();
        } else if (autoExtract) {
            startExtracting();
        }
    }

    private boolean checkDimletCrafting() {
        ItemStack stackBase = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_BASE);
        if (stackBase == null) {
            return false;
        }
        ItemStack stackController = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_CONTROLLER);
        if (stackController == null) {
            return false;
        }
        ItemStack stackTypeController = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_TYPE_CONTROLLER);
        if (stackTypeController == null) {
            return false;
        }
        ItemStack stackMemory = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_MEMORY);
        if (stackMemory == null) {
            return false;
        }
        ItemStack stackEnergy = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_ENERGY);
        if (stackEnergy == null) {
            return false;
        }
        ItemStack stackEssence = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_ESSENCE);
        if (stackEssence == null) {
            return false;
        }

        DimletType type = DimletType.values()[stackTypeController.getItemDamage()];
        IDimletType itype = type.dimletType;
        DimletKey key = itype.attemptDimletCrafting(stackController, stackMemory, stackEnergy, stackEssence);
        if (key != null) {
            inventoryHelper.setInventorySlotContents(1, DimletWorkbenchContainer.SLOT_OUTPUT, KnownDimletConfiguration.makeKnownDimlet(key, worldObj));
            return true;
        }
        return false;
    }

    public void craftDimlet() {
        inhibitCrafting++;
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_BASE, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_CONTROLLER, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_TYPE_CONTROLLER, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_ENERGY, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_MEMORY, 1);
        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_ESSENCE, 1);
        inhibitCrafting--;
        checkCrafting();
    }

    private void startExtracting() {
        if (extracting > 0) {
            // Already extracting
            return;
        }
        ItemStack stack = inventoryHelper.getStackInSlot(DimletWorkbenchContainer.SLOT_INPUT);
        if (stack != null) {
            if (DimletSetup.knownDimlet.equals(stack.getItem())) {
                DimletKey key = KnownDimletConfiguration.getDimletKey(stack, worldObj);
                DimletEntry entry = KnownDimletConfiguration.getEntry(key);
                if (entry != null) {
                    if (!KnownDimletConfiguration.craftableDimlets.contains(key)) {
                        extracting = 64;
                        idToExtract = key;
                        inventoryHelper.decrStackSize(DimletWorkbenchContainer.SLOT_INPUT, 1);
                        markDirty();
                    }
                }
            }
        }
    }

    private boolean doExtract() {
        int rf = DimletConstructionConfiguration.rfExtractOperation;
        if (getEnergyStored(ForgeDirection.DOWN) < rf) {
            // Not enough energy.
            return false;
        }
        consumeEnergy(rf);

        float factor = getInfusedFactor();

        DimletEntry entry = KnownDimletConfiguration.getEntry(idToExtract);

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(DimletConstructionSetup.dimletBaseItem));
        }

        int rarity = entry.getRarity();

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(DimletConstructionSetup.dimletTypeControllerItem, 1, entry.getKey().getType().ordinal()));
        }

        int level = DimletCraftingTools.calculateItemLevelFromRarity(rarity);
        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(DimletConstructionSetup.dimletMemoryUnitItem, 1, level));
        } else {
            factor += 0.1f;     // If this failed we increase our chances a bit
        }

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(DimletConstructionSetup.dimletEnergyModuleItem, 1, level));
        } else {
            factor += 0.1f;     // If this failed we increase our chances a bit
        }

        if (extractSuccess(factor)) {
            mergeItemOrThrowInWorld(new ItemStack(DimletConstructionSetup.dimletControlCircuitItem, 1, rarity));
        }

        idToExtract = null;
        markDirty();

        return true;
    }

    private boolean extractSuccess(float factor) {
        return worldObj.rand.nextFloat() <= (0.61f + factor * 0.4f);
    }

    private void mergeItemOrThrowInWorld(ItemStack stack) {
        int notInserted = inventoryHelper.mergeItemStack(this, stack, DimletWorkbenchContainer.SLOT_BUFFER, DimletWorkbenchContainer.SLOT_BUFFER + DimletWorkbenchContainer.SIZE_BUFFER, null);
        if (notInserted > 0) {
            BlockTools.spawnItemStack(worldObj, xCoord, yCoord, zCoord, stack);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        extracting = tagCompound.getInteger("extracting");
        idToExtract = null;
        if (tagCompound.hasKey("extDkey")) {
            DimletType type = DimletType.getTypeByOpcode(tagCompound.getString("extKtype"));
            idToExtract = new DimletKey(type, tagCompound.getString("extDkey"));
        } else {
            if (tagCompound.hasKey("idToExtract")) {
                // Compatibility with old system.
                int id = tagCompound.getInteger("idToExtract");
                if (id != -1) {
                    DimletMapping mapping = DimletMapping.getDimletMapping(worldObj);
                    idToExtract = mapping.getKey(id);
                }
            }
        }
        autoExtract = tagCompound.getBoolean("autoExtract");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound);
        tagCompound.setInteger("extracting", extracting);
        if (idToExtract != null) {
            tagCompound.setString("extKtype", idToExtract.getType().dimletType.getOpcode());
            tagCompound.setString("extDkey", idToExtract.getName());
        }
        tagCompound.setBoolean("autoExtract", autoExtract);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_STARTEXTRACT.equals(command)) {
            startExtracting();
            return true;
        } else if (CMD_SETAUTOEXTRACT.equals(command)) {
            autoExtract = args.get("auto").getBoolean();
            markDirty();
            return true;
        }
        return false;
    }

    // Request the extracting amount from the server. This has to be called on the client side.
    public void requestExtractingFromServer() {
        RFToolsMessages.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETEXTRACTING,
                CLIENTCMD_GETEXTRACTING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETEXTRACTING.equals(command)) {
            return extracting;
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETEXTRACTING.equals(command)) {
            extracting = result;
            return true;
        }
        return false;
    }

}
