package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.InventoryHelper;
import com.mcjty.entity.GenericEnergyHandlerTileEntity;
import com.mcjty.rftools.blocks.BlockTools;
import com.mcjty.rftools.blocks.ModBlocks;
import com.mcjty.rftools.dimension.DimensionInformation;
import com.mcjty.rftools.dimension.RfToolsDimensionManager;
import com.mcjty.rftools.dimension.world.WorldGenerationTools;
import com.mcjty.rftools.items.dimlets.*;
import com.mcjty.rftools.items.dimlets.types.IDimletType;
import com.mcjty.rftools.network.Argument;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.rftools.network.PacketRequestIntegerFromServer;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Map;

public class DimensionEditorTileEntity extends GenericEnergyHandlerTileEntity implements ISidedInventory {

    public static final String CMD_GETEDITING = "getEditing";
    public static final String CLIENTCMD_GETEDITING = "getEditing";

    private static int editPercentage = 0;
    private int ticksLeft = -1;
    private int ticksCost = -1;
    private int rfPerTick = -1;

    private InventoryHelper inventoryHelper = new InventoryHelper(this, DimensionEditorContainer.factory, 2);

    public DimensionEditorTileEntity() {
        super(DimletConfiguration.EDITOR_MAXENERGY, DimletConfiguration.EDITOR_RECEIVEPERTICK);
    }

    @Override
    protected void checkStateServer() {
        ItemStack injectableItemStack = validateInjectableItemStack();
        if (injectableItemStack == null) {
            return;
        }

        ItemStack dimensionItemStack = validateDimensionItemStack();
        if (dimensionItemStack == null) {
            return;
        }

        if (ticksLeft == -1) {
            // We were not injecting. Start now.
            if (isMatterReceiver(injectableItemStack)) {
                ticksCost = DimletCosts.baseDimensionTickCost + 1000;
                ticksLeft = ticksCost;
                rfPerTick = DimletCosts.baseDimensionCreationCost + 200;
            } else {
                DimletKey key = KnownDimletConfiguration.getDimletKey(injectableItemStack, worldObj);
                DimletEntry dimletEntry = KnownDimletConfiguration.getEntry(key);
                ticksCost = DimletCosts.baseDimensionTickCost + dimletEntry.getTickCost();
                ticksLeft = ticksCost;
                rfPerTick = DimletCosts.baseDimensionCreationCost + dimletEntry.getRfCreateCost();
            }
        } else {
            int rf = getEnergyStored(ForgeDirection.DOWN);
            int rfpt = rfPerTick;
            rfpt = (int) (rfpt * (2.0f - getInfusedFactor()) / 2.0f);

            if (rf >= rfpt) {
                // Enough energy.
                extractEnergy(ForgeDirection.DOWN, rfpt, false);

                ticksLeft--;
                if (ticksLeft <= 0) {
                    RfToolsDimensionManager dimensionManager = RfToolsDimensionManager.getDimensionManager(worldObj);

                    ItemStack dimensionTab = validateDimensionItemStack();
                    NBTTagCompound tagCompound = dimensionTab.getTagCompound();
                    int id = tagCompound.getInteger("id");

                    injectableItemStack = validateInjectableItemStack();
                    if (isMatterReceiver(injectableItemStack)) {
                        World dimWorld = dimensionManager.getWorldForDimension(id);
                        int y = findGoodReceiverLocation(dimWorld);
                        if (y == -1) {
                            y = dimWorld.getHeight() / 2;
                        }
                        dimWorld.setBlock(8, y, 8, ModBlocks.matterReceiverBlock, 0, 2);
                        ModBlocks.matterReceiverBlock.onBlockPlaced(dimWorld, 8, y, 8, 0, 0, 0, 0, 0);
                        ModBlocks.matterReceiverBlock.onBlockPlacedBy(dimWorld, 8, y, 8, null, injectableItemStack);
                        dimWorld.setBlockToAir(8, y+1, 8);
                        dimWorld.setBlockToAir(8, y+2, 8);
                    } else {
                        DimletKey key = KnownDimletConfiguration.getDimletKey(injectableItemStack, worldObj);

                        DimensionInformation information = dimensionManager.getDimensionInformation(id);
                        information.injectDimlet(key);
                        dimensionManager.save(worldObj);
                    }

                    inventoryHelper.decrStackSize(DimensionEditorContainer.SLOT_INJECTINPUT, 1);

                    stopInjecting();
                }
            }
        }
        markDirty();

        setState();
    }

    private int findGoodReceiverLocation(World dimWorld) {
        int y = WorldGenerationTools.findSuitableEmptySpot(dimWorld, 8, 8);
        y++;
        return y;
    }

    private ItemStack validateInjectableItemStack() {
        ItemStack itemStack = inventoryHelper.getStacks()[DimensionEditorContainer.SLOT_INJECTINPUT];
        if (itemStack == null || itemStack.stackSize == 0) {
            stopInjecting();
            return null;
        }

        if (isMatterReceiver(itemStack)) {
            return itemStack;
        }

        DimletKey key = KnownDimletConfiguration.getDimletKey(itemStack, worldObj);
        DimletType type = key.getType();
        IDimletType itype = type.dimletType;
        if (itype.isInjectable()) {
            return itemStack;
        } else {
            return null;
        }
    }

    private boolean isMatterReceiver(ItemStack itemStack) {
        Block block = BlockTools.getBlock(itemStack);
        if (block == ModBlocks.matterReceiverBlock) {
            // We can inject matter receivers too.
            return true;
        }
        return false;
    }

    private ItemStack validateDimensionItemStack() {
        ItemStack itemStack = inventoryHelper.getStacks()[DimensionEditorContainer.SLOT_DIMENSIONTARGET];
        if (itemStack == null || itemStack.stackSize == 0) {
            stopInjecting();
            return null;
        }

        NBTTagCompound tagCompound = itemStack.getTagCompound();
        int id = tagCompound.getInteger("id");
        if (id == 0) {
            // Not a valid dimension.
            stopInjecting();
            return null;
        }

        return itemStack;
    }

    private void stopInjecting() {
        setState();
        ticksLeft = -1;
        ticksCost = -1;
        rfPerTick = -1;
        markDirty();
    }

    private void setState() {
        int state = 0;
        if (ticksLeft == 0) {
            state = 0;
        } else if (ticksLeft == -1) {
            state = 1;
        } else if (((ticksLeft >> 2) & 1) == 0) {
            state = 2;
        } else {
            state = 3;
        }
        int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        int newmeta = BlockTools.setState(metadata, state);
        if (newmeta != metadata) {
            worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, newmeta, 2);
        }
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        return DimletResearcherContainer.factory.getAccessibleSlots();
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, int side) {
        return DimletResearcherContainer.factory.isInputSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, int side) {
        return DimletResearcherContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getStacks().length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStacks()[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public String getInventoryName() {
        return "Editor Inventory";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 16;
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

    // Request the building percentage from the server. This has to be called on the client side.
    public void requestBuildingPercentage() {
        PacketHandler.INSTANCE.sendToServer(new PacketRequestIntegerFromServer(xCoord, yCoord, zCoord,
                CMD_GETEDITING,
                CLIENTCMD_GETEDITING));
    }

    @Override
    public Integer executeWithResultInteger(String command, Map<String, Argument> args) {
        Integer rc = super.executeWithResultInteger(command, args);
        if (rc != null) {
            return rc;
        }
        if (CMD_GETEDITING.equals(command)) {
            if (ticksLeft == -1) {
                return 0;
            } else {
                return (ticksCost - ticksLeft) * 100 / ticksCost;
            }
        }
        return null;
    }

    @Override
    public boolean execute(String command, Integer result) {
        boolean rc = super.execute(command, result);
        if (rc) {
            return true;
        }
        if (CLIENTCMD_GETEDITING.equals(command)) {
            editPercentage = result;
            return true;
        }
        return false;
    }

    public static int getEditPercentage() {
        return editPercentage;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
        ticksLeft = tagCompound.getInteger("ticksLeft");
        ticksCost = tagCompound.getInteger("ticksCost");
        rfPerTick = tagCompound.getInteger("rfPerTick");
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.getStacks()[i] = ItemStack.loadItemStackFromNBT(nbtTagCompound);
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
        tagCompound.setInteger("ticksLeft", ticksLeft);
        tagCompound.setInteger("ticksCost", ticksCost);
        tagCompound.setInteger("rfPerTick", rfPerTick);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getStacks().length ; i++) {
            ItemStack stack = inventoryHelper.getStacks()[i];
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }
}
