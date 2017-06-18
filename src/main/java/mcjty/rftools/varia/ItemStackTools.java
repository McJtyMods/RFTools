package mcjty.rftools.varia;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemStackTools {

    /**
     * Extract itemstack out of a slot and return a new stack.
     * Supports both IItemHandler as IInventory
     * @param tileEntity
     * @param slot
     * @param amount
     */
    @Nonnull
    public static ItemStack extractItem(@Nullable TileEntity tileEntity, int slot, int amount) {
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            return capability.extractItem(slot, amount, false);
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            return inventory.decrStackSize(slot, amount);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Get an item from an inventory
     * Supports both IItemHandler as IInventory
     * @param tileEntity
     * @param slot
     */
    @Nonnull
    public static ItemStack getStack(@Nullable TileEntity tileEntity, int slot) {
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            return capability.getStackInSlot(slot);
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            return inventory.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    /**
     * Set a stack in a specific slot. This will totally replace whatever was in the slot before
     * Supports both IItemHandler as IInventory. Does not check for failure
     * @param tileEntity
     * @param slot
     * @param stack
     */
    public static void setStack(@Nullable TileEntity tileEntity, int slot, @Nonnull ItemStack stack) {
        if (tileEntity != null && tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            IItemHandler capability = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            capability.extractItem(slot, 64, false);        // Clear slot
            capability.insertItem(slot, stack, false);
        } else if (tileEntity instanceof IInventory) {
            IInventory inventory = (IInventory) tileEntity;
            inventory.setInventorySlotContents(slot, stack);
        }
    }

    public static List<ItemStack> getOres(String name) {
        return OreDictionary.getOres(name);
    }

    public static List<ItemStack> getOres(String name, boolean alwaysCreateEntry) {
        return OreDictionary.getOres(name, alwaysCreateEntry);
    }
}
