package com.mcjty.container;

import com.google.common.collect.Range;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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
    protected EntityPlayer player;
    private ContainerFactory factory;

    public GenericContainer(ContainerFactory factory, EntityPlayer player) {
        this.factory = factory;
        this.player = player;
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

    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_GHOST) {
                slot = new GhostSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else if (slotFactory.getSlotType() == SlotType.SLOT_GHOSTOUT) {
                slot = new GhostOutputSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else if (slotFactory.getSlotType() == SlotType.SLOT_ENDERPEARL) {
                slot = new Slot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return Items.ender_pearl.equals(stack.getItem());
                    }
                };
            } else {
                slot = new Slot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            }
            addSlotToContainer(slot);
        }
    }

    private boolean mergeItemStacks(ItemStack itemStack, SlotType slotType, boolean reverse) {
        SlotRanges ranges = factory.getSlotRangesMap().get(slotType);
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

            if (factory.isOutputSlot(index) || factory.isInputSlot(index)) {
                if (!mergeItemStacks(itemstack1, SlotType.SLOT_PLAYERINV, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (factory.isGhostSlot(index) || factory.isGhostOutputSlot(index)) {
                return null; // @@@ Right?
            } else if (factory.isPlayerInventorySlot(index)) {
                if (!mergeItemStacks(itemstack1, SlotType.SLOT_INPUT, false)) {
                    if (!mergeItemStacks(itemstack1, SlotType.SLOT_PLAYERHOTBAR, false)) {
                        return null;
                    }
                }
            } else if (factory.isPlayerHotbarSlot(index)) {
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
