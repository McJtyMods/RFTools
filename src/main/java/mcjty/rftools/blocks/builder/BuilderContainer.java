package mcjty.rftools.blocks.builder;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftools.items.storage.StorageFilterItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class BuilderContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_TAB = 0;
    public static final int SLOT_FILTER = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM,
                    new ItemStack(BuilderSetup.spaceChamberCardItem),
                    new ItemStack(BuilderSetup.shapeCardItem)),
                    CONTAINER_INVENTORY, SLOT_TAB, 87, 10, 1, 18, 1, 18);
            addSlot(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, StorageFilterItem.class), CONTAINER_INVENTORY, SLOT_FILTER, 88, 46);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public BuilderContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
