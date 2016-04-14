package mcjty.rftools.blocks.storagemonitor;

import mcjty.lib.base.StyleConfig;
import mcjty.lib.container.EmptyContainer;
import mcjty.lib.container.GenericGuiContainer;
import mcjty.lib.entity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.events.DefaultSelectionEvent;
import mcjty.lib.gui.layout.HorizontalAlignment;
import mcjty.lib.gui.layout.HorizontalLayout;
import mcjty.lib.gui.layout.VerticalLayout;
import mcjty.lib.gui.widgets.*;
import mcjty.lib.gui.widgets.Button;
import mcjty.lib.gui.widgets.Label;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.network.Argument;
import mcjty.lib.network.clientinfo.PacketGetInfoFromServer;
import mcjty.lib.varia.BlockPosTools;
import mcjty.lib.varia.Logging;
import mcjty.rftools.BlockInfo;
import mcjty.rftools.RFTools;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class GuiStorageScanner extends GenericGuiContainer<StorageScannerTileEntity> {
    public static final int STORAGE_MONITOR_WIDTH = 256;
    public static final int STORAGE_MONITOR_HEIGHT = 224;

    private WidgetList storageList;
    private WidgetList itemList;
    private EnergyBar energyBar;
    private EnergyBar progressBar;
    private ScrollableLabel radiusLabel;
    private Button scanButton;

    private int listDirty = 0;

    // From server: all the positions with inventories
    public static List<BlockPos> fromServer_inventories = new ArrayList<>();
    // From server: all the positions with inventories matching the search
    public static Set<BlockPos> fromServer_foundInventories = new HashSet<>();
    // From server: the contents of an inventory
    public static List<ItemStack> fromServer_inventory = new ArrayList<>();

    public GuiStorageScanner(StorageScannerTileEntity storageScannerTileEntity, EmptyContainer storageScannerContainer) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, storageScannerTileEntity, storageScannerContainer, RFTools.GUI_MANUAL_MAIN, "stomon");
        GenericEnergyStorageTileEntity.setCurrentRF(storageScannerTileEntity.getEnergyStored(EnumFacing.DOWN));

        xSize = STORAGE_MONITOR_WIDTH;
        ySize = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();


        int maxEnergyStored = tileEntity.getMaxEnergyStored(EnumFacing.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setVertical().setDesiredWidth(10).setDesiredHeight(84).setMaxValue(maxEnergyStored).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        storageList = new WidgetList(mc, this).addSelectionEvent(new DefaultSelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                getInventoryOnServer();
            }

            @Override
            public void doubleClick(Widget parent, int index) {
                hilightSelectedContainer(index);
            }
        });

        Slider storageListSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(storageList);

        Panel topPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(1)).
                setDesiredHeight(90).
                addChild(energyBar).
                addChild(storageList).addChild(storageListSlider);

        itemList = new WidgetList(mc, this);
        Slider itemListSlider = new Slider(mc, this).setDesiredWidth(10).setVertical().setScrollable(itemList);
        Panel midPanel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(1).setHorizontalMargin(1)).
                addChild(itemList).addChild(itemListSlider);

        scanButton = new Button(mc, this).
                setText("Scan").
                setDesiredWidth(50).
                setDesiredHeight(14).
                addButtonEvent(parent -> RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID,
                                                                                          new InventoriesInfoPacketServer(tileEntity.getWorld(), tileEntity.getPos(), true)))).
                setTooltips("Start/stop a scan of", "all storage units", "in radius");
        progressBar = new EnergyBar(mc, this).setShowText(false).
                setEnergyOnColor(0xff0022ee).setEnergyOffColor(0xff111163).setSpacerColor(0xff000043).
                setHorizontal().setMaxValue(100).setDesiredWidth(30).setValue(0);
        radiusLabel = new ScrollableLabel(mc, this).
                addValueEvent((parent, newValue) -> changeRadius(newValue)).
                setRealMinimum(1).
                setRealMaximum(20).
                setDesiredWidth(30);
        radiusLabel.setRealValue(tileEntity.getRadius());

        TextField textField = new TextField(mc, this).addTextEvent((parent, newText) -> {
            storageList.clearHilightedRows();
            fromServer_foundInventories.clear();
            startSearch(newText);
        });
        Panel searchPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(18).addChild(new Label(mc, this).setText("Search:")).addChild(textField);

        Slider radiusSlider = new Slider(mc, this).
                setHorizontal().
                setTooltips("Radius of scan").
                setMinimumKnobSize(12).
                setScrollable(radiusLabel);
        Panel scanPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(18).addChild(scanButton).addChild(progressBar).addChild(radiusSlider).addChild(radiusLabel);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout().setSpacing(1).setVerticalMargin(3)).addChild(topPanel).addChild(midPanel).addChild(searchPanel).addChild(scanPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);

        Keyboard.enableRepeatEvents(true);

        tileEntity.requestRfFromServer(RFTools.MODID);
    }

    private void hilightSelectedContainer(int index) {
        if (index == -1) {
            return;
        }
        BlockPos c = fromServer_inventories.get(index);
        RFTools.instance.clientInfo.hilightBlock(c, System.currentTimeMillis()+1000* StorageScannerConfiguration.hilightTime);
        Logging.message(mc.thePlayer, "The inventory is now highlighted");
        mc.thePlayer.closeScreen();
    }

    private void changeRadius(int r) {
        sendServerCommand(RFToolsMessages.INSTANCE, StorageScannerTileEntity.CMD_SETRADIUS, new Argument("r", r));
    }

    private void startSearch(String text) {
        if (!text.isEmpty()) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID,
                                                                              new SearchItemsInfoPacketServer(tileEntity.getWorld(), tileEntity.getPos(), text)));
        }
    }

    private void getInventoryOnServer() {
        BlockPos c = getSelectedContainerPos();
        if (c != null) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID,
                                                                              new GetContentsInfoPacketServer(tileEntity.getWorld(), tileEntity.getPos(), c)));
        }
    }

    private BlockPos getSelectedContainerPos() {
        int selected = storageList.getSelected();
        if (selected != -1) {
            if (selected < fromServer_inventories.size()) {
                return fromServer_inventories.get(selected);
            }
        }
        return null;
    }

    private void requestListsIfNeeded() {
        listDirty--;
        if (listDirty <= 0) {
            RFToolsMessages.INSTANCE.sendToServer(new PacketGetInfoFromServer(RFTools.MODID,
                                                                              new InventoriesInfoPacketServer(tileEntity.getWorld(), tileEntity.getPos(), false)));
            listDirty = 20;
        }
    }

    private void updateContentsList() {
        itemList.removeChildren();

        Pair<Panel,Integer> currentPos = MutablePair.of(null, 0);
        int numcolumns = 11;
        int spacing = 3;

        for (ItemStack item : fromServer_inventory) {
            currentPos = addItemToList(item, itemList, currentPos, numcolumns, spacing);
        }
    }

    private Pair<Panel,Integer> addItemToList(ItemStack stack, WidgetList itemList, Pair<Panel,Integer> currentPos, int numcolumns, int spacing) {
        Panel panel = currentPos.getKey();
        if (panel == null || currentPos.getValue() >= numcolumns) {
            panel = new Panel(mc, this).setLayout(new HorizontalLayout().setSpacing(spacing)).setDesiredHeight(12).setUserObject(new Integer(-1)).setDesiredHeight(16);
            currentPos = MutablePair.of(panel, 0);
            itemList.addChild(panel);
        }
        BlockRender blockRender = new BlockRender(mc, this).setRenderItem(stack).setUserObject(new Integer(0)).setOffsetX(-1).setOffsetY(-1);
        panel.addChild(blockRender);
        currentPos.setValue(currentPos.getValue() + 1);
        return currentPos;
    }




    private void updateStorageList() {
        storageList.removeChildren();
        for (BlockPos c : fromServer_inventories) {
            IBlockState state = mc.theWorld.getBlockState(c);
            Block block = state.getBlock();
            String displayName;
            if (mc.theWorld.isAirBlock(c)) {
                displayName = "[REMOVED]";
                block = null;
            } else {
                displayName = BlockInfo.getReadableName(state);
            }

            Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
            panel.addChild(new BlockRender(mc, this).setRenderItem(block));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(90));
            panel.addChild(new Label(mc, this).setColor(StyleConfig.colorTextInListNormal).setDynamic(true).setText(BlockPosTools.toString(c)));
            storageList.addChild(panel);
        }


        storageList.clearHilightedRows();
        int i = 0;
        for (BlockPos c : fromServer_inventories) {
            if (fromServer_foundInventories.contains(c)) {
                storageList.addHilightedRow(i);
            }
            i++;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateStorageList();
        updateContentsList();
        requestListsIfNeeded();
        drawWindow();
        int currentRF = GenericEnergyStorageTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
