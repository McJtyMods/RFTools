package mcjty.rftools.blocks.itemfilter;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class ItemFilterContainer extends GenericContainer {

    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_GHOST = 0;
    public static final int GHOST_SIZE = 9;
    public static final int SLOT_BUFFER = 9;
    public static final int BUFFER_SIZE = 9;
    public static final int SLOT_PLAYERINV = GHOST_SIZE + BUFFER_SIZE;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_GHOST, 24, 105, 9, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 24, 87, 9, 18, 1, 18);
            layoutPlayerInventorySlots(24, 130);
        }
    };

    public ItemFilterContainer(EntityPlayer player, ItemFilterTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
