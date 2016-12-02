package mcjty.rftools.blocks.storage;

import mcjty.lib.container.*;
import mcjty.lib.tools.InventoryTools;
import mcjty.lib.tools.ItemStackTools;
import mcjty.rftools.craftinggrid.CraftingGridInventory;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.items.storage.StorageTypeItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";
    public static final String CONTAINER_GRID = "grid";

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
            layoutGridInventorySlots(CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET);
        }

        protected void layoutGridInventorySlots(int leftCol, int topRow) {
            this.addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, leftCol, topRow, 3, 18, 3, 18);
            topRow += 58;
            this.addSlotRange(new SlotDefinition(SlotType.SLOT_GHOSTOUT), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, leftCol, topRow, 1, 18);
        }

    };

    public ModularStorageTileEntity getModularStorageTileEntity() {
        return modularStorageTileEntity;
    }

    public ModularStorageContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        modularStorageTileEntity = (ModularStorageTileEntity) containerInventory;

        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        addInventory(CONTAINER_GRID, modularStorageTileEntity.getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (CONTAINER_GRID.equals(slotFactory.getInventoryName())) {
                SlotType slotType = slotFactory.getSlotType();
                IInventory inventory = this.inventories.get(slotFactory.getInventoryName());
                int index = slotFactory.getIndex();
                int x = slotFactory.getX();
                int y = slotFactory.getY();
                slot = this.createSlot(slotFactory, inventory, index, x, y, slotType);
            } else if (slotFactory.getSlotType() == SlotType.SLOT_SPECIFICITEM) {
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
                            return ItemStackTools.getEmptyStack();
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
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {
        if (index == SLOT_STORAGE_MODULE && !player.getEntityWorld().isRemote) {
            modularStorageTileEntity.copyToModule();
        }
        System.out.println("index = " + index);
        if (index == 347) {
            System.out.println("ModularStorageContainer.slotClick");
        }
        return super.slotClick(index, button, mode, player);
    }

    @Override
    public void detectAndSendChanges() {
        List<Pair<Integer, ItemStack>> differentSlots = new ArrayList<>();
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = InventoryTools.getContainerItemStacks(this).get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                itemstack1 = ItemStackTools.isEmpty(itemstack) ? ItemStackTools.getEmptyStack() : itemstack.copy();
                InventoryTools.getContainerItemStacks(this).set(i, itemstack1);
                differentSlots.add(Pair.of(i, itemstack));
                if (differentSlots.size() >= 30) {
                    syncSlotsToListeners(differentSlots);
                    // Make a new list so that the one we gave to syncSlots is preserved
                    differentSlots = new ArrayList<>();
                }
            }
        }
        if (!differentSlots.isEmpty()) {
            syncSlotsToListeners(differentSlots);
        }
    }

    private void syncSlotsToListeners(List<Pair<Integer, ItemStack>> differentSlots) {
        String sortMode = modularStorageTileEntity.getSortMode();
        String viewMode = modularStorageTileEntity.getViewMode();
        boolean groupMode = modularStorageTileEntity.isGroupMode();
        String filter = modularStorageTileEntity.getFilter();

        for (IContainerListener listener : this.listeners) {
            if (listener instanceof EntityPlayerMP) {
                EntityPlayerMP player = (EntityPlayerMP) listener;
                RFToolsMessages.INSTANCE.sendTo(new PacketSyncSlotsToClient(
                        modularStorageTileEntity.getPos(),
                        sortMode, viewMode, groupMode, filter,
                        modularStorageTileEntity.getMaxSize(),
                        modularStorageTileEntity.getNumStacks(),
                        differentSlots), player);
            }
        }
    }
}
