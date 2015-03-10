package com.mcjty.rftools.blocks.shield;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class ShieldContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int BUFFER_SIZE = 1;
    public static final int SLOT_PLAYERINV = SLOT_BUFFER + BUFFER_SIZE;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 31, 142, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public ShieldContainer(EntityPlayer player, ShieldTEBase containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
