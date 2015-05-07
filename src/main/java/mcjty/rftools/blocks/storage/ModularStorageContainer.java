package mcjty.rftools.blocks.storage;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModularStorageContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_STORAGE_MODULE = 0;
    public static final int SLOT_FILTER_MODULE = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, SLOT_STORAGE_MODULE, 13, 160, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_FILTER_MODULE, 13, 178, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 160);
        }
    };

    public ModularStorageContainer(EntityPlayer player, ModularStorageTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
