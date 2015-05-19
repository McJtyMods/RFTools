package mcjty.rftools.blocks.storage;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ModularStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int MAXSIZE_STORAGE = 300;

    private int id;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, 0, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
        }
    };

    public ModularStorageItemContainer(EntityPlayer player) {
        super(factory);
        ItemStack stack = player.getHeldItem();
        // We assume the item is right here
        id = stack.getTagCompound().getInteger("id");
        RemoteStorageTileEntity remoteStorageTileEntity = RemoteStorageIdRegistry.getRemoteStorage(player.worldObj, id);
        if (remoteStorageTileEntity != null) {
            int si = remoteStorageTileEntity.findRemoteIndex(id);
            if (si != -1) {
                stack.getTagCompound().setInteger("maxSize", remoteStorageTileEntity.getMaxStacks(si));
            }
        }

        addInventory(CONTAINER_INVENTORY, new ModularStorageItemInventory(player, id));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
