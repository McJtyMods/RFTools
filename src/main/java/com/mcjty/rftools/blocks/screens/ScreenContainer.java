package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class ScreenContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_MODULES = 0;
    public static final int SCREEN_MODULES = 11;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_MODULES, 7, 7, 1, 18, SCREEN_MODULES, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public ScreenContainer(EntityPlayer player, ScreenTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
