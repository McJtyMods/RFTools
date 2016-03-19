package mcjty.rftools.blocks.storage;

import mcjty.lib.container.*;
import mcjty.rftools.items.storage.StorageModuleItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class ModularStorageItemContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    private EntityPlayer entityPlayer;
    private int tabletIndex;

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
        tabletIndex = player.inventory.currentItem;
        generateSlots();
    }

    private int getMaxSize() {
        return StorageModuleItem.MAXSIZE[entityPlayer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound().getInteger("childDamage")];
    }

    @Override
    public void generateSlots() {
        for (SlotFactory slotFactory : factory.getSlots()) {
            Slot slot;
            if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERHOTBAR) {
                if (slotFactory.getIndex() == tabletIndex) {
                    slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
                        @Override
                        public boolean canTakeStack(EntityPlayer player) {
                            // We don't want to take the stack from this slot.
                            return false;
                        }
                    };
                } else {
                    slot = new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY());
                }
            } else if (slotFactory.getSlotType() == SlotType.SLOT_PLAYERINV) {
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
