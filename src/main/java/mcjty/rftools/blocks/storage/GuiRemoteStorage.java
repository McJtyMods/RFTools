package mcjty.rftools.blocks.storage;

import mcjty.container.GenericGuiContainer;
import mcjty.gui.Window;
import mcjty.gui.layout.PositionalLayout;
import mcjty.gui.widgets.EnergyBar;
import mcjty.gui.widgets.Panel;
import mcjty.gui.widgets.Widget;
import mcjty.rftools.RFTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import java.awt.*;


public class GuiRemoteStorage extends GenericGuiContainer<RemoteStorageTileEntity> {
    public static final int STORAGE_WIDTH = 180;
    public static final int STORAGE_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/remotestorage.png");

    private EnergyBar energyBar;

    public GuiRemoteStorage(RemoteStorageTileEntity remoteStorageTileEntity, RemoteStorageContainer container) {
        super(remoteStorageTileEntity, container);

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        int maxEnergyStored = tileEntity.getMaxEnergyStored(ForgeDirection.DOWN);
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(new PositionalLayout.PositionalHint(10, 7, 8, 54)).setShowText(false);
        energyBar.setValue(tileEntity.getCurrentRF());

        Widget toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        window.draw();
        energyBar.setValue(tileEntity.getCurrentRF());
        tileEntity.requestRfFromServer();
    }
}
