package mcjty.rftools.blocks.screens;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;

public class ScreenControllerContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public ScreenControllerContainer(PlayerEntity player, IInventory containerInventory) {
        super(factory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
