package mcjty.rftools.blocks.screens;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

public class ScreenContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_MODULES = 0;
    public static final int SCREEN_MODULES = 11;

    public static final ContainerFactory CONTAINER_FACTORY = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_MODULES, 7, 8, 1, 18, SCREEN_MODULES, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public ScreenContainer(PlayerEntity player, IInventory containerInventory) {
        super(CONTAINER_FACTORY);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
