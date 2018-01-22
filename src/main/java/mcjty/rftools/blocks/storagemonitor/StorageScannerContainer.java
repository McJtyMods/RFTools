package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.container.ContainerFactory;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.SlotDefinition;
import mcjty.lib.container.SlotType;
import mcjty.rftools.craftinggrid.CraftingGridInventory;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class StorageScannerContainer extends GenericContainer {

    public static final String CONTAINER_INVENTORY = "container";
    public static final String CONTAINER_GRID = "grid";

    public static final int SLOT_IN = 0;            // This slot is input for the user interface
    public static final int SLOT_OUT = 1;
    public static final int SLOT_IN_AUTO = 2;       // This slot is not shown in the user interface but is for automation
    public static final int SLOT_PLAYERINV = 2;

    private StorageScannerTileEntity storageScannerTileEntity;

    public static final ContainerFactory factory = new ContainerFactory() {
        @Override
        protected void setup() {
            addSlotBox(new SlotDefinition(SlotType.SLOT_INPUT), CONTAINER_INVENTORY, SLOT_IN, 28, 220, 1, 18, 1, 18);
            addSlotBox(new SlotDefinition(SlotType.SLOT_OUTPUT), CONTAINER_INVENTORY, SLOT_OUT, 55, 220, 1, 18, 1, 18);
            layoutPlayerInventorySlots(86, 162);
            layoutGridInventorySlots(CraftingGridInventory.GRID_XOFFSET, CraftingGridInventory.GRID_YOFFSET);
        }

        protected void layoutGridInventorySlots(int leftCol, int topRow) {
            this.addSlotBox(new SlotDefinition(SlotType.SLOT_GHOST), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTINPUT, leftCol, topRow, 3, 18, 3, 18);
            topRow += 58;
            this.addSlotRange(new SlotDefinition(SlotType.SLOT_GHOSTOUT), CONTAINER_GRID, CraftingGridInventory.SLOT_GHOSTOUTPUT, leftCol, topRow, 1, 18);
        }
    };

    public void clearGrid() {
        IInventory inventory = inventories.get(CONTAINER_GRID);
        for (int i = 0 ; i < inventory.getSizeInventory() ; i++) {
            inventory.setInventorySlotContents(i, ItemStack.EMPTY);
        }
    }

    public StorageScannerTileEntity getStorageScannerTileEntity() {
        return storageScannerTileEntity;
    }

    public StorageScannerContainer(EntityPlayer player, IInventory containerInventory) {
        super(factory);
        storageScannerTileEntity = (StorageScannerTileEntity) containerInventory;

        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        addInventory(CONTAINER_GRID, storageScannerTileEntity.getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }

    public StorageScannerContainer(EntityPlayer player, IInventory containerInventory, CraftingGridProvider provider) {
        super(factory);
        storageScannerTileEntity = (StorageScannerTileEntity) containerInventory;

        addInventory(CONTAINER_INVENTORY, containerInventory);
        addInventory(ContainerFactory.CONTAINER_PLAYER, player.inventory);
        addInventory(CONTAINER_GRID, provider.getCraftingGrid().getCraftingGridInventory());
        generateSlots();
    }
}
