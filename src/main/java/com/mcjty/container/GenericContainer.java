package com.mcjty.container;

import com.google.common.collect.Range;
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
    protected IInventory inventory;
    protected EntityPlayer player;

    private Map<Integer,SlotType> indexToType = new HashMap<Integer, SlotType>();
    private Map<SlotType,SlotRanges> slotRangesMap = new HashMap<SlotType,SlotRanges>();

    public GenericContainer(EntityPlayer player, IInventory inventory) {
        this.inventory = inventory;
        this.player = player;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return inventory.isUseableByPlayer(entityPlayer);
    }

    public IInventory getContainerInventory() {
        return inventory;
    }

    public SlotType getSlotType(int index) {
        SlotType slotType = indexToType.get(index);
        if (slotType == null) {
            return SlotType.SLOT_UNKNOWN;
        }
        return slotType;
    }

    public boolean isOutputSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_OUTPUT;
    }

    public boolean isInputSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_INPUT;
    }

    public boolean isGhostSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_GHOST;
    }

    public boolean isPlayerInventorySlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERINV;
    }

    public boolean isPlayerHotbarSlot(int index) {
        return getSlotType(index) == SlotType.SLOT_PLAYERHOTBAR;
    }

    public void addSlot(SlotType slotType, IInventory inventory, int index, int x, int y) {
        Slot slot;
        if (slotType == SlotType.SLOT_GHOST) {
            slot = new GhostSlot(inventory, index, x, y); //@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        } else {
            slot = new Slot(inventory, index, x, y);
        }
        addSlotToContainer(slot);

        SlotRanges slotRanges = slotRangesMap.get(slotType);
        if (slotRanges == null) {
            slotRanges = new SlotRanges(slotType);
            slotRangesMap.put(slotType, slotRanges);
        }
        slotRanges.addSingle(slot.slotNumber);

        indexToType.put(slot.slotNumber, slotType);
    }

    public int addSlotRange(SlotType slotType, IInventory inventory, int index, int x, int y, int amount, int dx) {
        for (int i = 0 ; i < amount ; i++) {
            addSlot(slotType, inventory, index, x, y);
            x += dx;
            index++;
        }
        return index;
    }

    public int addSlotBox(SlotType slotType, IInventory inventory, int index, int x, int y, int horAmount, int dx, int verAmount, int dy) {
        for (int j = 0 ; j < verAmount ; j++) {
            index = addSlotRange(slotType, inventory, index, x, y, horAmount, dx);
            y += dy;
        }
        return index;
    }

    private boolean mergeItemStacks(ItemStack itemStack, SlotType slotType, boolean reverse) {
        SlotRanges ranges = slotRangesMap.get(slotType);
        for (Range<Integer> r : ranges.asRanges()) {
            Integer start = r.lowerEndpoint();
            int end = r.upperEndpoint() + 1;
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

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (isOutputSlot(index) || isInputSlot(index)) {
                if (!mergeItemStacks(itemstack1, SlotType.SLOT_PLAYERINV, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (isGhostSlot(index)) {
                return null; // @@@ Right?
            } else if (isPlayerInventorySlot(index)) {
                if (!mergeItemStacks(itemstack1, SlotType.SLOT_INPUT, false)) {
                    if (!mergeItemStacks(itemstack1, SlotType.SLOT_PLAYERHOTBAR, false)) {
                        return null;
                    }
                }
            } else if (isPlayerHotbarSlot(index)) {
                if (!mergeItemStacks(itemstack1, SlotType.SLOT_INPUT, false)) {
                    if (!mergeItemStacks(itemstack1, SlotType.SLOT_PLAYERINV, false)) {
                        return null;
                    }
                }
            } else {
                System.out.println("WEIRD SLOT???");
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.stackSize == itemstack.stackSize) {
                return null;
            }

            slot.onPickupFromSlot(player, itemstack1);
        }

        return itemstack;
    }
}
