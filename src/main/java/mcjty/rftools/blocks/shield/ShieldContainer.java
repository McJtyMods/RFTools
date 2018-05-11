package mcjty.rftools.blocks.shield;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftools.blocks.builder.BuilderSetup;
import mcjty.rftools.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ShieldContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_BUFFER = 0;
    public static final int SLOT_SHAPE = 1;
    public static final int SLOT_SHARD = 2;
    public static final int BUFFER_SIZE = 3;
//    public static final int SLOT_PLAYERHOTBAR = SLOT_PLAYERINV + 3*9;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_BUFFER, 26, 142, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(BuilderSetup.shapeCardItem)), CONTAINER_INVENTORY, SLOT_SHAPE, 26, 200, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(ModItems.dimensionalShardItem)), CONTAINER_INVENTORY, SLOT_SHARD, 229, 118, 1, 18, 1, 18);
            layoutPlayerInventorySlots(85, 142);
        }
    };


    public ShieldContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
