package mcjty.rftools.blocks.infuser;

import mcjty.container.ContainerFactory;
import mcjty.container.GenericContainer;
import mcjty.container.SlotDefinition;
import mcjty.container.SlotType;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class MachineInfuserContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_SHARDINPUT = 0;
    public static final int SLOT_MACHINEOUTPUT = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.dimensionalShard)), CONTAINER_INVENTORY, SLOT_SHARDINPUT, 64, 24, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_MACHINEOUTPUT, 118, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public MachineInfuserContainer(EntityPlayer player, MachineInfuserTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
