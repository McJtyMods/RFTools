package com.mcjty.rftools.blocks.storagemonitor;

import com.mcjty.entity.SyncedValueList;
import com.mcjty.gui.Window;
import com.mcjty.gui.events.ButtonEvent;
import com.mcjty.gui.events.SelectionEvent;
import com.mcjty.gui.events.ValueEvent;
import com.mcjty.gui.layout.HorizontalAlignment;
import com.mcjty.gui.layout.HorizontalLayout;
import com.mcjty.gui.layout.VerticalLayout;
import com.mcjty.gui.widgets.*;
import com.mcjty.gui.widgets.Button;
import com.mcjty.gui.widgets.Label;
import com.mcjty.gui.widgets.Panel;
import com.mcjty.rftools.BlockInfo;
import com.mcjty.rftools.network.PacketHandler;
import com.mcjty.varia.Coordinate;
import net.minecraft.block.Block;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;


public class GuiStorageScanner extends GuiContainer {
    public static final int STORAGE_MONITOR_WIDTH = 256;
    public static final int STORAGE_MONITOR_HEIGHT = 224;

    private Window window;
    private WidgetList storageList;
    private WidgetList itemList;
    private EnergyBar energyBar;
    private EnergyBar progressBar;
    private ScrollableLabel radiusLabel;
    private Button scanButton;
    private final StorageScannerTileEntity storageScannerTileEntity;
    private int clientVersion = -1;

    public GuiStorageScanner(StorageScannerTileEntity storageScannerTileEntity, StorageScannerContainer storageScannerContainer) {
        super(storageScannerContainer);
        this.storageScannerTileEntity = storageScannerTileEntity;
        storageScannerTileEntity.setOldRF(-1);
        storageScannerTileEntity.setCurrentRF(storageScannerTileEntity.getEnergyStored(ForgeDirection.DOWN));

        xSize = STORAGE_MONITOR_WIDTH;
        ySize = STORAGE_MONITOR_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = storageScannerTileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setFilledRectThickness(1).setVertical().setDesiredWidth(10).setDesiredHeight(60).setMaxValue(maxEnergyStored).setShowText(false);
        energyBar.setValue(storageScannerTileEntity.getCurrentRF());

        storageList = new WidgetList(mc, this).setRowheight(16).addSelectionEvent(new SelectionEvent() {
            @Override
            public void select(Widget parent, int index) {
                itemList.removeChildren();
                storageScannerTileEntity.clearShowingItems();
                getInventoryOnServer();
            }
        }).setFilledRectThickness(1);
        Slider storageListSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(storageList);

        Panel topPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                setDesiredHeight(90).
                addChild(energyBar).
                addChild(storageList).addChild(storageListSlider);

        itemList = new WidgetList(mc, this).setRowheight(16).setFilledRectThickness(1);
        Slider itemListSlider = new Slider(mc, this).setDesiredWidth(15).setVertical().setScrollable(itemList);
        Panel midPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).
                addChild(itemList).addChild(itemListSlider);

        scanButton = new Button(mc, this).
                setText("Scan").
                setDesiredWidth(50).
                setDesiredHeight(16).
                addButtonEvent(new ButtonEvent() {
                    @Override
                    public void buttonClicked(Widget parent) {
                        startStopScan();
                    }
                }).
                setTooltips("Start/stop a scan of", "all storage units", "in radius");
        progressBar = new EnergyBar(mc, this).setShowText(false).
                setColor1(0xFF777777).setColor2(0xFF555555).
                setHorizontal().setMaxValue(100).setDesiredWidth(30).setValue(0);
        radiusLabel = new ScrollableLabel(mc, this).
                addValueEvent(new ValueEvent() {
                    @Override
                    public void valueChanged(Widget parent, int newValue) {
                        changeRadius(newValue);
                    }
                }).
                setRealMinimum(1).
                setRealMaximum(20).
                setDesiredWidth(30);
        radiusLabel.setRealValue(storageScannerTileEntity.getRadius());

        Slider radiusSlider = new Slider(mc, this).
                setHorizontal().
                setTooltips("Radius of scan").
                setScrollable(radiusLabel);
        Panel bottomPanel = new Panel(mc, this).setLayout(new HorizontalLayout()).setDesiredHeight(20).addChild(scanButton).addChild(progressBar).addChild(radiusSlider).addChild(radiusLabel);

        Widget toplevel = new Panel(mc, this).setFilledRectThickness(2).setLayout(new VerticalLayout()).addChild(topPanel).addChild(midPanel).addChild(bottomPanel);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void changeRadius(int r) {
        PacketHandler.INSTANCE.sendToServer(new PacketSetRadius(storageScannerTileEntity.xCoord, storageScannerTileEntity.yCoord, storageScannerTileEntity.zCoord, r));
    }

    private void startStopScan() {
        PacketHandler.INSTANCE.sendToServer(new PacketStartScan(storageScannerTileEntity.xCoord, storageScannerTileEntity.yCoord, storageScannerTileEntity.zCoord,
                !storageScannerTileEntity.isScanning()));
    }

    private void getInventoryOnServer() {
        InvBlockInfo invBlockInfo = getSelectedContainer();
        if (invBlockInfo != null) {
            Coordinate c = invBlockInfo.getCoordinate();
            PacketHandler.INSTANCE.sendToServer(new PacketGetInventory(storageScannerTileEntity.xCoord, storageScannerTileEntity.yCoord, storageScannerTileEntity.zCoord,
                    c.getX(), c.getY(), c.getZ()));
        }
    }

    private InvBlockInfo getSelectedContainer() {
        int selected = storageList.getSelected();
        if (selected != -1) {
            SyncedValueList<InvBlockInfo> inventories = storageScannerTileEntity.getInventories();
            if (selected < inventories.size()) {
                InvBlockInfo invBlockInfo = inventories.get(selected);
                return invBlockInfo;
            }
        }
        return null;
    }

    private void updateContentsList() {
        List<ItemStack> items = storageScannerTileEntity.getShowingItems();
        if (itemList.getMaximum() == 0) {
            // We need to refresh.
            for (ItemStack stack : items) {
                if (stack != null) {
                    String displayName = BlockInfo.getReadableName(stack, 0);

                    Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
                    panel.addChild(new BlockRender(mc, this).setRenderItem(stack));
                    panel.addChild(new Label(mc, this).setDynamic(true).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT));
                    itemList.addChild(panel);
                }
            }
        }
    }

    private void updateStorageList() {
        SyncedValueList<InvBlockInfo> inventories = storageScannerTileEntity.getInventories();
        if (inventories.getClientVersion() != clientVersion) {
            clientVersion = inventories.getClientVersion();
            storageList.removeChildren();
            for (InvBlockInfo blockInfo : inventories) {
                Coordinate c = blockInfo.getCoordinate();
                Block block = mc.theWorld.getBlock(c.getX(), c.getY(), c.getZ());
                int meta = mc.theWorld.getBlockMetadata(c.getX(), c.getY(), c.getZ());
                String displayName = BlockInfo.getReadableName(block, meta);

                Panel panel = new Panel(mc, this).setLayout(new HorizontalLayout());
                panel.addChild(new BlockRender(mc, this).setRenderItem(block));
                panel.addChild(new Label(mc, this).setText(displayName).setHorizontalAlignment(HorizontalAlignment.ALIGH_LEFT).setDesiredWidth(120));
                panel.addChild(new Label(mc, this).setDynamic(true).setText(c.toString()));
                storageList.addChild(panel);
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        java.util.List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            int x = Mouse.getEventX() * width / mc.displayWidth;
            int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;
            drawHoveringText(tooltips, x - guiLeft, y - guiTop, mc.fontRenderer);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        updateStorageList();
        updateContentsList();
        if (storageScannerTileEntity.isScanning()) {
            scanButton.setText("Stop");
            progressBar.setValue(storageScannerTileEntity.getProgress());
        } else {
            scanButton.setText("Scan");
            progressBar.setValue(0);
        }
        window.draw();
        int currentRF = storageScannerTileEntity.getCurrentRF();
        energyBar.setValue(currentRF);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        window.handleMouseInput();
    }

    @Override
    protected void mouseMovedOrUp(int x, int y, int button) {
        super.mouseMovedOrUp(x, y, button);
        window.mouseMovedOrUp(x, y, button);
    }

}
