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
        System.out.println("canInteractWith");
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
                System.out.println("com.mcjty.container.GenericContainer.generateSlots");
                slot = new GhostSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
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
        System.out.println("transferStackInSlot: index = " + index);
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
            } else if (factory.isGhostSlot(index)) {
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

    @Override
    protected boolean mergeItemStack(ItemStack stack, int start, int end, boolean reverse) {
        System.out.println("mergeItemStack: start = " + start + ", end = " + end);

        return super.mergeItemStack(stack, start, end, reverse);
    }


}
