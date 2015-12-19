package mcjty.rftools.blocks.storage;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class RemoteStorageContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_STORAGE = 0;
    public static final int SLOT_LINKER = 4;

    private RemoteStorageTileEntity remoteStorageTileEntity;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 0, 43, 9, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 1, 43, 36, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 2, 120, 9, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 3, 120, 36, 1, 18, 1, 18);

            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 4, 76, 9, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 5, 76, 36, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 6, 153, 9, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModularStorageSetup.storageModuleItem)), CONTAINER_INVENTORY, 7, 153, 36, 1, 18, 1, 18);

            layoutPlayerInventorySlots(10, 70);
        }
    };

    public RemoteStorageContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        remoteStorageTileEntity = (RemoteStorageTileEntity) containerInventory;
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    @Override
    public ItemStack slotClick(int index, int button, int mode, EntityPlayer player) {
        if (index >= 0 && index < SLOT_LINKER && !player.worldObj.isRemote) {
            remoteStorageTileEntity.copyToModule(index);
        }
        return super.slotClick(index, button, mode, player);
    }
}
