package com.mcjty.rftools.blocks.dimletconstruction;

import com.mcjty.container.*;
import com.mcjty.rftools.Achievements;
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
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.knownDimlet)), CONTAINER_INVENTORY, SLOT_INPUT, 11, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CRAFTRESULT), CONTAINER_INVENTORY, SLOT_OUTPUT, 173, 115, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletBaseItem)), CONTAINER_INVENTORY, SLOT_BASE, 173, 7, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletControlCircuitItem)), CONTAINER_INVENTORY, SLOT_CONTROLLER, 173, 25, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletEnergyModuleItem)), CONTAINER_INVENTORY, SLOT_ENERGY, 173, 43, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletMemoryUnitItem)), CONTAINER_INVENTORY, SLOT_MEMORY, 173, 61, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimletTypeControllerItem)), CONTAINER_INVENTORY, SLOT_TYPE_CONTROLLER, 173, 79, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT/*@@@ ALSO SPECIFIC FOR ESSENCE ITEM!*/), CONTAINER_INVENTORY, SLOT_ESSENCE, 173, 97, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_BUFFER, 11, 25, 7, 18, 6, 18);
            layoutPlayerInventorySlots(29, 142);
        }
    };

    public DimletWorkbenchContainer(EntityPlayer player, final DimletWorkbenchTileEntity containerInventory) {
        super(factory, player);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        setCrafter(new GenericCrafter() {
            @Override
            public void craftItem() {
                containerInventory.craftDimlet();
            }
        });
        generateSlots();
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        return super.transferStackInSlot(player, index);
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index == SLOT_OUTPUT) {
            Achievements.trigger(player, Achievements.dimletMaster);
        }
        return super.slotClick(index, button, mode, player);
    }
}
