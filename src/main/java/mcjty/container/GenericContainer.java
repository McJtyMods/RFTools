package mcjty.container;

import com.google.common.collect.Range;
import mcjty.rftools.RFTools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Generic container support.
 */
public class GenericContainer extends Container {
    protected Map<String,IInventory> inventories = new HashMap<String, IInventory>();
    private ContainerFactory factory;
    private GenericCrafter crafter = null;

    public GenericContainer(ContainerFactory factory) {
        this.factory = factory;
    }

    public void addInventory(String name, IInventory inventory) {
        inventories.put(name, inventory);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        for (IInventory inventory : inventories.values()) {
            if (!inventory.isUseableByPlayer(entityPlayer)) {
                return false;
            }
        }
        return true;
    }

    public SlotType getSlotType(int index) {
        return factory.getSlotType(index);
    }

    public GenericCrafter getCrafter() {
        return crafter;
    }

    public void setCrafter(GenericCrafter crafter) {
        this.crafter = crafter;
    }

    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            IInventory inventory = inventories.get(slotFactory.getInventoryName());
            int index = slotFactory.getIndex();
            int x = slotFactory.getX();
            int y = slotFactory.getY();
            SlotType slotType = slotFactory.getSlotType();
            Slot slot = createSlot(slotFactory, inventory, index, x, y, slotType);
            addSlotToContainer(slot);
        }
    }

    protected Slot createSlot(SlotFactory slotFactory, final IInventory inventory, final int index, final int x, final int y, SlotType slotType) {
        Slot slot;
        if (slotType == SlotType.SLOT_GHOST) {
            slot = new GhostSlot(inventory, index, x, y);
        } else if (slotType == SlotType.SLOT_GHOSTOUT) {
            slot = new GhostOutputSlot(inventory, index, x, y);
        } else if (slotType == SlotType.SLOT_SPECIFICITEM) {
            final SlotDefinition slotDefinition = slotFactory.getSlotDefinition();
            slot = new Slot(inventory, index, x, y) {
                @Override
                public boolean isItemValid(ItemStack stack) {
                    return slotDefinition.itemStackMatches(stack);
                }
            };
        } else if (slotType == SlotType.SLOT_CRAFTRESULT) {
            slot = new CraftingSlot(inventory, index, x, y, crafter);
        } else {
            slot = new BaseSlot(inventory, index, x, y);
        }
        return slot;
    }

    private boolean mergeItemStacks(ItemStack itemStack, SlotType slotType, boolean reverse) {
        if (slotType == SlotType.SLOT_SPECIFICITEM) {
            for (SlotDefinition definition : factory.getSlotRangesMap().keySet()) {
                if (slotType.equals(definition.getType())) {
                    if (mergeItemStacks(itemStack, definition, reverse)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return mergeItemStacks(itemStack, new SlotDefinition(slotType), reverse);
        }
    }

    private boolean mergeItemStacks(ItemStack itemStack, SlotDefinition slotDefinition, boolean reverse) {
        SlotRanges ranges = factory.getSlotRangesMap().get(slotDefinition);
        if (ranges == null) {
            return false;
        }

        SlotType slotType = slotDefinition.getType();

        if (itemStack.getItem() != null && slotType == SlotType.SLOT_SPECIFICITEM && !slotDefinition.itemStackMatches(itemStack)) {
            return false;
        }
        for (Range<Integer> r : ranges.asRanges()) {
            Integer start = r.lowerEndpoint();
            int end = r.upperEndpoint();
            if (mergeItemStack(itemStack, start, end, reverse)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack origStack = slot.getStack();
            itemstack = origStack.copy();

            if (factory.isSpecificItemSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, true)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                        return null;
                    }
                }
                slot.onSlotChange(origStack, itemstack);
            } else if (factory.isOutputSlot(index) || factory.isInputSlot(index) || factory.isContainerSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, true)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                            return null;
                        }
                    }
                }
                slot.onSlotChange(origStack, itemstack);
            } else if (factory.isGhostSlot(index) || factory.isGhostOutputSlot(index)) {
                return null; // @@@ Right?
            } else if (factory.isPlayerInventorySlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_INPUT, false)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERHOTBAR, false)) {
                            return null;
                        }
                    }
                }
            } else if (factory.isPlayerHotbarSlot(index)) {
                if (!mergeItemStacks(origStack, SlotType.SLOT_SPECIFICITEM, false)) {
                    if (!mergeItemStacks(origStack, SlotType.SLOT_INPUT, false)) {
                        if (!mergeItemStacks(origStack, SlotType.SLOT_PLAYERINV, false)) {
                            return null;
                        }
                    }
                }
            } else {
                RFTools.log("Weird slot at index: " + index);
            }

            if (origStack.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (origStack.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, origStack);
        }

        return itemstack;
    }


    @Override
    protected boolean mergeItemStack(ItemStack par1ItemStack, int fromIndex, int toIndex, boolean reversOrder) {
        boolean result = false;
        int checkIndex = fromIndex;

        if (reversOrder) {
            checkIndex = toIndex - 1;
        }

        Slot slot;
        ItemStack itemstack1;

        if (par1ItemStack.isStackable()) {

            while (par1ItemStack.stackSize > 0 && (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex)) {
                slot = (Slot) this.inventorySlots.get(checkIndex);
                itemstack1 = slot.getStack();

                if (itemstack1 != null && itemstack1.getItem() == par1ItemStack.getItem() && (!par1ItemStack.getHasSubtypes() || par1ItemStack.getItemDamage() == itemstack1.getItemDamage())
                        && ItemStack.areItemStackTagsEqual(par1ItemStack, itemstack1) && slot.isItemValid(par1ItemStack)) {

                    int mergedSize = itemstack1.stackSize + par1ItemStack.stackSize;
                    int maxStackSize = Math.min(par1ItemStack.getMaxStackSize(), slot.getSlotStackLimit());
                    if (mergedSize <= maxStackSize) {
                        par1ItemStack.stackSize = 0;
                        itemstack1.stackSize = mergedSize;
                        slot.onSlotChanged();
                        result = true;
                    } else if (itemstack1.stackSize < maxStackSize) {
                        par1ItemStack.stackSize -= maxStackSize - itemstack1.stackSize;
                        itemstack1.stackSize = maxStackSize;
                        slot.onSlotChanged();
                        result = true;
                    }
                }

                if (reversOrder) {
                    --checkIndex;
                } else {
                    ++checkIndex;
                }
            }
        }

        if (par1ItemStack.stackSize > 0) {
            if (reversOrder) {
                checkIndex = toIndex - 1;
            } else {
                checkIndex = fromIndex;
            }

            while (!reversOrder && checkIndex < toIndex || reversOrder && checkIndex >= fromIndex) {
                slot = (Slot) this.inventorySlots.get(checkIndex);
                itemstack1 = slot.getStack();

                if (itemstack1 == null && slot.isItemValid(par1ItemStack)) {
                    ItemStack in = par1ItemStack.copy();
                    in.stackSize = Math.min(in.stackSize, slot.getSlotStackLimit());

                    slot.putStack(in);
                    slot.onSlotChanged();
                    if (in.stackSize >= par1ItemStack.stackSize) {
                        par1ItemStack.stackSize = 0;
                    } else {
                        par1ItemStack.stackSize -= in.stackSize;
                    }
                    result = true;
                    break;
                }

                if (reversOrder) {
                    --checkIndex;
                } else {
                    ++checkIndex;
                }
            }
        }

        return result;
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (factory.isGhostSlot(index)) {
            Slot slot = getSlot(index);
            if (slot.getHasStack()) {
                slot.putStack(null);
            }
        }
        return super.slotClick(index, button, mode, player);
    }
}
