package mcjty.rftools.items.storage;

import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.InventoryHelper;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftools.ClientCommandHandler;
import mcjty.rftools.blocks.storage.*;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.servernet.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;

public class StorageTools {


    public static void compact(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        CompoundNBT tagCompound = heldItem.getTag();
        if (tagCompound == null) {
            return;
        }
        if (!tagCompound.hasKey("childDamage")) {
            // Should not be possible. Just for safety.
            return;
        }

        int moduleDamage = tagCompound.getInt("childDamage");
        if (moduleDamage == StorageModuleItem.STORAGE_REMOTE) {
            int id = tagCompound.getInt("id");
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

    public static void clearGrid(PlayerEntity player) {
        ItemStack mainhand = player.getHeldItemMainhand();
        if (!mainhand.isEmpty() && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
            if (player.openContainer instanceof ModularStorageItemContainer) {
                ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTag().removeTag("grid");
            } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTag().removeTag("grid");
            } else if (player.openContainer instanceof StorageScannerContainer) {
                StorageScannerContainer storageItemContainer = (StorageScannerContainer) player.openContainer;
                storageItemContainer.clearGrid();
                mainhand.getTag().removeTag("grid");
            }
        }
    }

    public static void cycleStorage(PlayerEntity player) {
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);
        if (heldItem.isEmpty()) {
            return;
        }
        CompoundNBT tagCompound = heldItem.getTag();
        if (tagCompound == null) {
            return;
        }
        int id = tagCompound.getInt("id");
        RemoteStorageTileEntity remoteStorage = RemoteStorageIdRegistry.getRemoteStorage(player.getEntityWorld(), id);
        if (remoteStorage != null) {
            id = remoteStorage.cycle(id);
            tagCompound.putInt("id", id);
            int si = remoteStorage.findRemoteIndex(id);
            if (si != -1) {
                int maxStacks = remoteStorage.getMaxStacks(si);
                tagCompound.putInt("maxSize", maxStacks);
            }
            remoteStorage.markDirty();
        } else {
            Logging.message(player, TextFormatting.YELLOW + "Remote storage it not available (out of power or out of reach)!");
        }
    }

    public static void returnStorageInfo(PlayerEntity player, int dimension, BlockPos pos) {
        ServerWorld world = DimensionManager.getWorld(dimension);
        int cnt = -1;
        String nameModule = "";
        if (world != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity modularStorageTileEntity = (ModularStorageTileEntity) te;
                cnt = modularStorageTileEntity.getNumStacks();
                ItemStack storageModule = modularStorageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                if (!storageModule.isEmpty() && storageModule.getTag().hasKey("display")) {
                    nameModule = storageModule.getDisplayName();
                }
            }
        }
        RFToolsMessages.sendToClient(player, ClientCommandHandler.CMD_RETURN_STORAGE_INFO,
                TypedMap.builder().put(ClientCommandHandler.PARAM_COUNTER, cnt).put(ClientCommandHandler.PARAM_NAME, nameModule));
    }
}
