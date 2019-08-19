package mcjty.rftools.items.storage;

import mcjty.lib.container.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class StorageFilterContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_FILTER = 0;
    public static final int FILTER_SLOTS = 6*5;

	private int cardIndex;

	public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_INVENTORY, SLOT_FILTER, 10, 9, 6, 18, 5, 18);
            layoutPlayerInventorySlots(10, 106);
        }
    };

    public StorageFilterContainer(PlayerEntity player) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, new StorageFilterInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
		cardIndex = player.inventory.currentItem;
        generateSlots();
    }

	@Override
	protected Slot createSlot(SlotFactory slotFactory, IInventory inventory, int index, int x, int y, SlotType slotType) {
		if (slotType == SlotType.SLOT_PLAYERHOTBAR && index == cardIndex) {
			return new BaseSlot(inventories.get(slotFactory.getInventoryName()), slotFactory.getIndex(), slotFactory.getX(), slotFactory.getY()) {
				@Override
				public boolean canTakeStack(PlayerEntity player) {
					// We don't want to take the stack from this slot.
					return false;
				}
			};
		} else {
			return super.createSlot(slotFactory, inventory, index, x, y, slotType);
		}
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity player, int index) {
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack() && index >= FILTER_SLOTS && index < FILTER_SLOTS + 36) {
			ItemStack stack = slot.getStack().copy();
			stack.setCount(1);
			IInventory inv = inventories.get(CONTAINER_INVENTORY);
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getStackInSlot(i).isEmpty()) {
					inv.setInventorySlotContents(i, stack);
					break;
				}
			}
			slot.onSlotChanged();

		}

		return ItemStack.EMPTY;
	}
}
