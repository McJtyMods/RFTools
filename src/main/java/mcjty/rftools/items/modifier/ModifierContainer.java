package mcjty.rftools.items.modifier;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import net.minecraft.entity.player.EntityPlayer;

public class ModifierContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

	public static final int COUNT_SLOTS = 2;
	public static final int SLOT_FILTER = 0;
	public static final int SLOT_REPLACEMENT = 1;

	public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
			addSlot(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_FILTER, 10, 8);
			addSlot(new SlotDefinition(SlotType.SLOT_CONTAINER), CONTAINER_INVENTORY, SLOT_REPLACEMENT, 154, 8);
            layoutPlayerInventorySlots(10, 146);
        }
    };

    public ModifierContainer(EntityPlayer player) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, new ModifierInventory(player));
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
