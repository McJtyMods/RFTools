package mcjty.rftools.blocks.storage;

import mcjty.lib.container.*;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.items.storage.StorageTypeItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_TYPE_MODULE = 1;
    public static final int SLOT_FILTER_MODULE = 2;
    public static final int SLOT_STORAGE = 3;
    public static final int MAXSIZE_STORAGE = 300;

    private ModularStorageTileEntity modularStorageTileEntity;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, SLOT_STORAGE_MODULE, 5, 157, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, StorageTypeItem.class), CONTAINER_INVENTORY, SLOT_TYPE_MODULE, 5, 175, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, StorageFilterItem.class), CONTAINER_INVENTORY, SLOT_FILTER_MODULE, 5, 193, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_STORAGE, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
        }
    };

    public ModularStorageContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        modularStorageTileEntity = (ModularStorageTileEntity) containerInventory;
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_SPECIFICITEM) {
                final SlotDefinition slotDefinition = slotFactory.getSlotDefinition();
                slot = new Slot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return slotDefinition.itemStackMatches(stack);
                    }
                };
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean getHasStack() {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        return super.getHasStack();
                    }

                    @Override
                    public ItemStack getStack() {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return null;
                        }
                        return super.getStack();
                    }

                    @Override
                    public boolean canTakeStack(EntityPlayer player) {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        return super.canTakeStack(player);
                    }

                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        if (getSlotIndex() >= (modularStorageTileEntity.getMaxSize() + SLOT_STORAGE)) {
                            return false;
                        }
                        if (!modularStorageTileEntity.isItemValidForSlot(getSlotIndex(), stack)) {
                            return false;
                        }
                        return super.isItemValid(stack);
                    }
                };
            }
            addSlotToContainer(slot);
        }
    }


    @Override
    public ItemStack func_184996_a(int index, int button, ClickType mode, EntityPlayer player) {
        if (index == SLOT_STORAGE_MODULE && !player.worldObj.isRemote) {
            modularStorageTileEntity.copyToModule();
        }
        return super.func_184996_a(index, button, mode, player);
    }
}
