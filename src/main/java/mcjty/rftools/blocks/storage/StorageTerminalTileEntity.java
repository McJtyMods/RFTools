package mcjty.rftools.blocks.storage;

import mcjty.lib.container.DefaultSidedInventory;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.blocks.logic.generic.LogicTileEntity;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import mcjty.rftools.craftinggrid.*;
import mcjty.rftools.jei.JEIRecipeAcceptor;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

import java.util.List;

public class StorageTerminalTileEntity extends LogicTileEntity implements DefaultSidedInventory, CraftingGridProvider, JEIRecipeAcceptor {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, StorageTerminalContainer.factory, 1);
    private CraftingGrid craftingGrid = new CraftingGrid();

    @Override
    public void setRecipe(int index, ItemStack[] stacks) {
        craftingGrid.setRecipe(index, stacks);
        markDirty();
    }

    @Override
    public void storeRecipe(int index) {
        craftingGrid.storeRecipe(index);
        markDirty();
    }

    @Override
    public CraftingGrid getCraftingGrid() {
        return craftingGrid;
    }

    @Override
    public void markInventoryDirty() {
        markDirty();
    }

    @Override
    public int[] craft(EntityPlayerMP player, int n, boolean test) {
        ItemStack module = inventoryHelper.getStackInSlot(StorageTerminalContainer.SLOT_MODULE);
        if (ItemStackTools.isEmpty(module)) {
            // No module. Should not be possible
            return new int[0];
        }

        int dimension = RFToolsTools.getDimensionFromModule(module);
        BlockPos scannerPos = RFToolsTools.getPositionFromModule(module);
        WorldServer world = DimensionManager.getWorld(dimension);

        StorageScannerTileEntity scannerTE = null;
        if (RFToolsTools.chunkLoaded(world, scannerPos)) {
            TileEntity te = world.getTileEntity(scannerPos);
            if (te instanceof StorageScannerTileEntity) {
                scannerTE = (StorageScannerTileEntity) te;
            }
        }

        if (scannerTE == null) {
            // Scanner is not chunkloaded or not valid. Only use player inventory
            IItemSource itemSource = new InventoriesItemSource().add(player.inventory, 0);
            if (test) {
                return StorageCraftingTools.testCraftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
            } else {
                StorageCraftingTools.craftItems(player, n, craftingGrid.getActiveRecipe(), itemSource);
                return null;
            }
        } else {
            // Delegate crafting to scanner
            return scannerTE.craft(player, n, test, craftingGrid.getActiveRecipe());
        }
    }

    @Override
    public void setGridContents(List<ItemStack> stacks) {
        for (int i = 0 ; i < stacks.size() ; i++) {
            craftingGrid.getCraftingGridInventory().setInventorySlotContents(i, stacks.get(i));
        }
        markDirty();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet) {
        boolean module = inventoryHelper.getStackInSlot(StorageTerminalContainer.SLOT_MODULE) != null;

        super.onDataPacket(net, packet);

        if (getWorld().isRemote) {
            // If needed send a render update.
            boolean newmodule = inventoryHelper.getStackInSlot(StorageTerminalContainer.SLOT_MODULE) != null;
            if (newmodule != module) {
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(this.getInventoryStackLimit(), index, stack);
        if (!getWorld().isRemote) {
            // Make sure we update client-side
            markDirtyClient();
        } else {
            getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound, inventoryHelper);
        craftingGrid.readFromNBT(tagCompound.getCompoundTag("grid"));
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        writeBufferToNBT(tagCompound, inventoryHelper);
        tagCompound.setTag("grid", craftingGrid.writeToNBT());
    }

    @Override
    protected boolean needsCustomInvWrapper() {
        return true;
    }

    @Override
    public InventoryHelper getInventoryHelper() {
        return inventoryHelper;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == StorageTerminalContainer.SLOT_MODULE && stack.getItem() != ScreenSetup.storageControlModuleItem) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isUsable(EntityPlayer player) {
        return canPlayerAccess(player);
    }
}
