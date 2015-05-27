package mcjty.rftools.items.storage;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class StorageFilterContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int FILTER_SLOTS = 6*3;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, 0, 10, 9, 6, 18, 3, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public StorageFilterContainer(EntityPlayer player) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, new StorageFilterInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
