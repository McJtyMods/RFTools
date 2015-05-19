package mcjty.rftools.blocks.storage;

import mcjty.container.*;
import mcjty.rftools.items.storage.StorageTypeItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ModularStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int MAXSIZE_STORAGE = 300;

    private int id;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, 0, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
        }
    };

    public ModularStorageItemContainer(EntityPlayer player) {
        super(factory);
        ItemStack stack = player.getHeldItem();
        // We assume the item is right here
        id = stack.getTagCompound().getInteger("id");
        RemoteStorageTileEntity remoteStorageTileEntity = RemoteStorageIdRegistry.getRemoteStorage(player.worldObj, id);
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si != -1) {
                stack.getTagCompound().setInteger("maxSize", remoteStorageTileEntity.getMaxStacks(si));
            }
        }

        addInventory(CONTAINER_INVENTORY, new ModularStorageItemInventory(player, id));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());

//                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
//                    @Override
//                    public boolean getHasStack() {
//                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + 2)) {
//                            return false;
//                        }
//                        return super.getHasStack();
//                    }
//
//                    @Override
//                    public ItemStack getStack() {
//                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + 2)) {
//                            return null;
//                        }
//                        return super.getStack();
//                    }
//
//                    @Override
//                    public boolean canTakeStack(EntityPlayer player) {
//                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + 2)) {
//                            return false;
//                        }
//                        return super.canTakeStack(player);
//                    }
//
//                    @Override
//                    public boolean isItemValid(ItemStack stack) {
//                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + 2)) {
//                            return false;
//                        }
//                        return super.isItemValid(stack);
//                    }
//                };
            }
            addSlotToContainer(slot);
        }
    }

}
