package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.ContainerFactory;
import com.mcjty.container.GenericContainer;
import com.mcjty.container.SlotDefinition;
import com.mcjty.container.SlotType;
import com.mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimletWorkbenchContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    public static final int SLOT_BASE = 2;
    public static final int SLOT_CONTROLLER = 3;
    public static final int SLOT_ENERGY = 4;
    public static final int SLOT_MEMORY = 5;
    public static final int SLOT_TYPE_CONTROLLER = 6;
    public static final int SLOT_ESSENCE = 7;
    public static final int SLOT_BUFFER = 8;

    public static final int SIZE_BUFFER = 7*6;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.knownDimlet)), CONTAINER_INVENTORY, SLOT_INPUT, 10, 6, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT, new ItemStack(ModItems.knownDimlet)), CONTAINER_INVENTORY, SLOT_OUTPUT, 173, 114, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletBaseItem)), CONTAINER_INVENTORY, SLOT_BASE, 173, 6, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletControlCircuitItem)), CONTAINER_INVENTORY, SLOT_CONTROLLER, 173, 24, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletEnergyModuleItem)), CONTAINER_INVENTORY, SLOT_ENERGY, 173, 42, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletMemoryUnitItem)), CONTAINER_INVENTORY, SLOT_MEMORY, 173, 60, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletTypeControllerItem)), CONTAINER_INVENTORY, SLOT_TYPE_CONTROLLER, 173, 78, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT/*@@@ ALSO SPECIFIC FOR ESSENCE ITEM!*/), CONTAINER_INVENTORY, SLOT_ESSENCE, 173, 96, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_BUFFER, 10, 24, 7, 18, 6, 18);
            layoutPlayerInventorySlots(28, 142);
        }
    };

    public DimletWorkbenchContainer(EntityPlayer player, DimletWorkbenchTileEntity containerInventory) {
        super(factory, player);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
