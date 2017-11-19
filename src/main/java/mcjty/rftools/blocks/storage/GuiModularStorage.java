package mcjty.rftools.blocks.storage;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.GenericContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.container.GhostOutputSlot;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketUpdateNBTItem;
import mcjty.lib.varia.Logging;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.modules.DefaultTypeModule;
import mcjty.rftools.blocks.storage.modules.TypeModule;
import mcjty.rftools.blocks.storage.sorters.ItemSorter;
import mcjty.rftools.craftinggrid.CraftingGridProvider;
import mcjty.rftools.craftinggrid.GuiCraftingGrid;
import mcjty.rftools.craftinggrid.PacketRequestGridSync;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mcjty.rftools.blocks.storage.ModularStorageContainer.CONTAINER_GRID;


public class GuiModularStorage extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int STORAGE_WIDTH = 256;
    public static final int STORAGE_HEIGHT0 = ModularStorageConfiguration.height1;
    public static final int STORAGE_HEIGHT1 = ModularStorageConfiguration.height2;
    public static final int STORAGE_HEIGHT2 = ModularStorageConfiguration.height3;

    public static final String VIEW_LIST = "list";
    public static final String VIEW_COLUMNS = "columns";
    public static final String VIEW_ICONS = "icons";

    private TypeModule typeModule;

    private static final ResourceLocation iconLocationTop = new ResourceLocation(RFTools.MODID, "textures/gui/modularstoragetop.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/modularstorage.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private WidgetList itemList;
    private TextField filter;
    private ImageChoiceLabel viewMode;
    private ImageChoiceLabel sortMode;
    private ImageChoiceLabel groupMode;
    private Label amountLabel;
    private Button cycleButton;
    private Button compactButton;

    private GuiCraftingGrid craftingGrid;

    public GuiModularStorage(ModularStorageTileEntity modularStorageTileEntity, ModularStorageContainer container) {
        this(modularStorageTileEntity, (Container) container);
    }

    public GuiModularStorage(RemoteStorageItemContainer container) {
        this(null, container);
    }

    public GuiModularStorage(ModularStorageItemContainer container) {
        this(null, container);
    }

    public GuiModularStorage(ModularStorageTileEntity modularStorageTileEntity, Container container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, modularStorageTileEntity, container, RFTools.GUI_MANUAL_MAIN, "storage");

        craftingGrid = new GuiCraftingGrid();

        xSize = STORAGE_WIDTH;

        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft());
        int height = scaledresolution.getScaledHeight();

        if (height > 510) {
            ySize = STORAGE_HEIGHT2;
        } else  if (height > 340) {
            ySize = STORAGE_HEIGHT1;
        } else {
            ySize = STORAGE_HEIGHT0;
        }

        IInventory gridInventory = ((GenericContainer) container).getInventory(CONTAINER_GRID);
        for (Object o : container.inventorySlots) {
            Slot slot = (Slot) o;
            if (slot.inventory != gridInventory) {
                slot.yPos = slot.yPos + ySize - STORAGE_HEIGHT0;
                //                slot.yPos += ySize - STORAGE_HEIGHT0;
            }
        }
    }

    @Override
    public void initGui() {
        super.initGui();

        itemList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 235, ySize-89)).setNoSelectionMode(true).setUserObject(new Integer(-1)).
                setLeftMargin(0).setRowheight(-1);
        Slider slider = new Slider(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(241, 3, 11, ySize - 89)).setDesiredWidth(11).setVertical().setScrollable(itemList);


        Panel modePanel = setupModePanel();

        cycleButton = new Button(mc, this).setText("C").setTooltips("Cycle to the next storage module").setLayoutHint(new PositionalLayout.PositionalHint(5, ySize-23, 16, 16)).
                addButtonEvent(parent -> cycleStorage());

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider)
                .addChild(modePanel)
                .addChild(cycleButton);

        toplevel.setBackgrounds(iconLocationTop, iconLocation);
        toplevel.setBackgroundLayout(false, ySize-STORAGE_HEIGHT0+2);

        if (tileEntity == null) {
            // We must hide three slots.
            ImageLabel hideLabel = new ImageLabel(mc, this);
            hideLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, ySize-26-3*18, 20, 55));
            hideLabel.setImage(guiElements, 32, 32);
            toplevel.addChild(hideLabel);
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        if (ModularStorageConfiguration.autofocusSearch) {
            window.setTextFocus(filter);
        }

        CraftingGridProvider provider;
        BlockPos pos = null;
        if (tileEntity != null) {
            provider = tileEntity;
            pos = tileEntity.getPos();
        } else if (inventorySlots instanceof ModularStorageItemContainer) {
            ModularStorageItemContainer storageItemContainer = (ModularStorageItemContainer) inventorySlots;
            provider = storageItemContainer.getCraftingGridProvider();
        } else if (inventorySlots instanceof RemoteStorageItemContainer) {
            RemoteStorageItemContainer storageItemContainer = (RemoteStorageItemContainer) inventorySlots;
            provider = storageItemContainer.getCraftingGridProvider();
        } else {
            throw new RuntimeException("Should not happen!");
        }

        craftingGrid.initGui(modBase, network, mc, this, pos, provider, guiLeft, guiTop, xSize, ySize);
        network.sendToServer(new PacketRequestGridSync(pos));
    }

    private Panel setupModePanel() {
        filter = new TextField(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(3, 3, 57, 13)).setTooltips("Name based filter for items")
                .addTextEvent((parent, newText) -> updateSettings());

        viewMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(4, 19, 16, 16)).setTooltips("Control how items are shown", "in the view")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        viewMode.addChoice(VIEW_LIST, "Items are shown in a list view", guiElements, 9 * 16, 16);
        viewMode.addChoice(VIEW_COLUMNS, "Items are shown in columns", guiElements, 10 * 16, 16);
        viewMode.addChoice(VIEW_ICONS, "Items are shown with icons", guiElements, 11 * 16, 16);

        updateTypeModule();

        sortMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(23, 19, 16, 16)).setTooltips("Control how items are sorted", "in the view")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.addChoice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }

        groupMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(42, 19, 16, 16)).setTooltips("If enabled it will show groups", "based on sorting criterium")
                .addChoiceEvent((parent, newChoice) -> updateSettings());
        groupMode.addChoice("Off", "Don't show groups", guiElements, 13 * 16, 0);
        groupMode.addChoice("On", "Show groups", guiElements, 14 * 16, 0);

        amountLabel = new Label(mc, this);
        amountLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        amountLabel.setLayoutHint(new PositionalLayout.PositionalHint(16, 40, 66, 12));
        amountLabel.setTooltips("Amount of stacks / maximum amount");
        amountLabel.setText("?/?");

        compactButton = new Button(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(4, 39, 12, 12)).setText("z").setTooltips("Compact equal stacks")
                .addButtonEvent(parent -> compact());

        if (tileEntity != null) {
            filter.setText(ModularStorageConfiguration.clearSearchOnOpen ? "" : tileEntity.getFilter());
            setViewMode(tileEntity.getViewMode());
            setSortMode(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
        } else {
            ItemStack heldItem = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.hasTagCompound()) {
                NBTTagCompound tagCompound = heldItem.getTagCompound();
                filter.setText(ModularStorageConfiguration.clearSearchOnOpen ? "" : tagCompound.getString("filter"));
                setViewMode(tagCompound.getString("viewMode"));
                setSortMode(tagCompound.getString("sortMode"));
                groupMode.setCurrentChoice(tagCompound.getBoolean("groupMode") ? 1 : 0);
            }
        }

        return new Panel(mc, this).setLayout(new PositionalLayout()).setLayoutHint(new PositionalLayout.PositionalHint(24, ySize-80, 64, 77))
                .setFilledRectThickness(-2)
                .setFilledBackground(StyleConfig.colorListBackground)
                .addChild(filter).addChild(viewMode).addChild(sortMode).addChild(groupMode).addChild(amountLabel).addChild(compactButton);
    }

    private void setSortMode(String sortMode) {
        int idx;
        idx = this.sortMode.findChoice(sortMode);
        if (idx == -1) {
            this.sortMode.setCurrentChoice(0);
        } else {
            this.sortMode.setCurrentChoice(idx);
        }
    }

    private void setViewMode(String viewMode) {
        int idx = this.viewMode.findChoice(viewMode);
        if (idx == -1) {
            this.viewMode.setCurrentChoice(VIEW_LIST);
        } else {
            this.viewMode.setCurrentChoice(idx);
        }
    }

    private void cycleStorage() {
        if (tileEntity != null) {
            sendServerCommand(RFToolsMessages.INSTANCE, ModularStorageTileEntity.CMD_CYCLE);
        } else {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCycleStorage());
        }
    }

    private void compact() {
        if (tileEntity != null) {
            sendServerCommand(RFToolsMessages.INSTANCE, ModularStorageTileEntity.CMD_COMPACT);
        } else {
            RFToolsMessages.INSTANCE.sendToServer(new PacketCompact());
        }
    }

    private void updateSettings() {
        if (tileEntity != null) {
            tileEntity.setSortMode(sortMode.getCurrentChoice());
            tileEntity.setViewMode(viewMode.getCurrentChoice());
            tileEntity.setFilter(filter.getText());
            tileEntity.setGroupMode(groupMode.getCurrentChoiceIndex() == 1);
            sendServerCommand(RFToolsMessages.INSTANCE, ModularStorageTileEntity.CMD_SETTINGS,
                    new Argument("sortMode", sortMode.getCurrentChoice()),
                    new Argument("viewMode", viewMode.getCurrentChoice()),
                    new Argument("filter", filter.getText()),
                    new Argument("groupMode", groupMode.getCurrentChoiceIndex() == 1));
        } else {
            RFToolsMessages.INSTANCE.sendToServer(new PacketUpdateNBTItemStorage(
                    new Argument("sortMode", sortMode.getCurrentChoice()),
                    new Argument("viewMode", viewMode.getCurrentChoice()),
                    new Argument("filter", filter.getText()),
                    new Argument("groupMode", groupMode.getCurrentChoiceIndex() == 1)));
        }
    }

    private Slot findEmptySlot() {
        for (Object slotObject : inventorySlots.inventorySlots) {
            Slot slot = (Slot) slotObject;
            // Skip the first two slots if we are on a modular storage block.
            if (tileEntity != null && slot.getSlotIndex() < ModularStorageContainer.SLOT_STORAGE) {
                continue;
            }
            if ((!slot.getHasStack()) || slot.getStack().getCount() == 0) {
                return slot;
            }
        }
        return null;
    }

    @Override
    public boolean isMouseOverSlot(Slot slotIn, int x, int y) {
        if (slotIn.inventory instanceof ModularStorageTileEntity || slotIn.inventory instanceof ModularStorageItemInventory
                || slotIn.inventory instanceof RemoteStorageItemInventory) {
            Widget widget = window.getToplevel().getWidgetAtPosition(x, y);
            if (widget instanceof BlockRender) {
                Object userObject = widget.getUserObject();
                if (userObject instanceof Integer) {
                    Integer slotIndex = (Integer) userObject;
                    return slotIndex == slotIn.getSlotIndex();
                }
            } else {
                return super.isMouseOverSlot(slotIn, x, y);
            }
            return false;
        } else {
            return super.isMouseOverSlot(slotIn, x, y);
        }
    }

    @Override
    public Slot getSlotAtPosition(int x, int y) {
        Widget widget = window.getToplevel().getWidgetAtPosition(x, y);
        if (widget != null) {
            Object userObject = widget.getUserObject();
            if (userObject instanceof Integer) {
                Integer slotIndex = (Integer) userObject;
                if (slotIndex != -1) {
                    return inventorySlots.getSlot(slotIndex);
                } else {
                    return findEmptySlot();
                }
            }
        }

        return super.getSlotAtPosition(x, y);
    }

    private void dumpClasses(String name, Object o) {
        Logging.log(name + ":" + o.getClass().getCanonicalName());
        Class<?>[] classes = o.getClass().getClasses();
        for (Class<?> a : classes) {
            Logging.log("        " + a.getCanonicalName());
        }
        Logging.log("        Super:" + o.getClass().getGenericSuperclass());
        for (Type type : o.getClass().getGenericInterfaces()) {
            Logging.log("        type:" + type.getClass().getCanonicalName());
        }

    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            Slot slot = getSlotAtPosition(x, y);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                Item item = stack.getItem();
                if (item instanceof ItemBlock) {
                    Block block = ((ItemBlock) item).getBlock();
                    dumpClasses("Block", block);
                } else {
                    dumpClasses("Item", item);
                }
            }
        }
        super.mouseClicked(x, y, button);
        craftingGrid.getWindow().mouseClicked(x, y, button);
        if (button == 1) {
            Slot slot = getSlotAtPosition(x, y);
            if (slot instanceof GhostOutputSlot) {
                if (tileEntity != null) {
                    sendServerCommand(RFToolsMessages.INSTANCE, ModularStorageTileEntity.CMD_CLEARGRID);
                } else {
                    RFToolsMessages.INSTANCE.sendToServer(new PacketClearGrid());
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        craftingGrid.getWindow().handleMouseInput();
    }

    @Override
    protected void mouseReleased(int x, int y, int state) {
        super.mouseReleased(x, y, state);
        craftingGrid.getWindow().mouseMovedOrUp(x, y, state);
    }

    private void updateList() {
        itemList.removeChildren();

        if (tileEntity != null && !inventorySlots.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getHasStack()) {
            amountLabel.setText("(empty)");
            compactButton.setEnabled(false);
            cycleButton.setEnabled(false);
            return;
        }

        cycleButton.setEnabled(isTabletWithRemote() || isRemote());

        String filterText = filter.getText().toLowerCase().trim();

        String view = viewMode.getCurrentChoice();
        int numcolumns;
        int labelWidth;
        int spacing;
        if (VIEW_LIST.equals(view)) {
            numcolumns = 1;
            labelWidth = 210;
            spacing = 5;
        } else if (VIEW_COLUMNS.equals(view)) {
            numcolumns = 2;
            labelWidth = 86;
            spacing = 5;
        } else {
            numcolumns = 12;
            labelWidth = 0;
            spacing = 3;
        }

        int max;
        List<Pair<ItemStack,Integer>> items = new ArrayList<>();
        if (tileEntity != null) {
            for (int i = ModularStorageContainer.SLOT_STORAGE; i < tileEntity.getSizeInventory(); i++) {
                ItemStack stack = tileEntity.getStackInSlot(i);
                if (!stack.isEmpty()) {
                    String displayName = stack.getDisplayName();
                    if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
                        items.add(Pair.of(stack, i));
                    }
                }
            }
            max = tileEntity.getSizeInventory() - ModularStorageContainer.SLOT_STORAGE;
        } else {
            // Also works for ModularStorageItemContainer
            for (int i = 0; i < RemoteStorageItemContainer.MAXSIZE_STORAGE ; i++) {
                Slot slot = inventorySlots.getSlot(i);
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    String displayName = stack.getDisplayName();
                    if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
                        items.add(Pair.of(stack, i));
                    }
                }
            }
            ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
            if (!heldItem.isEmpty() && heldItem.hasTagCompound()) {
                max = heldItem.getTagCompound().getInteger("maxSize");
            } else {
                max = 0;
            }
        }
        amountLabel.setText(items.size() + "/" + max);
        compactButton.setEnabled(max > 0);

        int sort = getCurrentSortMode();

        boolean dogroups = groupMode.getCurrentChoiceIndex() == 1;

        ItemSorter itemSorter = typeModule.getSorters().get(sort);
        Collections.sort(items, itemSorter.getComparator());

        Pair<Panel,Integer> currentPos = MutablePair.of(null, 0);
        Pair<ItemStack, Integer> prevItem = null;
        for (Pair<ItemStack, Integer> item : items) {
            currentPos = addItemToList(item.getKey(), itemList, currentPos, numcolumns, labelWidth, spacing, item.getValue(),
                    dogroups && (prevItem == null || !itemSorter.isSameGroup(prevItem, item)), itemSorter.getGroupName(item));
            prevItem = item;
        }

        int newfirst = -1;
        if (itemList.getCountSelected() == 0) {
            if (itemList.getBounds() != null) {
                itemList.setFirstSelected(0);
                newfirst = itemList.getChildCount() - itemList.getCountSelected();
                if (newfirst < 0) {
                    newfirst = 0;
                }
            }
        } else if (itemList.getFirstSelected() > (itemList.getChildCount() - itemList.getCountSelected())) {
            newfirst = itemList.getChildCount() - itemList.getCountSelected();
        }
        if (newfirst >= 0) {
            itemList.setFirstSelected(newfirst);
        }
    }

    private boolean isRemote() {
        ItemStack stack = inventorySlots.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getStack();
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE;
    }

    private boolean isTabletWithRemote() {
        if (tileEntity != null) {
            return false;
        }
        ItemStack heldItem = mc.player.getHeldItem(EnumHand.MAIN_HAND);
        if (!heldItem.isEmpty() && heldItem.hasTagCompound()) {
            int storageType = heldItem.getTagCompound().getInteger("childDamage");
            return storageType == StorageModuleItem.STORAGE_REMOTE;
        } else {
            return false;
        }
    }

    private int getCurrentSortMode() {
        updateTypeModule();
        String sortName = sortMode.getCurrentChoice();
        sortMode.clear();
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.addChoice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }
        int sort = sortMode.findChoice(sortName);
        if (sort == -1) {
            sort = 0;
        }
        sortMode.setCurrentChoice(sort);
        return sort;
    }

    private void updateTypeModule() {
        if (tileEntity != null) {
            ItemStack typeStack = tileEntity.getStackInSlot(ModularStorageContainer.SLOT_TYPE_MODULE);
            if (typeStack.isEmpty() || !(typeStack.getItem() instanceof TypeModule)) {
                typeModule = new DefaultTypeModule();
            } else {
                typeModule = (TypeModule) typeStack.getItem();
            }
        } else {
            typeModule = new DefaultTypeModule();
        }
    }

    private Pair<Panel,Integer> addItemToList(ItemStack stack, WidgetList itemList, Pair<Panel,Integer> currentPos, int numcolumns, int labelWidth, int spacing, int slot,
                                              boolean newgroup, String groupName) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns || (newgroup && groupName != null)) {
            if (newgroup && groupName != null) {
                AbstractWidget groupLabel = new Label(mc, this).setText(groupName).setColor(ModularStorageConfiguration.groupForeground)
                        .setColor(StyleConfig.colorTextInListNormal)
                        .setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setFilledBackground(ModularStorageConfiguration.groupBackground).setDesiredHeight(10)
                        .setDesiredWidth(231);
                itemList.addChild(new Panel(mc, this).setLayout(new HorizontalLayout().setHorizontalMargin(2).setVerticalMargin(0)).setDesiredHeight(10).addChild(groupLabel));
            }

            panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(spacing)).setDesiredHeight(12).setUserObject(new Integer(-1)).setDesiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.addChild(panel);
        }
        BlockRender blockRender = new BlockRender(mc, this).setRenderItem(stack).setUserObject(new Integer(slot)).setOffsetX(-1).setOffsetY(-1);
        panel.addChild(blockRender);
        if (labelWidth > 0) {
            String displayName;
            if (labelWidth > 100) {
                displayName = typeModule.getLongLabel(stack);
            } else {
                displayName = typeModule.getShortLabel(stack);
            }
            AbstractWidget label = new Label(mc, this).setText(displayName).setColor(StyleConfig.colorTextInListNormal).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(labelWidth).setUserObject(new Integer(-1));
            panel.addChild(label);
        }
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!window.keyTyped(typedChar, keyCode)) {
            if (typedChar >= '1' && typedChar <= '9') {
                return;
            }
            super.keyTyped(typedChar, keyCode);
        }

        craftingGrid.getWindow().keyTyped(typedChar, keyCode);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateList();

        if (tileEntity != null) {
            viewMode.setCurrentChoice(tileEntity.getViewMode());
            sortMode.setCurrentChoice(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
            filter.setText(tileEntity.getFilter());
        }

        drawWindow();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i1, int i2) {
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        List<String> tooltips = craftingGrid.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, mc.fontRenderer);
        }

        super.drawGuiContainerForegroundLayer(i1, i2);
    }

    @Override
    protected void drawWindow() {
        super.drawWindow();
        craftingGrid.draw();
    }
}
