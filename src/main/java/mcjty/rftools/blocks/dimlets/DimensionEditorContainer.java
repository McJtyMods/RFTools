package mcjty.rftools.blocks.dimlets;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class DimensionEditorContainer extends GenericContainer {
    public static final String CONTAINER_INVENTORY = "container";

    public static final int SLOT_INJECTINPUT = 0;
    public static final int SLOT_DIMENSIONTARGET = 1;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.knownDimlet), new ItemStack(TeleporterSetup.matterReceiverBlock)), CONTAINER_INVENTORY, SLOT_INJECTINPUT, 64, 24, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_SPECIFICITEM, new ItemStack(DimletSetup.realizedDimensionTab)), CONTAINER_INVENTORY, SLOT_DIMENSIONTARGET, 118, 24, 1, 18, 1, 18);
            layoutPlayerInventorySlots(10, 70);
        }
    };

    public DimensionEditorContainer(EntityPlayer player, DimensionEditorTileEntity containerInventory) {
        super(factory);
        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        generateSlots();
    }
}
