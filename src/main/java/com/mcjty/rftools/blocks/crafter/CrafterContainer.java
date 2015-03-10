package com.mcjty.rftools.blocks.crafter;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class CrafterContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_CRAFTINPUT = 0;
    public static final int SLOT_CRAFTOUTPUT = 9;
    public static final int SLOT_BUFFER = 10;
    public static final int BUFFER_SIZE = (13*2);
    public static final int SLOT_BUFFEROUT = SLOT_BUFFER + BUFFER_SIZE;
    public static final int BUFFEROUT_SIZE = 4;
//    public static final int SLOT_PLAYERINV = SLOT_BUFFEROUT + BUFFEROUT_SIZE;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_CRAFTINPUT, 193, 7, 3, 18, 3, 18);
            addSlot(new SlotDefinition(SlotType.SLOT_GHOSTOUT), CONTAINER_INVENTORY, SLOT_CRAFTOUTPUT, 193, 65);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 12, 97, 13, 18, 2, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_BUFFEROUT, 31, 142, 2, 18, 2, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public CrafterContainer(EntityPlayer player, CrafterBaseTE containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
