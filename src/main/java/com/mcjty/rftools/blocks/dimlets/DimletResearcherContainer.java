package com.mcjty.rftools.blocks.dimlets;

import com.mcjty.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimletResearcherContainer extends GenericContainer {

    public DimletResearcherContainer(EntityPlayer player, DimletResearcherTileEntity containerInventory) {
        super(DimletResearcherContainerFactory.getInstance(), player);
        addInventory(DimletResearcherContainerFactory.CONTAINER_INVENTORY, containerInventory);
        addInventory(DimletResearcherContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        return super.slotClick(index, button, mode, player);
    }
}
