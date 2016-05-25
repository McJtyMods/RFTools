package mcjty.rftools.blocks.storage;

import mcjty.lib.container.*;
import mcjty.rftools.items.storage.StorageFilterItem;
import mcjty.rftools.items.storage.StorageTypeItem;
import mcjty.rftools.network.RFToolsMessages;
import mcjty.rftools.varia.RFToolsTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.List;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_TYPE_MODULE = 1;
    public static final int SLOT_FILTER_MODULE = 2;
    public static final int SLOT_STORAGE = 3;
    public static final int MAXSIZE_STORAGE = 300;

    // Change detection data
    private String sortMode;
    private String viewMode;
    private Boolean groupMode;
    private String filter;
    private int maxsize;

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

    private void copyFromTE() {
        sortMode = modularStorageTileEntity.getSortMode();
        viewMode = modularStorageTileEntity.getViewMode();
        groupMode = modularStorageTileEntity.isGroupMode();
        filter = modularStorageTileEntity.getFilter();
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
    public void putStackInSlot(int slotID, ItemStack stack) {
        super.putStackInSlot(slotID, stack);
    }

    @Override
    public ItemStack slotClick(int index, int button, ClickType mode, EntityPlayer player) {
        if (index == SLOT_STORAGE_MODULE && !player.worldObj.isRemote) {
            modularStorageTileEntity.copyToModule();
        }
        return super.slotClick(index, button, mode, player);
    }

    @Override
    public void detectAndSendChanges() {
//        super.detectAndSendChanges();

        System.out.println((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? "CLIENT " : "SERVER ") + "detectAndSendChanges");

        boolean differs = false;
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack itemstack = this.inventorySlots.get(i).getStack();
            ItemStack itemstack1 = this.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
                differs = true;
                itemstack1 = itemstack == null ? null : itemstack.copy();
                this.inventoryItemStacks.set(i, itemstack1);
            }
        }
        if (differs) {
            System.out.println((FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT ? "CLIENT " : "SERVER ") + "detectAndSendChanges DIFFERS");
            List<ItemStack> stacks = new ArrayList<>(modularStorageTileEntity.getSizeInventory());
            for (int i = 0 ; i < modularStorageTileEntity.getSizeInventory() ; i++) {
                stacks.add(modularStorageTileEntity.getStackInSlot(i));
            }

            for (IContainerListener listener : this.listeners) {
                if (listener instanceof EntityPlayerMP) {
                    EntityPlayerMP player = (EntityPlayerMP) listener;
                    RFToolsMessages.INSTANCE.sendTo(new PacketSyncInventoryToClient(modularStorageTileEntity.getPos(), modularStorageTileEntity.getMaxSize(),
                            modularStorageTileEntity.getNumStacks(), stacks), player);
                }
            }
        }

        boolean same = RFToolsTools.safeEquals(sortMode, modularStorageTileEntity.getSortMode()) &&
                RFToolsTools.safeEquals(viewMode, modularStorageTileEntity.getViewMode()) &&
                RFToolsTools.safeEquals(filter, modularStorageTileEntity.getFilter()) &&
                RFToolsTools.safeEquals(groupMode, modularStorageTileEntity.isGroupMode());
        if (!same) {
            copyFromTE();
            notifyPlayerOfChanges(RFToolsMessages.INSTANCE, modularStorageTileEntity.getWorld(), modularStorageTileEntity.getPos());
        }
    }
}
