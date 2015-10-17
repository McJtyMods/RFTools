package mcjty.rftools.blocks.shield;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ShieldContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int SLOT_SHAPE = 1;
    public static final int BUFFER_SIZE = 2;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 31, 142, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.shapeCardItem)), CONTAINER_INVENTORY, SLOT_SHAPE, 31, 200, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public ShieldContainer(EntityPlayer player, ShieldTEBase containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
