package mcjty.rftools.blocks.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.layout.HorizontalAlignment;
import mcjty.gui.layout.HorizontalLayout;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.*;
import mcjty.gui.widgets.Label;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.TextField;
import mcjty.rftools.RFTools;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class GuiModularStorage extends GenericGuiContainer<ModularStorageTileEntity> {
    public static final int STORAGE_WIDTH = 256;
    public static final int STORAGE_HEIGHT = 236;

    public static final String VIEW_LIST = "list";
    public static final String VIEW_COLUMNS = "columns";
    public static final String VIEW_ICONS = "icons";

    public static final String SORT_NAME = "name";
    public static final String SORT_COUNT = "count";

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/modularstorage.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private WidgetList itemList;
    private Slider slider;
    private TextField filter;
    private ImageChoiceLabel viewMode;
    private ImageChoiceLabel sortMode;

    public GuiModularStorage(ModularStorageTileEntity modularStorageTileEntity, ModularStorageContainer container) {
        super(modularStorageTileEntity, container);

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        itemList = new WidgetList(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(5, 3, 235, 147)).setNoSelectionMode(true).setUserObject(new Integer(-1)).
                setFilledBackground(0xff8090a0).setLeftMargin(0);
        slider = new Slider(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(241, 3, 11, 147)).setDesiredWidth(11).setVertical().setScrollable(itemList);

        filter = new TextField(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, 157, 80, 12)).setTooltips("Name based filter for items");

        viewMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(8, 170, 16, 16)).setTooltips("Control how items are shown", "in the view");
        viewMode.addChoice(VIEW_LIST, "Items are shown in a list view", guiElements, 9 * 16, 16);
        viewMode.addChoice(VIEW_COLUMNS, "Items are shown in columns", guiElements, 10 * 16, 16);
        viewMode.addChoice(VIEW_ICONS, "Items are shown with icons", guiElements, 11 * 16, 16);
        viewMode.setCurrentChoice(VIEW_LIST);

        sortMode = new ImageChoiceLabel(mc, this).setLayoutHint(new PositionalLayout.PositionalHint(28, 170, 16, 16)).setTooltips("Control how items are sorted", "in the view");
        sortMode.addChoice(SORT_NAME, "Sort on name", guiElements, 12 * 16, 16);
        sortMode.addChoice(SORT_COUNT, "Sort on count", guiElements, 13 * 16, 16);
        sortMode.setCurrentChoice(SORT_NAME);

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(itemList).addChild(slider).addChild(filter).addChild(viewMode).addChild(sortMode);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private Slot findEmptySlot() {
        for (Object slotObject : inventorySlots.inventorySlots) {
            Slot slot = (Slot) slotObject;
            if (!slot.getHasStack()) {
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

    private void updateList() {
        itemList.removeChildren();

        if (!inventorySlots.getSlot(0).getHasStack()) {
            return;
        }

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

        String sort = sortMode.getCurrentChoice();

        List<Pair<ItemStack,Integer>> items = new ArrayList<Pair<ItemStack, Integer>>();
        for (int i = 2 ; i < tileEntity.getSizeInventory() ; i++) {
            ItemStack stack = tileEntity.getStackInSlot(i);
            if (stack != null && stack.stackSize > 0) {
                String displayName = stack.getDisplayName();
                if (filterText.isEmpty() || displayName.toLowerCase().contains(filterText)) {
                    items.add(Pair.of(stack, i));
                }
            }
        }

        if (SORT_NAME.equals(sort)) {
            Collections.sort(items, new Comparator<Pair<ItemStack, Integer>>() {
                @Override
                public int compare(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
                    String name1 = o1.getLeft().getDisplayName().toLowerCase();
                    String name2 = o2.getLeft().getDisplayName().toLowerCase();
                    return name1.compareTo(name2);
                }
            });
        } else {
            Collections.sort(items, new Comparator<Pair<ItemStack, Integer>>() {
                @Override
                public int compare(Pair<ItemStack, Integer> o1, Pair<ItemStack, Integer> o2) {
                    Integer c1 = o1.getLeft().stackSize;
                    Integer c2 = o2.getLeft().stackSize;
                    return c2.compareTo(c1);
                }
            });
        }

        Pair<Panel,Integer> currentPos = MutablePair.of(null, 0);
        for (Pair<ItemStack, Integer> item : items) {
            currentPos = addItemToList(item.getKey(), itemList, currentPos, numcolumns, labelWidth, spacing, item.getValue());
        }
    }

    private Pair<Panel,Integer> addItemToList(ItemStack stack, WidgetList itemList, Pair<Panel,Integer> currentPos, int numcolumns, int labelWidth, int spacing, int slot) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns) {
            panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(spacing)).setDesiredHeight(12).setUserObject(new Integer(-1));
            currentPos = MutablePair.of(panel, 0);
            itemList.addChild(panel);
        }
        String displayName = stack.getDisplayName();
        BlockRender blockRender = new BlockRender(mc, this).setRenderItem(stack).setUserObject(new Integer(slot));
        panel.addChild(blockRender);
        if (labelWidth > 0) {
            AbstractWidget label = new Label(mc, this).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(labelWidth).setUserObject(new Integer(-1));
            panel.addChild(label);
        }
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateList();
        window.draw();
    }
}
