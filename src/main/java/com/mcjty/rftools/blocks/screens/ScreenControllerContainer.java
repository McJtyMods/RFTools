package com.mcjty.rftools.blocks.screens;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import net.minecraft.entity.player.EntityPlayer;

public class ScreenControllerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public ScreenControllerContainer(EntityPlayer player, ScreenControllerTileEntity containerInventory) {
        super(factory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
