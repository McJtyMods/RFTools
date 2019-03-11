package mcjty.rftools.blocks.storage;

import mcjty.lib.gui.GenericGuiContainer;
import mcjty.lib.tileentity.GenericEnergyStorageTileEntity;
import mcjty.lib.gui.Window;
import mcjty.lib.gui.layout.PositionalLayout;
import mcjty.lib.gui.widgets.EnergyBar;
import mcjty.lib.gui.widgets.ImageChoiceLabel;
import mcjty.lib.gui.widgets.Panel;
import mcjty.lib.typed.TypedMap;
import mcjty.rftools.RFTools;
import mcjty.rftools.proxy.GuiProxy;
import mcjty.rftools.network.RFToolsMessages;
import net.minecraft.util.ResourceLocation;

import java.awt.Rectangle;

import static mcjty.rftools.blocks.storage.RemoteStorageTileEntity.PARAM_GLOBAL;
import static mcjty.rftools.blocks.storage.RemoteStorageTileEntity.PARAM_INDEX;


public class GuiRemoteStorage extends GenericGuiContainer<RemoteStorageTileEntity> {
    public static final int STORAGE_WIDTH = 180;
    public static final int STORAGE_HEIGHT = 152;

    private static final ResourceLocation iconLocation = new ResourceLocation(RFTools.MODID, "textures/gui/remotestorage.png");
    private static final ResourceLocation guiElements = new ResourceLocation(RFTools.MODID, "textures/gui/guielements.png");

    private EnergyBar energyBar;
    private ImageChoiceLabel global[] = new ImageChoiceLabel[] { null, null, null, null };

    public GuiRemoteStorage(RemoteStorageTileEntity remoteStorageTileEntity, RemoteStorageContainer container) {
        super(RFTools.instance, RFToolsMessages.INSTANCE, remoteStorageTileEntity, container, GuiProxy.GUI_MANUAL_MAIN, "remotestor");

        xSize = STORAGE_WIDTH;
        ySize = STORAGE_HEIGHT;
    }

    @Override
    public void initGui() {
        super.initGui();

        long maxEnergyStored = tileEntity.getCapacity();
        energyBar = new EnergyBar(mc, this).setVertical().setMaxValue(maxEnergyStored).setLayoutHint(10, 7, 8, 54).setShowText(false);
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());

        Panel toplevel = new Panel(mc, this).setBackground(iconLocation).setLayout(new PositionalLayout()).addChild(energyBar);
        for (int i = 0 ; i < 4 ; i++) {
            global[i] = new ImageChoiceLabel(mc, this);
            final int finalI = i;
            global[i].addChoiceEvent((parent, newChoice) -> changeGlobal(finalI));
            global[i].addChoice("off" + i, "Cross-dimension access disabled", guiElements, 0, 32);
            global[i].addChoice("on" + i, "Cross-dimension access enabled", guiElements, 16, 32);
            global[i].setLayoutHint(new PositionalLayout.PositionalHint(i < 2 ? (43 - 18) : (120 - 18), (i % 2) == 0 ? 9 : 36, 16, 16));
            global[i].setCurrentChoice(tileEntity.isGlobal(i) ? 1 : 0);
            toplevel.addChild(global[i]);
        }

        toplevel.setBounds(new Rectangle(guiLeft, guiTop, xSize, ySize));

        window = new Window(this, toplevel);
    }

    private void changeGlobal(int index) {
        sendServerCommand(RFToolsMessages.INSTANCE, RemoteStorageTileEntity.CMD_SETGLOBAL,
                TypedMap.builder()
                        .put(PARAM_INDEX, index)
                        .put(PARAM_GLOBAL, global[index].getCurrentChoiceIndex() == 1)
                        .build());
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float v, int i, int i2) {
        drawWindow();
        energyBar.setValue(GenericEnergyStorageTileEntity.getCurrentRF());
        tileEntity.requestRfFromServer(RFTools.MODID);
    }
}
