package mcjty.rftools.blocks.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.events.ButtonEvent;
import mcjty.gui.events.ChoiceEvent;
import mcjty.gui.events.TextEvent;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Button;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.storage.modules.DefaultTypeModule;
import mcjty.rftools.blocks.storage.modules.TypeModule;
import mcjty.rftools.blocks.storage.sorters.ItemSorter;
import mcjty.rftools.items.storage.StorageModuleItem;
import mcjty.rftools.network.Argument;
import mcjty.rftools.network.PacketHandler;
import mcjty.rftools.network.PacketUpdateNBTItem;
import mcjty.varia.Logging;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class GuiModularStorage extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int STORAGE_WIDTH = 256;
    public static final int STORAGE_HEIGHT0 = 236;
    public static final int STORAGE_HEIGHT1 = 320;
    public static final int STORAGE_HEIGHT2 = 490;

    public static final String VIEW_LIST = "list";
    public static final String VIEW_COLUMNS = "columns";
    public static final String VIEW_ICONS = "icons";

    private TypeModule typeModule;

    private static final ResourceLocation iconLocationTop = new ResourceLocation(RFTools.MODID, "textures/gui/modularstorageTop.png");
    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/modularstorage.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private WidgetList itemList;
    private Slider slider;
    private TextField filter;
    private ImageChoiceLabel viewMode;
    private ImageChoiceLabel sortMode;
    private ImageChoiceLabel groupMode;
    private Label amountLabel;
    private Button cycleButton;
    private Button compactButton;

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
        super(modularStorageTileEntity, container, RFTools.GUI_MANUAL_MAIN, "storage");
        xSize = STORAGE_WIDTH;

        int width = Minecraft.getMinecraft().displayWidth;
        int height = Minecraft.getMinecraft().displayHeight;
        ScaledResolution scaledresolution = new ScaledResolution(Minecraft.getMinecraft(), width, height);
        width = scaledresolution.getScaledWidth();
        height = scaledresolution.getScaledHeight();

        if (height > 510) {
            ySize = STORAGE_HEIGHT2;
        } else  if (height > 340) {
            ySize = STORAGE_HEIGHT1;
        } else {
            ySize = STORAGE_HEIGHT0;
        }

        for (Object o : container.inventorySlots) {
            Slot slot = (Slot) o;
            slot.yDisplayPosition += ySize - STORAGE_HEIGHT0;
        }

    }

    @Override
    public void initGui() {
        super.initGui();

        itemList = createStyledList().setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 235, ySize-89)).setNoSelectionMode(true).setUserObject(new Integer(-1)).
                setLeftMargin(0).setRowheight(-1);
        slider = new Slider(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(241, 3, 11, ySize-89)).setDesiredWidth(11).setVertical().setScrollable(itemList);

        filter = new TextField(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, ySize-79, 80, 12)).setTooltips("Name based filter for items").addTextEvent(new TextEvent() {
            @Override
            public void textChanged(Widget parent, String newText) {
                updateSettings();
            }
        });

        viewMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, ySize-66, 16, 16)).setTooltips("Control how items are shown", "in the view").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        viewMode.addChoice(VIEW_LIST, "Items are shown in a list view", guiElements, 9 * 16, 16);
        viewMode.addChoice(VIEW_COLUMNS, "Items are shown in columns", guiElements, 10 * 16, 16);
        viewMode.addChoice(VIEW_ICONS, "Items are shown with icons", guiElements, 11 * 16, 16);

        updateTypeModule();

        sortMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(28, ySize-66, 16, 16)).setTooltips("Control how items are sorted", "in the view").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        for (ItemSorter sorter : typeModule.getSorters()) {
            sortMode.addChoice(sorter.getName(), sorter.getTooltip(), guiElements, sorter.getU(), sorter.getV());
        }

        groupMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(48, ySize-66, 16, 16)).setTooltips("If enabled it will show groups", "based on sorting criterium").addChoiceEvent(new ChoiceEvent() {
            @Override
            public void choiceChanged(Widget parent, String newChoice) {
                updateSettings();
            }
        });
        groupMode.addChoice("Off", "Don't show groups", guiElements, 13 * 16, 0);
        groupMode.addChoice("On", "Show groups", guiElements, 14 * 16, 0);

        amountLabel = new Label(mc, this);
        amountLabel.setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT);
        amountLabel.setLayoutHint(new PositionalLayout.PositionalHint(22, ySize-48, 66, 12));
        amountLabel.setTooltips("Amount of stacks / maximum amount");
        amountLabel.setText("? / ?");

        compactButton = new Button(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, ySize-48, 12, 12)).setText("z").setTooltips("Compact equal stacks").addButtonEvent(new ButtonEvent() {
            @Override
            public void buttonClicked(Widget parent) {
                compact();
            }
        });

        cycleButton = new Button(mc, this).setText("C").setTooltips("Cycle to the next storage module").setLayoutHint(new PositionalLayout.PositionalHint(66, ySize-66, 16, 16)).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        cycleStorage();
                    }
                });

        if (tileEntity != null) {
            filter.setText(tileEntity.getFilter());
            setViewMode(tileEntity.getViewMode());
            setSortMode(tileEntity.getSortMode());
            groupMode.setCurrentChoice(tileEntity.isGroupMode() ? 1 : 0);
        } else {
            NBTTagCompound tagCompound = Minecraft.getMinecraft().thePlayer.getHeldItem().getTagCompound();
            filter.setText(tagCompound.getString("filter"));
            setViewMode(tagCompound.getString("viewMode"));
            setSortMode(tagCompound.getString("sortMode"));
            groupMode.setCurrentChoice(tagCompound.getBoolean("groupMode") ? 1 : 0);
        }

        Panel toplevel = new Panel(mc, this).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider).addChild(filter).
                addChild(viewMode).addChild(sortMode).addChild(groupMode).addChild(amountLabel).addChild(compactButton).addChild(cycleButton);

        toplevel.setBackgrounds(iconLocationTop, iconLocation);
        toplevel.setBackgroundLayout(false, ySize-STORAGE_HEIGHT0+2);

        if (tileEntity == null) {
            // We must hide two slots.
            ImageLabel hideLabel = new ImageLabel(mc, this);
            hideLabel.setLayoutHint(new PositionalLayout.PositionalHint(4, ySize-23, 62, 21));
            hideLabel.setImage(guiElements, 32, 32);
            toplevel.addChild(hideLabel);
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        window.setTextFocus(filter);
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
            sendServerCommand(ModularStorageTileEntity.CMD_CYCLE);
        } else {
            PacketHandler.INSTANCE.sendToServer(new PacketCycleStorage());
        }
    }

    private void compact() {
        if (tileEntity != null) {
            sendServerCommand(ModularStorageTileEntity.CMD_COMPACT);
        } else {
            PacketHandler.INSTANCE.sendToServer(new PacketCompact());
        }
    }

    private void updateSettings() {
        if (tileEntity != null) {
            sendServerCommand(ModularStorageTileEntity.CMD_SETTINGS,
                    new Argument("sortMode", sortMode.getCurrentChoice()),
                    new Argument("viewMode", viewMode.getCurrentChoice()),
                    new Argument("filter", filter.getText()),
                    new Argument("groupMode", groupMode.getCurrentChoiceIndex() == 1));
        } else {
            PacketHandler.INSTANCE.sendToServer(new PacketUpdateNBTItem(
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
            if ((!slot.getHasStack()) || slot.getStack().stackSize == 0) {
                return slot;
            }
        }
        return null;
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
    protected void mouseClicked(int x, int y, int button) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
            Slot slot = getSlotAtPosition(x, y);
            if (slot != null && slot.getHasStack()) {
                ItemStack stack = slot.getStack();
                Item item = stack.getItem();
                if (item instanceof ItemBlock) {
                    Block block = ((ItemBlock) item).field_150939_a;
                    dumpClasses("Block", block);
                } else {
                    dumpClasses("Item", item);
                }
            }
        }
        super.mouseClicked(x, y, button);
    }

    private void updateList() {
        itemList.removeChildren();

        if (tileEntity != null && !inventorySlots.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getHasStack()) {
            amountLabel.setText("(no storage)");
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
        List<Pair<ItemStack,Integer>> items = new ArrayList<Pair<ItemStack, Integer>>();
        if (tileEntity != null) {
            for (int i = ModularStorageContainer.SLOT_STORAGE; i < tileEntity.getSizeInventory(); i++) {
                ItemStack stack = tileEntity.getStackInSlot(i);
                if (stack != null && stack.stackSize > 0) {
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
                if (stack != null && stack.stackSize > 0) {
                    String displayName = stack.getDisplayName();
                    if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
                        items.add(Pair.of(stack, i));
                    }
                }
            }
            max = mc.thePlayer.getHeldItem().getTagCompound().getInteger("maxSize");
        }
        amountLabel.setText(items.size() + " / " + max);
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
    }

    private boolean isRemote() {
        ItemStack stack = inventorySlots.getSlot(ModularStorageContainer.SLOT_STORAGE_MODULE).getStack();
        if (stack == null) {
            return false;
        }
        return stack.getItemDamage() == StorageModuleItem.STORAGE_REMOTE;
    }

    private boolean isTabletWithRemote() {
        if (tileEntity != null) {
            return false;
        }
        int storageType = mc.thePlayer.getHeldItem().getTagCompound().getInteger("childDamage");
        return storageType == StorageModuleItem.STORAGE_REMOTE;
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
            if (typeStack == null || typeStack.stackSize == 0 || !(typeStack.getItem() instanceof TypeModule)) {
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
                itemList.addChild(new Label(mc, this).setText(groupName).setColor(ModularStorageConfiguration.groupForeground).
                        setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setFilledBackground(ModularStorageConfiguration.groupBackground).setDesiredHeight(10));
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
            AbstractWidget label = new Label(mc, this).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(labelWidth).setUserObject(new Integer(-1));
            panel.addChild(label);
        }
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (!window.keyTyped(typedChar, keyCode)) {
            if (typedChar >= '1' && typedChar <= '9') {
                return;
            }
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateList();
        drawWindow();
    }
}
