package mcjty.rftools.blocks.storage;

import mcjty.container.*;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ModularStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    private EntityPlayer entityPlayer;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, 0, -500, -500, 30, 0, 10, 0);
            layoutPlayerInventorySlots(91, 157);
        }
    };

    public ModularStorageItemContainer(EntityPlayer player) {
        super(factory);
        this.entityPlayer = player;
        addInventory(CONTAINER_INVENTORY, new ModularStorageItemInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }

    private int getMaxSize() {
        return StorageModuleItem.MAXSIZE[entityPlayer.getHeldItem().getTagCompound().getInteger("childDamage")];
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV || slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
            } else {
                slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                    @Override
                    public boolean isItemValid(ItemStack stack) {
                        return getSlotIndex() < getMaxSize();
                    }
                };
            }
            addSlotToContainer(slot);
        }
    }

}
