package mcjty.rftools.items.storage;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.varia.Logging;
import mcjty.rftools.blocks.storage.*;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;

public class StorageTools {


    public static void compact(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return;
        }
        if (!tagCompound.hasKey("childDamage")) {
            // Should not be possible. Just for safety.
            return;
        }

        int moduleDamage = tagCompound.getInteger("childDamage");
        if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
            int id = tagCompound.getInteger("id");
            RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(player.getEntityWorld(), id);
            if (remoteStorage != null) {
                remoteStorage.compact(id);
                remoteStorage.markDirty();
            } else {
                Logging.message(player, TextFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
            }
        } else {
            GenericContainer genericContainer = (GenericContainer) player.openContainer;
            IInventory inventory = genericContainer.getInventory(ModularStorageItemContainer.CONTAINER_INVENTORY);
            ModularStorageItemInventory modularStorageItemInventory = (ModularStorageItemInventory) inventory;
            InventoryHelper.compactStacks(modularStorageItemInventory.getStacks(), 0, inventory.getSizeInventory());
            modularStorageItemInventory.markDirty();
        }
    }

    public static void clearGrid(EntityPlayer player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
            if (player.openContainer instanceof ModularStorageItemContainer) {
                ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTagCompound().removeTag("grid");
            } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTagCompound().removeTag("grid");
            } else if (player.openContainer instanceof StorageScannerContainer) {
                StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTagCompound().removeTag("grid");
            }
        }
    }

    public static void cycleStorage(EntityPlayer player) {
        ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        NBTTagCompound tagCompound = heldItem.getTagCompound();
        if (tagCompound == null) {
            return;
        }
        int id = tagCompound.getInteger("id");
        RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(player.getEntityWorld(), id);
        if (remoteStorage != null) {
            id = remoteStorage.cycle(id);
            tagCompound.setInteger("id", id);
            int si = remoteStorage.findRemoteIndex(id);
            if (si != -1) {
                int maxStacks = remoteStorage.getMaxStacks(si);
                tagCompound.setInteger("maxSize", maxStacks);
            }
            remoteStorage.markDirty();
        } else {
            Logging.message(player, TextFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
        }
    }
}
