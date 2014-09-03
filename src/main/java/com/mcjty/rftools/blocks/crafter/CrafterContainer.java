package com.mcjty.rftools.blocks.crafter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraftforge.common.util.ForgeDirection;

public class CrafterContainer extends Container {
    private CrafterBlockTileEntity inventory;
    private EntityPlayer player;

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (13*2);

    public CrafterContainer(EntityPlayer player, CrafterBlockTileEntity containerInventory, int xSize, int ySize) {
        inventory = containerInventory;
        this.player = player;
        layoutContainer(xSize, ySize);
    }

    private void layoutContainer(int xSize, int ySize) {
        layoutCraftingGrid();
        layoutBuffer();
        layoutPlayerInventory();
    }

    private void layoutBuffer() {
        int leftCol = 13;
        int topRow = 89;

        for (int py = 0 ; py < 2 ; py++) {
            for (int px = 0 ; px < 12 ; px++) {
                addSlotToContainer(new Slot(inventory, SLOT_BUFFER + (px + py * 13), leftCol + px * 18, topRow + py * 18));
            }
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

    //    @Override
//    public ItemStack transferStackInSlot(EntityPlayer player, int i) {
//        ItemStack itemStack = null;
//        Slot slot = (Slot) inventorySlots.get(i);
//        if (slot != null && slot.getHasStack()) {
//            ItemStack itemStack1 = slot.getStack();
//            itemStack = itemStack1.copy();
//
//        }
//    }
}
