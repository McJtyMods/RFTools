package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.GenericEnergyHandlerContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class CrafterContainer extends GenericEnergyHandlerContainer {

    public CrafterContainer(EntityPlayer player, CrafterBlockTileEntity3 containerInventory) {
        super(CrafterContainerFactory.getInstance(), player, containerInventory);
        addInventory(CrafterContainerFactory.CONTAINER_INVENTORY, containerInventory);
        addInventory(CrafterContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (CrafterContainerFactory.getInstance().isGhostSlot(index)) {
            Slot slot = getSlot(index);
            if (slot.getHasStack()) {
                slot.putStack(null);
            }
        }
        return super.slotClick(index, button, mode, player);
    }
}
