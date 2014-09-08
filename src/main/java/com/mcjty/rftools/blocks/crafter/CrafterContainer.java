package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericContainer;
import com.mcjty.container.GhostSlot;
import com.mcjty.container.SlotType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public class CrafterContainer extends GenericContainer {
    private CrafterBlockTileEntity crafterBlockTileEntity;

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (12*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 2;
    public static final int SLOT_PLAYERINV = SLOT_BUFFEROUT + BUFFEROUT_SIZE;
    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public CrafterContainer(EntityPlayer player, CrafterBlockTileEntity containerInventory) {
        super(player, containerInventory);
        crafterBlockTileEntity = containerInventory;
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
        addSlotBox(SlotType.SLOT_INPUT, inventory, SLOT_BUFFER, leftCol, topRow, 12, 18, 2, 18);
        // Output slots
        for (int py = 0 ; py < 2 ; py++) {
            addSlot(SlotType.SLOT_OUTPUT, inventory, SLOT_BUFFEROUT + py, leftCol + 13 * 18, topRow + py * 18);
        }
    }

    private void layoutCraftingGrid() {
        int leftCol = 13;
        int topRow = 134;
        addSlotBox(SlotType.SLOT_GHOST, inventory, SLOT_CRAFTINPUT, leftCol, topRow, 3, 18, 3, 18);
        addSlot(SlotType.SLOT_GHOST, inventory, SLOT_CRAFTOUTPUT, leftCol, 192);
    }

    private void layoutPlayerInventory() {
        // Player inventory
        int leftCol = 85;
        int topRow = 134;
        addSlotBox(SlotType.SLOT_PLAYERINV, player.inventory, 9, leftCol, topRow, 9, 18, 3, 18);

        // Hotbar
        topRow = 192;
        addSlotRange(SlotType.SLOT_PLAYERHOTBAR, player.inventory, 0, leftCol, topRow, 9, 18);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int energyStored = crafterBlockTileEntity.getEnergyStored(ForgeDirection.DOWN);
        if (energyStored != crafterBlockTileEntity.getOldRF()) {
            crafterBlockTileEntity.setOldRF(energyStored);
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
            crafterBlockTileEntity.setCurrentRF(value);
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        crafting.sendProgressBarUpdate(this, 0, 0);
    }
}
