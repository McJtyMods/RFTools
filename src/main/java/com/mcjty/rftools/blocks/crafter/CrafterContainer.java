package com.mcjty.rftools.blocks.crafter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CrafterContainer extends Container {
    private CrafterBlockTileEntity inventory;
    private EntityPlayer player;

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (12*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 2;
    public static final int SLOT_PLAYERINV = SLOT_BUFFEROUT + BUFFEROUT_SIZE;
    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static boolean isOutputSlot(int index) {
        return index >= SLOT_BUFFEROUT && index < SLOT_PLAYERINV;
    }

    public static boolean isInputSlot(int index) {
        return index >= SLOT_BUFFER && index < SLOT_BUFFEROUT;
    }

    public static boolean isGhostSlot(int index) {
        return index <= SLOT_CRAFTOUTPUT;
    }

    public static boolean isPlayerInventorySlot(int index) {
        return index >= SLOT_PLAYERINV && index < SLOT_PLAYERHOTBAR;
    }

    public static boolean isPlayerHotbarSlot(int index) {
        return index >= SLOT_PLAYERHOTBAR;
    }

    public CrafterContainer(EntityPlayer player, CrafterBlockTileEntity containerInventory) {
        inventory = containerInventory;
        this.player = player;
        layoutContainer();
    }

    private void layoutContainer() {
        layoutCraftingGrid();
        layoutBuffer();
        layoutPlayerInventory();
    }

    private void layoutBuffer() {
        int leftCol = 12;
        int topRow = 89;

        // Input slots
        for (int py = 0 ; py < 2 ; py++) {
            for (int px = 0 ; px < 13 ; px++) {
                addSlotToContainer(new Slot(inventory, SLOT_BUFFER + (px + py * 12), leftCol + px * 18, topRow + py * 18));
            }
        }

        // Output slots
        for (int py = 0 ; py < 2 ; py++) {
            addSlotToContainer(new Slot(inventory, SLOT_BUFFEROUT + py, leftCol + 13 * 18, topRow + py * 18));
        }
    }

    private void layoutCraftingGrid() {
        int leftCol = 13;
        int topRow = 134;
        for (int py = 0 ; py < 3 ; py++) {
            for (int px = 0 ; px < 3 ; px++) {
                addSlotToContainer(new GhostSlot(inventory, SLOT_CRAFTINPUT + (px + py * 3), leftCol + px * 18, topRow + py * 18));
            }
        }

        addSlotToContainer(new GhostSlot(inventory, SLOT_CRAFTOUTPUT, leftCol, 192));
    }

    private void layoutPlayerInventory() {
        // Player inventory
        int leftCol = 85;
        int topRow = 134;
        for (int py = 0 ; py < 3 ; py++) {
            for (int px = 0 ; px < 9 ; px++) {
                addSlotToContainer(new Slot(player.inventory, px + py * 9 + 9, leftCol + px * 18, topRow + py * 18));
            }
        }

        // Hotbar
        topRow = 192;
        for (int hx = 0 ; hx < 9 ; hx++) {
            addSlotToContainer(new Slot(player.inventory, hx, leftCol + hx * 18, topRow));
        }
    }

    public IInventory getContainerInventory() {
        return inventory;
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityPlayer) {
        return inventory.isUseableByPlayer(entityPlayer);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int energyStored = inventory.getEnergyStored(ForgeDirection.DOWN);
        if (energyStored != inventory.getOldRF()) {
            inventory.setOldRF(energyStored);
            for (Object crafter : this.crafters) {
                ICrafting icrafting = (ICrafting) crafter;
                icrafting.sendProgressBarUpdate(this, 0, energyStored);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int type, int value) {
        super.updateProgressBar(type, value);
        if (type == 0) {
            inventory.setCurrentRF(value);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        crafting.sendProgressBarUpdate(this, 0, 0);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        System.out.println("index = " + index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (isOutputSlot(index) || isInputSlot(index)) {
                if (!mergeItemStack(itemstack1, SLOT_PLAYERINV, SLOT_PLAYERINV+4*9, true)) {
                    return null;
                }
                slot.onSlotChange(itemstack1, itemstack);
            } else if (isGhostSlot(index)) {
                return null; // @@@ Right?
            } else if (isPlayerInventorySlot(index)) {
                if (!mergeItemStack(itemstack1, SLOT_BUFFER, SLOT_BUFFER+BUFFER_SIZE, false)) {
                        if (!mergeItemStack(itemstack1, SLOT_PLAYERHOTBAR, SLOT_PLAYERHOTBAR+9, false)) {
                        return null;
                    }
                }
            } else if (isPlayerHotbarSlot(index)) {
                if (!mergeItemStack(itemstack1, SLOT_BUFFER, SLOT_BUFFER+BUFFER_SIZE, false)) {
                    if (!mergeItemStack(itemstack1, SLOT_PLAYERINV, SLOT_PLAYERINV+3*9, false)) {
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
